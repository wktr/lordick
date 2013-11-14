package lordick.bot.commands;

import io.netty.channel.Channel;
import lordick.bot.BotCommand;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;

import java.sql.*;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Markov extends BotCommand {

    private Connection connection;
    private Random randy = new Random();
    private int replyrate = 2;
    private int replynick = 100;

    public Markov() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:markov.db");
            //connection.setAutoCommit(false);
            connection.createStatement().executeUpdate("create table if not exists markov (seed_a TEXT, seed_b TEXT, seed_c TEXT, unique(seed_a, seed_b, seed_c) on conflict ignore)");
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

    private static Pattern command = Pattern.compile("chat\\s(\\S+):?\\s*(\\S+)?(?: (\\S+))?", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean shouldHandleCommand(IrcClient client, Channel channel, IrcChat chat) {
        return chat.getMessage().matches(command.pattern());
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

    @Override
    public void handleCommand(IrcClient client, Channel channel, IrcChat chat) {
        Matcher m = command.matcher(chat.getMessage());
        if (m.matches()) {
            String cmd = m.group(1);
            if (cmd.equalsIgnoreCase("about")) {
                if (m.group(2) == null) {
                    IrcClient.sendChat(channel, chat.getDestination(), "Need context");
                } else if (randy.nextFloat() * 100 <= replynick) {
                    String markov = markov_find(m.group(2), m.group(3));
                    if (markov == null) {
                        IrcClient.sendChat(channel, chat.getDestination(), "I can't :(");
                    } else {
                        IrcClient.sendChat(channel, chat.getDestination(), markov);
                    }
                }
            } else if (cmd.equalsIgnoreCase("replyrate")) {
                if (m.group(2) == null) {
                    IrcClient.sendChat(channel, chat.getDestination(), "Reply rate is: %d%%", replyrate);
                } else {
                    replyrate = s2i(m.group(2), 0, 100);
                    IrcClient.sendChat(channel, chat.getDestination(), "Reply rate set to: %d%%", replyrate);
                }
            } else if (cmd.equalsIgnoreCase("replynick")) {
                if (m.group(2) == null) {
                    IrcClient.sendChat(channel, chat.getDestination(), "Reply nick is: %d%%", replynick);
                } else {
                    replynick = s2i(m.group(2), 0, 100);
                    IrcClient.sendChat(channel, chat.getDestination(), "Reply nick set to: %d%%", replynick);
                }
            } else {
                IrcClient.sendChat(channel, chat.getDestination(), "Unknown command: %d%%", cmd);
            }
        }
    }

    @Override
    public String getHelp() {
        return "Usage: chat [about|replynick|replyrate] <context>";
    }

    @Override
    public String getCommand() {
        return "chat";
    }

    @Override
    public void unhandledMessage(IrcClient client, Channel channel, IrcChat chat) {
        if (chat.isChannel()) {
            markov_learn(chat.getMessage());
            if (randy.nextFloat() * 100 <= replyrate) {
                String markov = markov_generate();
                if (markov != null) {
                    IrcClient.sendChat(channel, chat.getDestination(), markov);
                }
            }
        }
    }

    private void markov_learn(String input) {
        String seed1, seed2;
        seed1 = seed2 = "\n";
        String[] words = input.split(" ");
        for (String seed3 : words) {
            try {
                PreparedStatement ps = connection.prepareStatement("insert into markov (seed_a, seed_b, seed_c) values (?, ?, ?)");
                ps.setString(1, seed1);
                ps.setString(2, seed2);
                ps.setString(3, seed3);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            seed1 = seed2;
            seed2 = seed3;
        }
    }

    private String markov_find(String seed1, String seed2) {
        try {
            PreparedStatement ps;
            ps = connection.prepareStatement("select seed_a, seed_b from markov where seed_a = ? or seed_b = ? order by random() limit 1");
            ps.setString(1, seed1);
            ps.setString(2, (seed2 == null ? seed1 : seed2));
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
                String markov = markov_generate(found1, found2);
                if (markov != null) {
                    if (!found2.equalsIgnoreCase("\n")) {
                        markov = found2 + " " + markov;
                    }
                    if (!found1.equalsIgnoreCase("\n")) {
                        markov = found1 + " " + markov;
                    }
                    return markov;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String markov_generate(String seed1, String seed2) {
        //System.out.printf("Start seeds: %s - %s\n", seed1.replace("\n", "\\n"), seed2.replace("\n", "\\n"));
        int wordcount = randy.nextInt(20) + 10;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < wordcount; i++) {
            String seed3 = markov_nextseed(seed1, seed2);
            if (seed3 == null || seed3.equalsIgnoreCase("\n")) {
                break;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(seed3);
            seed1 = seed2;
            seed2 = seed3;
            //System.out.printf("Seeds: %s - %s\n", seed1.replace("\n", "\\n"), seed2.replace("\n", "\\n"));
        }
        if (result.length() > 0) {
            return result.toString();
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

}
