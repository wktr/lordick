package lordick.bot.commands;

import io.netty.channel.Channel;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Karma extends BotCommand {

    private static Pattern command = Pattern.compile("(?:karma|rep)(?: for)?:?(?: (\\S+))");
    private static Pattern karma = Pattern.compile("(?:([^ ]+)[ ]?\\+\\++)");
    private String[] commandList = {"karma", "rep"};
    private String help = command.pattern() + " - shows karma score for name, or name++ increases karma score for name";

    @Override
    public boolean shouldHandleCommand(IrcClient client, Channel channel, IrcChat chat) {
        return chat.getMessage().matches(command.pattern());
    }

    @Override
    public void handleCommand(IrcClient client, Channel channel, IrcChat chat) {
        // todo: this
    }

    @Override
    public String getHelp() {
        return help;
    }

    @Override
    public String[] getCommandList() {
        return commandList;
    }

    @Override
    public boolean shouldHandleMessage(IrcClient client, Channel channel, IrcChat chat) {
        return chat.getMessage().contains("++");
    }

    @Override
    public void handleMessage(IrcClient client, Channel channel, IrcChat chat) {
        Matcher m = karma.matcher(chat.getMessage());
        while (m.matches()) {
            // todo: this
        }
    }
}
