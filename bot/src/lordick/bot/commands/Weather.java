package lordick.bot.commands;

import io.netty.channel.Channel;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.util.Hostmask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Weather extends BotCommand {

    private static final String WEATHER_URL = "http://mobile.wunderground.com/cgi-bin/findweather/getForecast?brand=mobile&query=";
    private static Pattern table = Pattern.compile("<table border=\"1\" width=\"100%\">(.+?)</table>");

    @Override
    public String getHelp() {
        return "Usage: weather location";
    }

    @Override
    public String getCommand() {
        return "weather";
    }

    @Override
    public boolean shouldHandleCommand(IrcClient client, Channel channel, IrcChat chat) {
        return chat.isChannel() && chat.getMessage().matches(getCommand() + ":? .+");
    }

    @Override
    public void handleCommand(IrcClient client, Channel channel, IrcChat chat) {
        String location = chat.getMessage().substring(chat.getMessage().indexOf(" ") + 1);
        location = location.replaceAll("\\s+", "");
        String data = readurl(WEATHER_URL + location);
        if (data != null) {
            Matcher m = table.matcher(data.replaceAll("\t+", ""));
            if (m.find()) {
                String weather = m.group(1).replaceAll("</?b>", "\02").replaceAll("</tr>", ",").replaceAll("<.+?>", " ").replaceAll("&.+?;", " ").replaceAll("\\s+", " ").replaceAll("( ,)+", ",").replaceAll(", UV .*", "").trim();
                String[] moredata = weather.split("\02,", 2);
                IrcClient.sendChat(channel, chat.getDestination(), "%s: %s", Hostmask.getNick(chat.getPrefix()), moredata[0]);
                IrcClient.sendChat(channel, chat.getDestination(), moredata[1].trim());
            } else {
                IrcClient.sendChat(channel, chat.getDestination(), "%s: Invalid location: %s", Hostmask.getNick(chat.getPrefix()), location);
            }
        } else {
            IrcClient.sendChat(channel, chat.getDestination(), "%s: Unable to read weather data", Hostmask.getNick(chat.getPrefix()));
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
