package lordick.bot.commands;

import com.udojava.evalex.Expression;
import lordick.Lordick;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.messages.IrcMessage;

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
    public void handleCommand(Lordick client, String command, IrcMessage message) {
        try {
            Expression expression = new Expression(message.getMessage());
            BigDecimal result = expression.eval();
            message.sendChatf("Calc result: %s", result.toPlainString());
        } catch (Exception e) {
            message.sendChatf("Calc error: %s", e.getMessage());
        }
    }
}
