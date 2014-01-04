package lordick.bot.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lordick.Lordick;
import lordick.bot.CommandListener;
import lordick.bot.InitListener;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: t4
 * Date: 1/3/14
 * Time: 1:37
 * To change this template use File | Settings | File Templates.
 */
public class Urban implements CommandListener, InitListener {

    private static final String URBAN_URL = "http://api.urbandictionary.com/v0/define?term=";
    private static int TIMEOUT = 10 * 60 * 1000; // length between queries in MS

    private static String LASTRESULT_PREFIX = "urban.lastresult.";
    private static String LASTQUERY_PREFIX = "urban.lastquery.";

    @Override
    public boolean init(final Lordick client) {
        client.getGroup().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Statement s = client.getDatabaseConnection().createStatement();
                    s.execute("delete from keyvalues where key like (select 'urban.%.' || substr(key, 17) from keyvalues " +
                            "where key like '" + LASTQUERY_PREFIX + ".%' " +
                            "and value < " + (System.currentTimeMillis() - TIMEOUT) + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, TIMEOUT * 2, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public String getHelp() {
        return "Usage: urban [term]";
    }

    @Override
    public String getCommands() {
        return "urban";
    }

    @Override
    public void handleCommand(Lordick client, String command, IrcMessage message) {
        if (!message.hasMessage()) {
            message.sendChatf(getHelp());
            return;
        }
        String term = message.getMessage();

        long lastquery = client.getKeyValueLong(message.getServer(), LASTQUERY_PREFIX + term);
        if (lastquery > 0 && System.currentTimeMillis() - lastquery < TIMEOUT) { // if last query was less than 10 minutes ago, send last data
            String lastdata = client.getKeyValue(message.getServer(), LASTRESULT_PREFIX + term);
            if (lastdata != null && !lastdata.isEmpty()) {
                message.sendChatf("%s: %s", message.getHostmask().getNick(), lastdata);
            } else {
                message.sendChatf("%s: There was an error last time retrieving the definition for %s, please try again in %d minute(s)", message.getHostmask().getNick(),
                        TimeUnit.MILLISECONDS.toMinutes(TIMEOUT - (System.currentTimeMillis() - lastquery)));
            }
        } else {
            String term_encoded;
            try {
                term_encoded = URLEncoder.encode(term, "UTF-8");
            } catch (Exception ex) {
                message.sendChatf("%s: Error parsing location, %s", message.getHostmask().getNick(), ex.getMessage());
                return;
            }
            client.setKeyValue(message.getServer(), LASTQUERY_PREFIX + term, System.currentTimeMillis());
            HttpURLConnection conn;

            try {
                URL url = new URL(URBAN_URL + term_encoded);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
            } catch (Exception ex) {
                message.sendChatf("%s: Error connecting to urban service, %s", message.getHostmask().getNick(), ex.getMessage());
                return;
            }

            BufferedReader br;
            StringBuilder sb = new StringBuilder();
            try {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String data;
                while ((data = br.readLine()) != null) {
                    sb.append(data);
                    sb.append(' ');
                }
            } catch (Exception e) {
                message.sendChatf("%s: Error retrieving urban data for, %s", message.getHostmask().getNick(), e.getMessage());
                return;
            }
            try {
                br.close();
            } catch (Exception ignored) {
            }
            System.out.println("resp: " + sb.toString());
            JsonNode json;
            try {
                json = new ObjectMapper().readTree(sb.toString());
            } catch (Exception e) {
                message.sendChatf("%s: Error parsing urban data for, %s", message.getHostmask().getNick(), e.getMessage());
                return;
            }
            client.setKeyValue(message.getServer(), LASTQUERY_PREFIX + term, System.currentTimeMillis());
            StringBuilder definition = new StringBuilder(String.format("Urban definition of `%s`: ", term));

            try {
                definition.append(json.get("list").elements().next().get("definition").asText());
            } catch (Exception e) {
                message.sendChatf("%s: Error parsing urban data for, %s", message.getHostmask().getNick(), e.getMessage());
                return;
            }

            String formatted = definition.toString();
            System.out.println(formatted);
            client.setKeyValue(message.getServer(), LASTRESULT_PREFIX + term, formatted);
            message.sendChatf("%s: %s", message.getHostmask().getNick(), formatted);
        }

    }
}