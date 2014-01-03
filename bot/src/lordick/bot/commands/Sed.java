package lordick.bot.commands;

import lordick.Lordick;
import lordick.bot.CommandListener;
import lordick.bot.InitListener;
import lordick.bot.MessageListener;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Sed implements CommandListener, MessageListener, InitListener {

    private static Pattern SED_REGEX = Pattern.compile("^s/(.*?)/(.*?)/(g?)");

    private static String LASTMESSAGE_PREFIX = "sed.lastmessage.";

    @Override
    public void handleCommand(Lordick client, String command, IrcMessage message) {
        message.sendChat(getHelp());
    }

    @Override
    public String getHelp() {
        return "Usage: s/search/replace/[g]";
    }

    @Override
    public String getCommands() {
        return "sed";
    }

    @Override
    public void onMessage(Lordick client, IrcMessage message) {
        if (!message.isDestChannel() || !message.hasMessage()) {
            return;
        }
        Matcher m = SED_REGEX.matcher(message.getMessage());
        if (m.find()) {
            String lastmessage = client.getKeyValue(message.getServer(), LASTMESSAGE_PREFIX + message.getHostmask().getNick());
            if (lastmessage == null || lastmessage.isEmpty()) {
                message.sendChatf("%s: No last message on record", message.getHostmask().getNick());
                return;
            }
            String reply;
            if (m.group(3) == null || m.group(3).isEmpty()) {
                reply = lastmessage.replaceFirst(m.group(1), m.group(2));
            } else if (m.group(3) != null && m.group(3).equals("g")) {
                reply = lastmessage.replaceAll(m.group(1), m.group(2));
            } else {
                message.sendChatf("%s: You did something wrong... %s", message.getHostmask().getNick(), getHelp());
                return;
            }
            message.sendChatf("%s meant: %s", message.getHostmask().getNick(), reply);
        } else {
            client.setKeyValue(message.getServer(), LASTMESSAGE_PREFIX + message.getHostmask().getNick(), message.getMessage());
        }
    }

    @Override
    public boolean init(final Lordick client) {
        // delete old sed data every 60 minutes
        client.getGroup().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Statement s = client.getDatabaseConnection().createStatement();
                    s.execute("delete from keyvalues where key like '" + LASTMESSAGE_PREFIX + "%'");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60, TimeUnit.MINUTES);
        return true;
    }
}
