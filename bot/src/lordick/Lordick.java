package lordick;

import lordick.bot.CommandListener;
import lordick.bot.InitListener;
import lordick.bot.MessageListener;
import lordick.bot.UnhandledCommandListener;
import lordick.bot.commands.Help;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.messages.IrcMessage;
import xxx.moparisthebest.irclib.net.IrcServer;
import xxx.moparisthebest.irclib.properties.NetworkProperties;
import xxx.moparisthebest.irclib.properties.UserProperties;
import xxx.moparisthebest.util.ClassEnumerator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lordick extends IrcClient {

    // region event listeners

    private List<MessageListener> messageListeners = new CopyOnWriteArrayList<MessageListener>();
    private List<UnhandledCommandListener> unhandledCommandListeners = new CopyOnWriteArrayList<UnhandledCommandListener>();
    private Map<String, CommandListener> commandListeners = new ConcurrentHashMap<String, CommandListener>();

    public List<MessageListener> getMessageListeners() {
        return messageListeners;
    }

    public List<UnhandledCommandListener> getUnhandledCommandListeners() {
        return unhandledCommandListeners;
    }

    public Map<String, CommandListener> getCommandListeners() {
        return commandListeners;
    }

    private void loadListeners() {
        commandListeners.clear();
        unhandledCommandListeners.clear();
        messageListeners.clear();
        for (Class c : ClassEnumerator.getClassesForPackage(Help.class.getPackage())) {
            try {
                Object o = c.newInstance();
                if (InitListener.class.isAssignableFrom(c)) {
                    boolean b = ((InitListener) o).init(this);
                    if (!b) {
                        System.out.println("Not loading listener: " + o);
                        continue;
                    }
                }
                if (CommandListener.class.isAssignableFrom(c)) {
                    CommandListener command = (CommandListener) o;
                    for (String s : command.getCommands().split(",")) {
                        commandListeners.put(s, command);
                    }
                }
                if (UnhandledCommandListener.class.isAssignableFrom(c)) {
                    UnhandledCommandListener unhandled = (UnhandledCommandListener) o;
                    unhandledCommandListeners.add(unhandled);
                }
                if (MessageListener.class.isAssignableFrom(c)) {
                    MessageListener message = (MessageListener) o;
                    messageListeners.add(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // endregion

    // region irc overrides

    @Override
    public void onDisconnect(IrcServer server) {
        super.onDisconnect(server);
        final UserProperties up = server.getUserProperties();
        final NetworkProperties np = server.getNetworkProperties();
        server.getIrcClient().getGroup().schedule(new Runnable() {
            @Override
            public void run() {
                connect(up, np);
            }
        }, 10, TimeUnit.SECONDS);
    }

    private static Pattern command = Pattern.compile("(\\S+?)(?:[,:]? (.+))?");

    @Override
    public void onMessage(IrcMessage message) {
        super.onMessage(message);
        if (!message.hasMessage()) {
            return;
        }
        UserProperties up = message.getServer().getUserProperties();
        if ((message.isDestChannel() && message.getMessage().matches("^" + up.getNickname() + "[:,]? .+")) || message.isDestMe()) {
            String text = message.getMessage().substring(message.getMessage().indexOf(' ') + 1);
            Matcher m = command.matcher(text);
            if (m.matches()) {
                // simple spam filtering
                if (message.isDestChannel() || message.isDestMe()) {
                    if (System.currentTimeMillis() - getKeyValueLong(message.getServer(), "spam." + message.getHostmask().getNick()) < 5000) { // todo: set this 5000 somewhere ?
                        message.setSpam(true);
                    }
                    setKeyValue(message.getServer(), "spam." + message.getHostmask().getNick(), System.currentTimeMillis());
                }
                String command = m.group(1);
                IrcMessage newMessage = new IrcMessage(message.getRaw(), message.getSource(), message.getCommand(), message.getTarget(), m.group(2), message.getServer());
                newMessage.setSpam(message.isSpam());
                if (commandListeners.containsKey(command)) {
                    try {
                        commandListeners.get(command).handleCommand(this, command, newMessage);
                    } catch (Exception ex) {
                        message.sendChatf("Exception while handling command %s, %s", command, ex.getMessage());
                        ex.printStackTrace();
                    }
                    return;
                } else {
                    for (UnhandledCommandListener listener : unhandledCommandListeners) {
                        listener.unhandledCommand(this, command, newMessage);
                    }
                }
            }
        }
        for (MessageListener listener : messageListeners) {
            listener.onMessage(this, message);
        }
    }

    // endregion

    // region database

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private Connection databaseConnection;

    public Connection getDatabaseConnection() {
        return databaseConnection;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (databaseConnection != null) {
                databaseConnection.commit();
                databaseConnection.close();
            }
        } finally {
            super.finalize();
        }
    }

    private void connectDatabase() {
        try {
            databaseConnection = DriverManager.getConnection("jdbc:sqlite:lordick.db");
            databaseConnection.createStatement().executeUpdate("create table if not exists keyvalues (server TEXT, key TEXT, value TEXT, unique(server, key) on conflict replace)");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public long getKeyValueLong(IrcServer server, String key) {
        try {
            return Long.valueOf(getKeyValue(server, key));
        } catch (Exception e) {
            return -1;
        }
    }

    public String getKeyValue(IrcServer server, String key) {
        try {
            PreparedStatement ps = databaseConnection.prepareStatement("select value from keyvalues where server = ? and key = ?");
            ps.setString(1, server.getNetworkProperties().getHost());
            ps.setString(2, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setKeyValue(IrcServer server, String key, Object value) {
        try {
            PreparedStatement ps = databaseConnection.prepareStatement("insert into keyvalues (server, key, value) values (?, ?, ?)");
            ps.setString(1, server.getNetworkProperties().getHost());
            ps.setString(2, key);
            ps.setString(3, value.toString());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // endregion

    public void start(String... homechans) {
        connectDatabase();
        loadListeners();
        UserProperties up = new UserProperties("lordick", "lordick", "lordick", "lordick", null, homechans);
        NetworkProperties np = new NetworkProperties("irc.moparisthebest.xxx", 6667, false);
        connect(up, np);
    }

}