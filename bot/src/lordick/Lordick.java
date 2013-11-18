package lordick;

import io.netty.channel.Channel;
import lordick.bot.BotCommand;
import lordick.bot.commands.Karma;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.properties.NetworkProperties;
import xxx.moparisthebest.irclib.properties.UserProperties;
import xxx.moparisthebest.util.ClassEnumerator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lordick extends IrcClient {

    private List<BotCommand> commandHandlers = new CopyOnWriteArrayList<BotCommand>();
    private Map<String, BotCommand> commandList = new ConcurrentHashMap<String, BotCommand>();
    private String commandListString;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addCommandList(String s, BotCommand c) {
        commandList.put(s, c);
        if (commandListString == null) {
            commandListString = s;
        } else {
            commandListString += "," + s;
        }
    }

    public void loadCommandHandlers() {
        commandHandlers.clear();
        commandListString = null;
        for (Class c : ClassEnumerator.getClassesForPackage(Karma.class.getPackage())) {
            try {
                BotCommand command = (BotCommand) c.newInstance();
                commandHandlers.add(command);
                if (command.getCommandList() != null) {
                    for (String s : command.getCommandList()) {
                        addCommandList(s, command);
                    }
                }
                if (command.getCommand() != null) {
                    addCommandList(command.getCommand(), command);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        loadCommandHandlers();
        UserProperties up = new UserProperties("lordick", "lordick", "lordick", "lordick", null, "#lordick", "#mopar");
        NetworkProperties np = new NetworkProperties("irc.moparisthebest.xxx", 6667, false);
        connect(up, np);
    }

    private static Pattern help = Pattern.compile("help(?:[:]? (\\S+))?");

    @Override
    public void OnIrcMessage(Channel channel, IrcChat chat) {
        super.OnIrcMessage(channel, chat);
        if (!chat.isChannel()) {
            return;
        }
        UserProperties up = IrcClient.getUserProperties(channel);
        if (chat.getMessage().matches("^" + up.getNickname() + "[:,]* .+")) {
            String text = chat.getMessage().substring(chat.getMessage().indexOf(' ') + 1);
            Matcher m = help.matcher(text);
            if (m.matches()) {
                String command = m.group(1);
                if (command == null) {
                    IrcClient.sendChat(channel, chat.getDestination(), "Help available for: %s", commandListString);
                } else if (!commandList.containsKey(command)) {
                    IrcClient.sendChat(channel, chat.getDestination(), "No help for command: %s", command);
                } else {
                    IrcClient.sendChat(channel, chat.getDestination(), "Help for %s: %s", command, commandList.get(command).getHelp());
                }
            } else {
                chat.setMessage(text);
                boolean handled = false;
                for (BotCommand botCommand : commandHandlers) {
                    if (botCommand.shouldHandleCommand(this, channel, chat)) {
                        handled = true;
                        botCommand.handleCommand(this, channel, chat);
                    }
                }
                if (!handled) {
                    for (BotCommand botCommand : commandHandlers) {
                        botCommand.unhandledCommand(this, channel, chat);
                    }
                }
            }
        } else {
            boolean handled = false;
            for (BotCommand botCommand : commandHandlers) {
                if (botCommand.shouldHandleMessage(this, channel, chat)) {
                    handled = true;
                    botCommand.handleMessage(this, channel, chat);
                }
            }
            if (!handled) {
                for (BotCommand botCommand : commandHandlers) {
                    botCommand.unhandledMessage(this, channel, chat);
                }
            }
        }
    }
}
