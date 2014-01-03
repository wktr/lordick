package lordick.bot.commands;

import lordick.Lordick;
import lordick.bot.CommandListener;
import lordick.bot.MessageListener;
import lordick.util.Duration;
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
            message.sendChatf("Last seen %s: %s ago, %s", nick, Duration.getReadableDuration(client.getKeyValueLong(message.getServer(), "lastseen.time." + nick), System.currentTimeMillis()), lastSeenEvent);
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

}
