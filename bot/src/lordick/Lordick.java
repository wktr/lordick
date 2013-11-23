package lordick;

import lordick.bot.BotCommand;
import lordick.bot.commands.Karma;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.messages.IrcMessage;
import xxx.moparisthebest.irclib.net.IrcServer;
import xxx.moparisthebest.irclib.properties.NetworkProperties;
import xxx.moparisthebest.irclib.properties.UserProperties;
import xxx.moparisthebest.util.ClassEnumerator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
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
        UserProperties up = new UserProperties("lordick", "lordick", "lordick", "lordick", null, "#lordick");
        NetworkProperties np = new NetworkProperties("irc.moparisthebest.xxx", 6667, false);
        connect(up, np);
    }

    @Override
    public void onDisconnect(IrcServer server) {
        super.onDisconnect(server);
        final UserProperties up = server.getUserProperties();
        final NetworkProperties np = server.getNetworkProperties();
        server.getIrcClient().getGroup().schedule(new Runnable() {
            @Override
            public void run() {
                connect(up, np);
            }
        }, 10, TimeUnit.SECONDS);

    }

    private static Pattern help = Pattern.compile("help(?:[:]? (\\S+))?");

    @Override
    public void onMessage(IrcMessage message) {
        super.onMessage(message);
        if (!message.isDestChannel()) {
            return;
        }
        UserProperties up = message.getServer().getUserProperties();
        if (message.getMessage().matches("^" + up.getNickname() + ".? .+")) {
            String text = message.getMessage().substring(message.getMessage().indexOf(' ') + 1);
            Matcher m = help.matcher(text);
            if (m.find()) {
                String command = m.group(1);
                if (command == null) {
                    message.sendChatf("Help available for: %s", commandListString);
                } else if (!commandList.containsKey(command)) {
                    message.sendChatf("No help for command: %s", command);
                } else {
                    message.sendChatf("Help for %s: %s", command, commandList.get(command).getHelp());
                }
            } else {
                message.setMessage(text);
                boolean handled = false;
                for (BotCommand botCommand : commandHandlers) {
                    if (botCommand.shouldHandleCommand(this, message)) {
                        handled = true;
                        botCommand.handleCommand(this, message);
                    }
                }
                if (!handled) {
                    for (BotCommand botCommand : commandHandlers) {
                        botCommand.unhandledCommand(this, message);
                    }
                }
            }
        } else {
            boolean handled = false;
            for (BotCommand botCommand : commandHandlers) {
                if (botCommand.shouldHandleMessage(this, message)) {
                    handled = true;
                    botCommand.handleMessage(this, message);
                }
            }
            if (!handled) {
                for (BotCommand botCommand : commandHandlers) {
                    botCommand.unhandledMessage(this, message);
                }
            }
        }
    }
}
