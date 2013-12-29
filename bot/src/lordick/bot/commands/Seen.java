package lordick.bot.commands;

import lordick.Lordick;
import lordick.bot.CommandListener;
import lordick.bot.MessageListener;
import xxx.moparisthebest.irclib.messages.IrcMessage;

@SuppressWarnings("unused")
public class Seen implements CommandListener, MessageListener {

    @Override
    public String getHelp() {
        return "Usage: seen: nick";
    }

    @Override
    public String getCommands() {
        return "seen";
    }

    @Override
    public void handleCommand(Lordick client, String command, IrcMessage message) {
        if (!message.hasMessage()) {
            message.sendChatf(getHelp());
            return;
        }
        String nick = message.getMessage().toLowerCase().split(" ", 2)[0];
        String lastSeenEvent = client.getKeyValue(message.getServer(), "lastseen.event." + nick);
        if (lastSeenEvent == null) {
            message.sendChatf("Have not seen: %s", nick);
        } else {
            message.sendChatf("Last seen %s: %s ago, %s", nick, getReadable(client.getKeyValueLong(message.getServer(), "lastseen.time." + nick), System.currentTimeMillis()), lastSeenEvent);
        }
    }

    @Override
    public void onMessage(Lordick client, IrcMessage message) {
        String lastSeenEvent = null;
        if (message.getCommand().equals("PRIVMSG")) {
            lastSeenEvent = "saying: " + message.getMessage();
        } else if (message.getCommand().equals("JOIN")) {
            lastSeenEvent = "joining " + message.getMessage();
        } else if (message.getCommand().equals("PART")) {
            lastSeenEvent = "leaving " + message.getTargetParams() + ": " + message.getMessage();
        } else if (message.getCommand().equals("QUIT")) {
            lastSeenEvent = "quitting: " + message.getMessage();
        } else if (message.getCommand().equals("NICK")) {
            lastSeenEvent = "changing nick to: " + message.getTargetParams();
        }
        if (lastSeenEvent != null) {
            String nick = message.getHostmask().getNick().toLowerCase();
            client.setKeyValue(message.getServer(), "lastseen.time." + nick, System.currentTimeMillis());
            client.setKeyValue(message.getServer(), "lastseen.event." + nick, lastSeenEvent);
        }
    }

    private static void dostuff(StringBuilder sb, long n, String s) {
        if (n > 0) {
            sb.append(n);
            sb.append(' ');
            sb.append(s);
            if (n > 1) {
                sb.append('s');
            }
            sb.append(' ');
        }
    }

    public static String getReadable(long from, long to) {
        long diffInSeconds = (to - from) / 1000;
        if (diffInSeconds < 10) {
            return "just now";
        }

        long seconds = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        long minutse = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hours = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = (diffInSeconds = (diffInSeconds / 24));

        StringBuilder result = new StringBuilder();
        dostuff(result, days, "day");
        dostuff(result, hours, "hour");
        dostuff(result, minutse, "minute");
        dostuff(result, seconds, "second");
        return result.toString().trim();
    }
}
