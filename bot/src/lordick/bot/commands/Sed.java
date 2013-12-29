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

    private static Pattern SED_REGEX = Pattern.compile("^s([/|,!])(.*?)\\1(.*?)\\1(g?)");

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
            if (message.isSpam()) {
                return;
            }
            String lastmessage = client.getKeyValue(message.getServer(), "lastmessage." + message.getHostmask().getNick());
            if (lastmessage == null || lastmessage.isEmpty()) {
                return;
            }
            String reply;
            if (m.group(4) == null || m.group(4).equals("")) {
                reply = lastmessage.replaceFirst(m.group(2), m.group(3));
            } else if (m.group(4) != null && m.group(4).equals("g")) {
                reply = lastmessage.replaceAll(m.group(2), m.group(3));
            } else {
                message.sendChatf("%s: You did something wrong... %s", message.getHostmask().getNick(), getHelp());
                return;
            }
            message.sendChatf("%s meant: %s", message.getHostmask().getNick(), reply);
        } else {
            client.setKeyValue(message.getServer(), "lastmessage." + message.getHostmask().getNick(), message.getMessage());
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
                    s.execute("delete from keyvalues where key like 'lastmessage.%'");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60, TimeUnit.MINUTES);
        return true;
    }
}
