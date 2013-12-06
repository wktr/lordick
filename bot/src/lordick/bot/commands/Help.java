package lordick.bot.commands;

import lordick.Lordick;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.messages.IrcMessage;

public class Help extends BotCommand {

    @Override
    public String getHelp() {
        return "you're an idiot";
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public void handleCommand(Lordick client, String command, IrcMessage message) {
        if (!message.hasMessage()) {
            StringBuilder commandList = new StringBuilder();
            for (BotCommand botCommand : client.commandHandlers) {
                if (botCommand.getCommandList() != null) {
                    for (String s : botCommand.getCommandList()) {
                        if (commandList.length() > 0) {
                            commandList.append(',');
                        }
                        commandList.append(s.toLowerCase());
                    }
                }
                if (botCommand.getCommand() != null) {
                    if (commandList.length() > 0) {
                        commandList.append(',');
                    }
                    commandList.append(botCommand.getCommand().toLowerCase());
                }
            }
            message.sendChatf("Available commands: %s", commandList.toString());
            return;
        }
        String cmd = message.getMessage().toLowerCase();
        if (!client.commandList.containsKey(cmd)) {
            message.sendChatf("No help for command: %s", cmd);
        } else {
            String help = client.commandList.get(cmd).getHelp();
            if (help == null || help.isEmpty()) {
                message.sendChatf("No help available for: %s", cmd);
            } else {
                message.sendChatf("Help for %s: %s", cmd, help);
            }
        }
    }

    @Override
    public void unhandledCommand(Lordick client, String command, IrcMessage message) {
        message.sendChatf("Unknown command: %s", command);
    }
}
