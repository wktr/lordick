package lordick.bot.commands;

import com.udojava.evalex.Expression;
import lordick.Lordick;
import lordick.bot.CommandListener;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class Calc implements CommandListener {
    @Override
    public String getHelp() {
        return "Usage; calc expression";
    }

    @Override
    public String getCommands() {
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
