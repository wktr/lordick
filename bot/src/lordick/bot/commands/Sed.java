package lordick.bot.commands;

import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sed extends BotCommand {

    private static Pattern SED_REGEX = Pattern.compile("^s([\\/|,!])(.*?)\\1(.*?)\\1?(g?)");

    private Map<String, String> lastMessage = new HashMap<String, String>();

    @Override
    public String getHelp() {
        return "Usage: s/search/replace/[g]";
    }

    @Override
    public String getCommand() {
        return "sed";
    }

    @Override
    public boolean shouldHandleMessage(IrcClient client, IrcMessage message) {
        if (message.isDestChannel()) {
            boolean sedmatch = message.getMessage().matches(SED_REGEX.pattern());
            if (!sedmatch) {
                lastMessage.put(message.getPrefix(), message.getMessage());
            }
            return sedmatch;
        }
        return false;
    }

    @Override
    public void handleMessage(IrcClient client, IrcMessage message) {
        if (!lastMessage.containsKey(message.getPrefix())) {
            return;
        }
        Matcher m = SED_REGEX.matcher(message.getMessage());
        if (m.find()) {
            String last = lastMessage.get(message.getPrefix());
            String reply;
            if (m.group(4) == null || m.group(4).equals("")) {
                reply = last.replaceFirst(m.group(2), m.group(3));
            } else if (m.group(4) != null && m.group(4).equals("g")) {
                reply = last.replaceAll(m.group(2), m.group(3));
            } else {
                message.sendChatf("%s: You did something wrong... %s", message.getHostmask().getNick(), getHelp());
                return;
            }
            message.sendChatf("%s meant: %s", message.getHostmask().getNick(), reply);
        }
    }
}
