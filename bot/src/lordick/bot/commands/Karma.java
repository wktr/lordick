package lordick.bot.commands;

import io.netty.channel.Channel;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Karma extends BotCommand {

    private Connection connection;

    public Karma() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:karma.db");
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table if not exists karma(name TEXT UNIQUE, score INTEGER);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if(connection != null) {
                connection.commit();
                connection.close();
            }
        } finally {
            super.finalize();
        }
    }

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
        Matcher m = command.matcher(chat.getMessage());
        if (m.matches()) {
            String nick = m.group(1);
            try {
                PreparedStatement ps = connection.prepareStatement("select score from karma where name == ?");
                ps.setString(1, nick);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int score = rs.getInt(1);
                    IrcClient.sendChat(channel, chat.getDestination(), "karma for " + nick + ": " + score);
                } else {
                    IrcClient.sendChat(channel, chat.getDestination(), "no karma for " + nick);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
        return chat.getType().equalsIgnoreCase("PRIVMSG") && chat.getMessage().contains("++");
    }

    @Override
    public void handleMessage(IrcClient client, Channel channel, IrcChat chat) {
        Matcher m = karma.matcher(chat.getMessage());
        while (m.find()) {
            try {
                String nick = m.group(1);
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
