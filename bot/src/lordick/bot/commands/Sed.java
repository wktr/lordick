package lordick.bot.commands;

import io.netty.channel.Channel;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.util.Hostmask;

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
    public boolean shouldHandleMessage(IrcClient client, Channel channel, IrcChat chat) {
        if (chat.getType().equalsIgnoreCase("PRIVMSG") && chat.getDestination().startsWith("#")) {
            boolean sedmatch = chat.getMessage().matches(SED_REGEX.pattern());
            if (!sedmatch) {
                lastMessage.put(chat.getPrefix(), chat.getMessage());
            }
            return sedmatch;
        }
        return false;
    }

    @Override
    public void handleMessage(IrcClient client, Channel channel, IrcChat chat) {
        if (!lastMessage.containsKey(chat.getPrefix())) {
            return;
        }
        Matcher m = SED_REGEX.matcher(chat.getMessage());
        if (m.matches()) {
            String last = lastMessage.get(chat.getPrefix());
            String reply;
            if (m.group(4) == null || m.group(4).equals("")) {
                reply = last.replaceFirst(m.group(2), m.group(3));
            } else if (m.group(4) != null && m.group(4).equals("g")) {
                reply = last.replaceAll(m.group(2), m.group(3));
            } else {
                IrcClient.sendChat(channel, chat.getDestination(), "%s: You did something wrong... %s", Hostmask.getNick(chat.getPrefix()), getHelp());
                return;
            }
            IrcClient.sendChat(channel, chat.getDestination(), "%s meant: %s", Hostmask.getNick(chat.getPrefix()), reply);
        }
    }
}
