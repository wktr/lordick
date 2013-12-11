package lordick.bot.commands;

import lordick.Lordick;
import lordick.bot.CommandListener;
import lordick.bot.InitListener;
import lordick.bot.MessageListener;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import java.io.File;
import java.sql.*;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Markov implements CommandListener, MessageListener, InitListener {

    private Connection connection;
    private Random randy = new Random();
    private int replyrate = 1;
    private int replynick = 100;

    @Override
    public boolean init(Lordick client) {
        try {
            connection = client.getDatabaseConnection();
            connection.createStatement().executeUpdate("create table if not exists markov (seed_a TEXT, seed_b TEXT, seed_c TEXT, unique(seed_a, seed_b, seed_c) on conflict ignore)");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        File f = new File("markov.db");
        if (f.exists()) {
            try {
                System.out.println("Importing old markov.db... ");
                connection.setAutoCommit(false);
                Connection c = DriverManager.getConnection("jdbc:sqlite:markov.db");
                ResultSet rs = c.createStatement().executeQuery("select seed_a, seed_b, seed_c from markov");
                while (rs.next()) {
                    markov_insert(rs.getString(1), rs.getString(2), rs.getString(3));
                }
                c.close();
                if (!f.delete()) {
                    f.deleteOnExit();
                }
            } catch (Exception e) {
                System.out.println("Error importing old markov.db");
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

    private int s2i(String s, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i < min) {
                return min;
            } else if (i > max) {
                return max;
            } else {
                return i;
            }
        } catch (Exception ignored) {

        }
        return min;
    }

    private static Pattern command = Pattern.compile("(\\S+):?\\s*(\\S+)?(?: (\\S+))?", Pattern.CASE_INSENSITIVE);

    @Override
    public void handleCommand(Lordick client, String command, IrcMessage message) {
        if (!message.hasMessage()) {
            message.sendChatf(getHelp());
            return;
        }
        Matcher m = Markov.command.matcher(message.getMessage());
        if (m.find()) {
            String cmd = m.group(1);
            if (cmd.equalsIgnoreCase("about")) {
                if (m.group(2) == null || m.group(2).isEmpty()) {
                    message.sendChat("Need context");
                } else if (randy.nextFloat() * 100 <= replynick) {
                    String markov = markov_find(m.group(2), m.group(3));
                    if (markov == null) {
                        message.sendChat("I can't :(");
                    } else {
                        message.sendChat(markov);
                    }
                }
            } else if (message.getHostmask().getNick().equalsIgnoreCase("exemplar") && message.getHostmask().getHost().endsWith(".xxx")) {
                if (cmd.equalsIgnoreCase("replyrate")) {
                    if (m.group(2) == null) {
                        message.sendChatf("Reply rate is: %d%%", replyrate);
                    } else {
                        replyrate = s2i(m.group(2), 0, 100);
                        message.sendChatf("Reply rate set to: %d%%", replyrate);
                    }
                } else if (cmd.equalsIgnoreCase("replynick")) {
                    if (m.group(2) == null) {
                        message.sendChatf("Reply nick is: %d%%", replynick);
                    } else {
                        replynick = s2i(m.group(2), 0, 100);
                        message.sendChatf("Reply nick set to: %d%%", replynick);
                    }
                } else {
                    message.sendChatf("Unknown chat command: %s", cmd);
                }
            } else {
                message.sendChatf("Unknown chat command: %s", cmd);
            }
        }
    }

    @Override
    public String getHelp() {
        return "Markov usage: chat [about|replynick|replyrate] <context>";
    }

    @Override
    public String getCommands() {
        return "chat";
    }

    @Override
    public void onMessage(Lordick client, IrcMessage message) {
        if (message.isDestChannel()) {
            markov_learn(message.getMessage());
            if (randy.nextFloat() * 100 <= replyrate) {
                String markov = markov_generate();
                if (markov != null) {
                    message.sendChat(markov);
                }
            }
        }
    }

    private void markov_insert(String seed1, String seed2, String seed3) {
        try {
            PreparedStatement ps = connection.prepareStatement("insert into markov (seed_a, seed_b, seed_c) values (?, ?, ?)");
            ps.setString(1, seed1);
            ps.setString(2, seed2);
            ps.setString(3, seed3);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void markov_learn(String input) {
        String seed1, seed2;
        seed1 = seed2 = "\n";
        String[] words = input.split(" ");
        for (String seed3 : words) {
            markov_insert(seed1, seed2, seed3);
            seed1 = seed2;
            seed2 = seed3;
        }
    }

    private String markov_find(String seed1, String seed2) {
        try {
            PreparedStatement ps;
            if (seed2 == null) {
                ps = connection.prepareStatement("select seed_a, seed_b from markov where seed_a = ? or seed_b = ? COLLATE NOCASE order by random() limit 1");
                ps.setString(1, seed1);
                ps.setString(2, seed1);
            } else {
                ps = connection.prepareStatement("select seed_a, seed_b from markov where seed_a in (?, ?) or seed_b in (?, ?) COLLATE NOCASE order by random() limit 1");
                ps.setString(1, seed1);
                ps.setString(2, seed2);
                ps.setString(3, seed1);
                ps.setString(4, seed2);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String found1 = rs.getString(1);
                String found2 = rs.getString(2);
                return markov_generate(found1, found2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String markov_generate() {
        try {
            PreparedStatement ps = connection.prepareStatement("select seed_a, seed_b from markov order by random() limit 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String found1 = rs.getString(1);
                String found2 = rs.getString(2);
                return markov_generate(found1, found2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int markov_fill_backwards(StringBuilder result, int wordcount, String seed1, String seed2) {
        int count = 0;
        for (int i = 0; i < wordcount; i++) {
            String seed0 = markov_previousseed(seed1, seed2);
            if (seed0 == null || seed0.equalsIgnoreCase("\n")) {
                break;
            }
            count++;
            if (result.length() > 0) {
                result.insert(0, ' ');
            }
            result.insert(0, seed0);
            seed2 = seed1;
            seed1 = seed0;
        }
        return count;
    }

    private int markov_fill_forwards(StringBuilder result, int wordcount, String seed1, String seed2) {
        int count = 0;
        for (int i = 0; i < wordcount / 2; i++) {
            String seed3 = markov_nextseed(seed1, seed2);
            if (seed3 == null || seed3.equalsIgnoreCase("\n")) {
                break;
            }
            count++;
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(seed3);
            seed1 = seed2;
            seed2 = seed3;
        }
        return count;
    }

    private String markov_generate(String seed1, String seed2) {
        //System.out.printf("Start seeds: %s - %s\n", seed1.replace("\n", "\\n"), seed2.replace("\n", "\\n"));
        StringBuilder result = new StringBuilder();
        if (seed1 == null) {
            seed1 = "\n";
        }
        if (seed2 == null) {
            seed2 = "\n";
        }
        if (!seed1.equalsIgnoreCase("\n")) {
            result.append(seed1);
            result.append(' ');
        }
        if (!seed2.equalsIgnoreCase("\n")) {
            result.append(seed2);
        }
        int wordcount = randy.nextInt(30) + 10;
        int type = randy.nextInt(3);
        switch (type) {
            case 0:
                markov_fill_backwards(result, wordcount, seed1, seed2);
                break;
            case 1:
                markov_fill_forwards(result, wordcount, seed1, seed2);
                break;
            default:
                int num = markov_fill_backwards(result, wordcount / 2, seed1, seed2);
                markov_fill_forwards(result, wordcount - num, seed1, seed2);
                break;
        }
        if (result.length() > 0) {
            return result.toString().trim();
        } else {
            return null;
        }
    }

    private String markov_nextseed(String seed1, String seed2) {
        try {
            PreparedStatement ps = connection.prepareStatement("select seed_c from markov where seed_a = ? and seed_b = ? order by random() limit 1");
            ps.setString(1, seed1);
            ps.setString(2, seed2);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String markov_previousseed(String seed2, String seed3) {
        try {
            PreparedStatement ps = connection.prepareStatement("select seed_a from markov where seed_b = ? and seed_c = ? order by random() limit 1");
            ps.setString(1, seed2);
            ps.setString(2, seed3);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
