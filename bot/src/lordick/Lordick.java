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

    public void loadCommandHandlers() {
        commandHandlers.clear();
        for (Class c : ClassEnumerator.getClassesForPackage(Karma.class.getPackage())) {
            try {
                BotCommand command = (BotCommand) c.newInstance();
                commandHandlers.add(command);
                for (String s : command.getCommandList()) {
                    commandList.put(s, command);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        commandListString = null;
        for (String s : commandList.keySet()) {
            if (commandListString == null) {
                commandListString = s;
            } else {
                commandListString += "," + s;
            }
        }
    }

    public void start() {
        loadCommandHandlers();
        UserProperties up = new UserProperties("lordick", "lordick", "lordick", null, "#mopar");
        NetworkProperties np = new NetworkProperties("irc.moparisthebest.xxx", 6667, false);
        connect(up, np);
    }

    Pattern help = Pattern.compile("help(?:[:]? (?<command>\\S+))?");

    @Override
    public void OnIrcMessage(Channel channel, IrcChat chat) {
        UserProperties up = IrcClient.getUserProperties(channel);
        if (chat.getDestination().startsWith("#") && chat.getMessage().matches("^" + up.getNickname() + ":? ")) {
            String text = chat.getMessage().substring(chat.getMessage().indexOf(' ') + 1);
            Matcher m = help.matcher(text);
            if (m.matches()) {
                String command = m.group("command");
                if (command == null) {
                    channel.write("Help available for: " + commandList);
                } else if (!commandList.containsKey(command)) {
                    channel.write("No help for command: " + command);
                } else {
                    channel.write(command + ": " + commandList.get(command).getHelp());
                }
            } else {
                chat.setMessage(text);
                for (BotCommand botCommand : commandHandlers) {
                    if (botCommand.shouldHandleCommand(this, channel, chat)) {
                        botCommand.handleCommand(this, channel, chat);
                    }
                }
            }
        } else {
            for (BotCommand botCommand : commandHandlers) {
                if (botCommand.shouldHandleMessage(this, channel, chat)) {
                    botCommand.handleMessage(this, channel, chat);
                }
            }
        }
    }
}
