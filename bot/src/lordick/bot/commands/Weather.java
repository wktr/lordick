package lordick.bot.commands;

import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.IrcMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Weather extends BotCommand {

    private static final String WEATHER_URL = "http://mobile.wunderground.com/cgi-bin/findweather/getForecast?brand=mobile&query=";
    private static Pattern table = Pattern.compile("<table border=\"1\" width=\"100%\">(.+?)</table>");
    private ConcurrentHashMap<String, String> lastWeather = new ConcurrentHashMap<String, String>();

    @Override
    public String getHelp() {
        return "Usage: weather [location]";
    }

    @Override
    public String getCommand() {
        return "weather";
    }

    @Override
    public boolean shouldHandleCommand(IrcClient client, IrcMessage message) {
        return message.isDestChannel() && message.getMessage().startsWith(getCommand());
    }

    @Override
    public void handleCommand(IrcClient client, IrcMessage message) {
        String location;
        if (!message.getMessage().contains(" ")) {
            if (!lastWeather.containsKey(message.getHostmask().getNick())) {
                message.sendChatf("%s: No previous location stored", message.getHostmask().getNick());
                return;
            } else {
                location = lastWeather.get(message.getHostmask().getNick());
            }
        } else {
            location = message.getMessage().substring(message.getMessage().indexOf(" ") + 1);
        }
        location = location.replaceAll("\\s+", "");
        String data = readurl(WEATHER_URL + location);
        if (data != null) {
            Matcher m = table.matcher(data.replaceAll("\t+", ""));
            if (m.find()) {
                lastWeather.put(message.getHostmask().getNick(), location);
                String weather = m.group(1).replaceAll("</?b>", "\02").replaceAll("</tr>", ",").replaceAll("<.+?>", " ").replaceAll("&.+?;", " ").replaceAll("\\s+", " ").replaceAll("( ,)+", ",").replaceAll(", UV .*", "").trim();
                String[] moredata = weather.split("\02,", 2);
                message.sendChatf("%s: %s", message.getHostmask().getNick(), moredata[0]);
                message.sendChat(moredata[1].trim());
            } else {
                message.sendChatf("%s: Invalid location: %s", message.getHostmask().getNick(), location);
            }
        } else {
            message.sendChatf("%s: Unable to read weather data", message.getHostmask().getNick());
        }
    }

    private String readurl(String url) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(' ');
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
