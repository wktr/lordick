package lordick.bot.commands;

import lordick.Lordick;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Seen extends BotCommand {

    private Map<String, String> lastSeenEvent = new ConcurrentHashMap<String, String>();
    private Map<String, Long> lastSeenTime = new ConcurrentHashMap<String, Long>();

    @Override
    public String getHelp() {
        return "Usage: seen: nick";
    }

    @Override
    public String getCommand() {
        return "seen";
    }

    @Override
    public void handleCommand(Lordick client, String command, IrcMessage message) {
        if (!message.hasMessage()) {
            message.sendChatf(getHelp());
            return;
        }
        String nick = message.getMessage().toLowerCase();
        if (!lastSeenEvent.containsKey(nick)) {
            message.sendChatf("Have not seen: %s", nick);
        } else {
            message.sendChatf("Last seen %s: %s, %s", nick, getReadable(lastSeenTime.get(nick)), lastSeenEvent.get(nick));
        }
    }

    private String notnullpls(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void onMessage(Lordick client, IrcMessage message) {
        if (message.getCommand().equals("PRIVMSG")) {
            newSeen(message.getHostmask().getNick(), "saying: " + notnullpls(message.getMessage()));
        } else if (message.getCommand().equals("JOIN")) {
            newSeen(message.getHostmask().getNick(), "joining " + notnullpls(message.getMessage()));
        } else if (message.getCommand().equals("PART")) {
            newSeen(message.getHostmask().getNick(), "leaving " + message.getTargetParams() + ": " + notnullpls(message.getMessage()));
        } else if (message.getCommand().equals("QUIT")) {
            newSeen(message.getHostmask().getNick(), "quitting: " + notnullpls(message.getMessage()));
        } else if (message.getCommand().equals("NICK")) {
            newSeen(message.getHostmask().getNick(), "changing nick to: " + message.getTargetParams());
        }
    }

    private void newSeen(String nick, String message) {
        String n = nick.toLowerCase();
        lastSeenEvent.put(n, message);
        lastSeenTime.put(n, System.currentTimeMillis());
    }

    private void dostuff(StringBuilder sb, long n, String s) {
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

    private String getReadable(long from) {
        long diffInSeconds = (System.currentTimeMillis() - from) / 1000;
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
        result.append("ago");
        return result.toString();
    }
}
