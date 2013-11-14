package lordick.bot.commands;

import com.udojava.evalex.Expression;
import io.netty.channel.Channel;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;

import java.math.BigDecimal;

public class Calc extends BotCommand {
    @Override
    public String getHelp() {
        return "Usage; calc expression";
    }

    @Override
    public String getCommand() {
        return "calc";
    }

    @Override
    public boolean shouldHandleCommand(IrcClient client, Channel channel, IrcChat chat) {
        return chat.isChannel() && chat.getMessage().matches(getCommand() + ":? .+");
    }

    @Override
    public void handleCommand(IrcClient client, Channel channel, IrcChat chat) {
        Expression expression = new Expression(chat.getMessage().substring(chat.getMessage().indexOf(' ') + 1));
        BigDecimal result;
        try {
            result = expression.eval();
            IrcClient.sendChat(channel, chat.getDestination(), "Calc result: %s", result.toPlainString());
        } catch (Exception e) {
            IrcClient.sendChat(channel, chat.getDestination(), "Calc error: %s", e.getMessage());
        }
    }
}
