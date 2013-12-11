package lordick.bot.commands;

import lordick.Lordick;
import lordick.bot.CommandListener;
import lordick.bot.InitListener;
import lordick.bot.MessageListener;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import java.io.File;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Karma implements CommandListener, MessageListener, InitListener {

    private Connection connection;

    @Override
    public boolean init(Lordick client) {
        try {
            connection = client.getDatabaseConnection();
            connection.createStatement().executeUpdate("create table if not exists karma (name TEXT UNIQUE, score INTEGER)");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        File f = new File("karma.db");
        if (f.exists()) {
            try {
                System.out.println("Importing old karma.db... ");
                connection.setAutoCommit(false);
                Connection c = DriverManager.getConnection("jdbc:sqlite:karma.db");
                ResultSet rs = c.createStatement().executeQuery("select name, score from karma");
                while (rs.next()) {
                    PreparedStatement ps = connection.prepareStatement("insert or replace into karma (name, score) values (?, ?)");
                    ps.setString(1, rs.getString(1));
                    ps.setInt(2, rs.getInt(2));
                    ps.executeUpdate();
                }
                c.close();
                if (!f.delete()) {
                    f.deleteOnExit();
                }
            } catch (Exception e) {
                System.out.println("Error importing old karma.db");
                e.printStackTrace();
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (Exception ignored) {
                }
            }
        }
        return true;
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
            message.sendChatf("pls give something for %s", command);
            return;
        }
        try {
            PreparedStatement ps = connection.prepareStatement("select score from karma where name == ?");
            ps.setString(1, nick);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int score = rs.getInt(1);
                message.sendChatf("%s for %s: %d", command, nick, score);
            } else {
                message.sendChatf("no %s for %s", command, nick);
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
    public String getCommands() {
        return "karma,rep";
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
