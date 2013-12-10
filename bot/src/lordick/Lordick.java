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

    public List<BotCommand> commandHandlers = new CopyOnWriteArrayList<BotCommand>();
    public Map<String, BotCommand> commandList = new ConcurrentHashMap<String, BotCommand>();

    public Map<String, String> propMap = new ConcurrentHashMap<String, String>(); // todo: stuff for this, ie authing

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadCommandHandlers() {
        commandHandlers.clear();
        for (Class c : ClassEnumerator.getClassesForPackage(Karma.class.getPackage())) {
            try {
                BotCommand command = (BotCommand) c.newInstance();
                commandHandlers.add(command);
                boolean hasCommand = false;
                if (command.getCommandList() != null) {
                    for (String s : command.getCommandList()) {
                        commandList.put(s, command);
                        hasCommand = true;
                    }
                }
                if (command.getCommand() != null) {
                    commandList.put(command.getCommand(), command);
                    hasCommand = true;
                }
                if (!hasCommand) {
                    System.out.println("WARNING: BotCommand has no commands - " + command);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start(String... homechans) {
        loadCommandHandlers();
        UserProperties up = new UserProperties("lordick", "lordick", "lordick", "lordick", null, homechans);
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

    private static Pattern command = Pattern.compile("(\\S+?)(?:[,:]? (.+))?");

    @Override
    public void onMessage(IrcMessage message) {
        super.onMessage(message);
        if (!message.hasMessage()) {
            return;
        }
        UserProperties up = message.getServer().getUserProperties();
        if (message.getMessage().matches("^" + up.getNickname() + "[:,]? .+") || message.isDestMe()) {
            String text = message.getMessage().substring(message.getMessage().indexOf(' ') + 1);
            Matcher m = command.matcher(text);
            if (m.matches()) {
                String command = m.group(1);
                message.setMessage(m.group(2));
                if (commandList.containsKey(command)) {
                    try {
                        commandList.get(command).handleCommand(this, command, message);
                    } catch (Exception ex) {
                        message.sendChatf("Exception while handling command %s, %s", command, ex.getMessage());
                        ex.printStackTrace();
                    }
                    return;
                } else {
                    for (BotCommand botCommand : commandHandlers) {
                        botCommand.unhandledCommand(this, command, message);
                    }
                }
            }
        }
        for (BotCommand botCommand : commandHandlers) {
            botCommand.onMessage(this, message);
        }
    }
}