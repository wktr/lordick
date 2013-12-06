package lordick.bot.commands;

import lordick.Lordick;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Karma extends BotCommand {

    private Connection connection;

    public Karma() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:karma.db");
            //connection.setAutoCommit(false);
            connection.createStatement().executeUpdate("create table if not exists karma (name TEXT UNIQUE, score INTEGER)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (connection != null) {
                connection.commit();
                connection.close();
            }
        } finally {
            super.finalize();
        }
    }

    @Override
    public void handleCommand(Lordick client, String command, IrcMessage message) {
        if (!message.hasMessage()) {
            message.sendChatf(getHelp());
            return;
        }
        String nick = message.getMessage().toLowerCase();
        if (nick.matches("for:? .*")) {
            nick = nick.substring(nick.indexOf(" ") + 1);
        }
        if (nick.isEmpty()) {
            message.sendChatf("pls give something for rep");
            return;
        }
        try {
            PreparedStatement ps = connection.prepareStatement("select score from karma where name == ?");
            ps.setString(1, nick);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int score = rs.getInt(1);
                message.sendChatf("karma for %s: %d", nick, score);
            } else {
                message.sendChatf("no karma for %s", nick);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getHelp() {
        return "shows karma score for name, or name++ increases karma score for name";
    }

    @Override
    public String[] getCommandList() {
        return new String[]{"karma", "rep"};
    }

    private static Pattern karma = Pattern.compile("(?:(\\S+)\\s?\\+\\++)");

    @Override
    public void onMessage(Lordick client, IrcMessage message) {
        if (message.isDestChannel() && message.getMessage().contains("++")) {
            Matcher m = karma.matcher(message.getMessage());
            while (m.find()) {
                try {
                    String nick = m.group(1).toLowerCase();
                    PreparedStatement ps = connection.prepareStatement("insert or replace into karma (name, score) values (?, ifnull((select score + 1 from karma where name = ?), 1))");
                    ps.setString(1, nick);
                    ps.setString(2, nick);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
