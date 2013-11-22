package lordick.bot.commands;

import com.udojava.evalex.Expression;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.IrcMessage;

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
    public boolean shouldHandleCommand(IrcClient client, IrcMessage message) {
        return message.isDestChannel() && message.getMessage().matches(getCommand() + ":? .+");
    }

    @Override
    public void handleCommand(IrcClient client, IrcMessage message) {
        Expression expression = new Expression(message.getMessage().substring(message.getMessage().indexOf(' ') + 1));
        BigDecimal result;
        try {
            result = expression.eval();
            message.sendChatf("Calc result: %s", result.toPlainString());
        } catch (Exception e) {
            message.sendChatf("Calc error: %s", e.getMessage());
        }
    }
}
