package lordick.bot.commands;

import lordick.Lordick;
import lordick.bot.CommandListener;
import lordick.bot.UnhandledCommandListener;
import xxx.moparisthebest.irclib.messages.IrcMessage;

public class Help implements UnhandledCommandListener, CommandListener {

    @Override
    public String getHelp() {
        return "you're an idiot";
    }

    @Override
    public String getCommands() {
        return "help";
    }

    @Override
    public void handleCommand(Lordick client, String command, IrcMessage message) {
        if (message.isSpam()) {
            message.sendChatf("ur mental %s", message.getHostmask().getNick());
            return;
        }
        if (!message.hasMessage()) {
            StringBuilder commandList = new StringBuilder();
            for (String s : client.getCommandListeners().keySet()) {
                if (commandList.length() > 0) {
                    commandList.append(',');
                }
                commandList.append(s.toLowerCase());
            }
            message.sendChatf("Available commands: %s", commandList.toString());
            return;
        }
        String cmd = message.getMessage().toLowerCase();
        if (!client.getCommandListeners().containsKey(cmd)) {
            message.sendChatf("No help for command: %s", cmd);
        } else {
            String help = client.getCommandListeners().get(cmd).getHelp();
            if (help == null || help.isEmpty()) {
                message.sendChatf("No help available for: %s", cmd);
            } else {
                message.sendChatf("Help for %s: %s", cmd, help);
            }
        }
    }

    @Override
    public void unhandledCommand(Lordick client, String command, IrcMessage message) {
        // message.sendChatf("Unknown command: %s", command);
    }
}
