package lordick.bot.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lordick.Lordick;
import lordick.bot.InitListener;
import lordick.bot.MessageListener;
import lordick.util.Duration;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class LinkMetadata implements MessageListener, InitListener {

    private static int TIMEOUT = 60 * 60 * 1000;  // length between queries in MS

    private static String LASTRESULT_PREFIX = "url.lastresult.";
    private static String LASTQUERY_PREFIX = "url.lastquery.";

    @Override
    public boolean init(final Lordick client) {
        System.setProperty("http.agent", "");
        client.getGroup().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Statement s = client.getDatabaseConnection().createStatement();
                    s.execute("delete from keyvalues where key like (select 'url.%.' || substr(key, 15) from keyvalues " +
                            "where key like '" + LASTQUERY_PREFIX + ".%' " +
                            "and value < " + (System.currentTimeMillis() - TIMEOUT) + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, TIMEOUT * 2, TimeUnit.MILLISECONDS);
        return true;
    }

    private static String getRegex(String input, Pattern pattern) {
        Matcher m = pattern.matcher(input);
        if (m.find()) {
            return m.group(1).trim().replaceAll("[\r\n]+", "");
        } else {
            return null;
        }
    }

    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private static class RedditApi implements Runnable {

        StringBuilder sb;
        String url;
        CountDownLatch latch;

        private RedditApi(StringBuilder sb, String url, CountDownLatch latch) {
            this.sb = sb;
            this.url = url;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                JsonNode root = new ObjectMapper().readTree(downloadString(new URL("http://www.reddit.com/api/info.json?url=" + URLEncoder.encode(url, "UTF-8")).openStream()));
                JsonNode node = root.path("data").path("children").path(0).path("data");
                if (!node.isMissingNode()) {
                    long created_utc = node.get("created_utc").longValue() * 1000;
                    long duration = System.currentTimeMillis() - created_utc;
                    if (TimeUnit.MILLISECONDS.toDays(duration) < 7) { // only post it if it's recent
                        sb.append("[REDDIT] ");
                        appendSB(sb, "Posted: ", Duration.getReadableDuration(duration));
                        appendSB(sb, ", Title: ", node.get("title").textValue());
                        if (node.get("over_18").booleanValue()) {
                            sb.append(" !!!NSFW!!!");
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // who cares if reddit api spazzes
            }
            latch.countDown();
        }
    }

    private static Pattern youtube_title = Pattern.compile("<meta property=\"og:title\" content=\"(.+)\">");
    private static Pattern youtube_length = Pattern.compile("\"length_seconds\": (\\d+)");
    private static Pattern youtube_views = Pattern.compile("([\\d,]+) views");
    private static Pattern youtube_likes = Pattern.compile("\"likes-count\">([\\d,]+)");
    private static Pattern youtube_dislikes = Pattern.compile("\"dislikes-count\">([\\d,]+)");

    private static Pattern html_title = Pattern.compile("<title>(.+)</title>");

    private static Pattern imgur_direct = Pattern.compile("https?://(?:i\\.)?imgur\\.com/(.+)\\..{3,4}(?:\\?.+)?");
    private static Pattern imgur_width = Pattern.compile("<meta name=\"twitter:image:width\" content=\"(\\d+)\"/>");
    private static Pattern imgur_height = Pattern.compile("<meta name=\"twitter:image:height\" content=\"(\\d+)\"/>");
    private static Pattern imgur_title = Pattern.compile("\"title\":\"(.+?)\"");
    private static Pattern imgur_uploaded = Pattern.compile("<span id=\"nicetime\" title=\".+?\">(.+)</span>");
    private static Pattern imgur_views = Pattern.compile("<span id=\"views\">(.+)</span>");
    private static Pattern imgur_bandwidth = Pattern.compile("<span id=\"bandwidth\">(.+)</span>");
    private static Pattern imgur_nsfw = Pattern.compile("\"nsfw\":(true)");

    private static String downloadString(InputStream is) throws Exception {
        StringBuilder htmlBuilder = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            htmlBuilder.append(line);
            htmlBuilder.append("\n");
        }
        br.close();
        return htmlBuilder.toString();
    }

    private static void getMetaData(URL url, StringBuilder result) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
            if (url.getHost().startsWith("mopar") || url.getHost().startsWith("www.mopar")) {
                conn.setRequestProperty("Cookie", "mpr_agree4=yes");
            }
            parseConnection(result, conn);
        } catch (Exception e) {
            result.append("Error getting url data: ").append(e);
        } finally {
            if (conn != null) {
                try {
                    conn.getInputStream().close();
                } catch (Exception ignored) {
                } finally {
                    conn.disconnect();
                }
            }
        }
    }

    private static void appendSB(StringBuilder sb, String s1, Object o) {
        if (o != null) {
            String s2 = o.toString();
            if (!s2.isEmpty()) {
                sb.append(s1).append(s2);
            }
        }
    }

    private static void parseConnection(StringBuilder result, HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        if (code == 302) {
            String location = conn.getHeaderField("Location");
            result.append("[SITE] Http 302 moved: ");
            if (location == null) {
                result.append("with location field");
            } else {
                result.append(location);
            }
            return;
        } else if (code >= 400) {
            result.append("[SITE] Error: ").append(conn.getResponseMessage());
            return;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final String url = conn.getURL().toExternalForm();
        final StringBuilder reddit = new StringBuilder();
        new Thread(new RedditApi(reddit, url, latch)).start();
        String host = conn.getURL().getHost().replaceFirst("www\\.", "");
        String contentType = conn.getContentType();
        if (contentType.startsWith("text/html")) {
            String html = downloadString(conn.getInputStream());
            if (host.equalsIgnoreCase("youtube.com") && html.contains("\"og:title\"")) {
                // wtb lambda
                result.append("[YOUTUBE] ");
                String title = getRegex(html, youtube_title);
                if (title != null) {
                    appendSB(result, "Title: ", title);
                } else {
                    throw new Exception("unable to get youtube video title");
                }
                String length = getRegex(html, youtube_length);
                if (length != null) {
                    appendSB(result, ", Length: ", Duration.getReadableDuration(Long.valueOf(length) * 1000));
                }
                appendSB(result, ", Views: ", getRegex(html, youtube_views));
                appendSB(result, ", Likes: ", getRegex(html, youtube_likes));
                appendSB(result, ", Dislikes: ", getRegex(html, youtube_dislikes));
            } else if (host.equalsIgnoreCase("imgur.com") && html.contains("\"twitter:image:width\"")) {
                String width = getRegex(html, imgur_width);
                String height = getRegex(html, imgur_height);
                if (width == null || height == null) {
                    throw new Exception("unable to get imgur image dimensions");
                } else {
                    appendSB(result, "Dimensions: ", width + "x" + height);
                }
                appendSB(result, ", Title: ", getRegex(html, imgur_title));
                appendSB(result, ", Uploaded: ", getRegex(html, imgur_uploaded));
                appendSB(result, ", Views: ", getRegex(html, imgur_views));
                appendSB(result, ", Bandwidth: ", getRegex(html, imgur_bandwidth));
                String nsfw = getRegex(html, imgur_nsfw);
                if (nsfw != null) {
                    result.append(" !!!NSFW!!!");
                }
            } else {
                String title = getRegex(html, html_title);
                if (title != null) {
                    appendSB(result, "[SITE] Title: ", title);
                }
            }
        } else if (contentType.startsWith("image/") && url.matches(imgur_direct.pattern())) {
            URL newurl = new URL("http://imgur.com/" + getRegex(url, imgur_direct));
            result.append("[IMGUR] ");
            long size = conn.getContentLength();
            if (size > 0) {
                result.append("Size: ").append(humanReadableByteCount(size, false)).append(", ");
            }
            result.append("Type: ").append(contentType).append(", ");
            getMetaData(newurl, result);
        } else if (contentType.startsWith("image/")) {
            BufferedImage bimg = ImageIO.read(conn.getInputStream());
            result.append("[IMAGE] Type: ").append(contentType)
                    .append(", Dimensions: ").append(bimg.getWidth()).append("x").append(bimg.getHeight());
            long size = conn.getContentLength();
            if (size > 0) {
                result.append(", Size: ").append(humanReadableByteCount(size, false));
            }
        } else {
            result.append("[URL] Type: ").append(conn.getContentType());
            long size = conn.getContentLength();
            if (size > 0) {
                result.append(", Size: ").append(humanReadableByteCount(size, false));
            }
        }
        latch.await(2, TimeUnit.SECONDS);
        if (reddit.length() > 0) {
            result.append(' ');
            result.append(reddit);
        }
    }

    @Override
    public void onMessage(Lordick client, IrcMessage message) {
        if (message.isSpam() || !message.isDestChannel() || !message.hasMessage()) {
            return;
        }
        for (String word : message.getMessage().split("\\s+")) {
            URL url;
            try {
                url = new URL(word);
            } catch (Exception ignored) {
                continue;
            }
            try {
                if (!url.getProtocol().matches("https?")) {
                    return;
                }
                String lastResult = client.getKeyValue(message.getServer(), LASTRESULT_PREFIX + word);
                if (lastResult != null && System.currentTimeMillis() - client.getKeyValueLong(message.getServer(), LASTQUERY_PREFIX + word) < TIMEOUT) { // last time was < 60 minutes ago
                    message.sendChat(lastResult);
                } else {
                    StringBuilder result = new StringBuilder();
                    getMetaData(url, result);
                    if (result.length() > 0) {
                        message.sendChat(result.toString());
                        client.setKeyValue(message.getServer(), LASTRESULT_PREFIX + url.toExternalForm(), result);
                        client.setKeyValue(message.getServer(), LASTQUERY_PREFIX + url.toExternalForm(), System.currentTimeMillis());
                    } else {
                        message.sendChatf("No url data available");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        }
    }
}
