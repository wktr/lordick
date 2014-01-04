package xxx.moparisthebest.irclib.messages;

import xxx.moparisthebest.irclib.net.IrcServer;

public class IrcMessage {

    private final String raw, source, command, target, targetParams;
    private String message;
    private final IrcServer server;
    private IrcHostmask hostmask;
    private boolean spam = false;

    public IrcMessage(String raw, String source, String command, String target, String message, IrcServer server) {
        this.raw = raw;
        this.source = source;
        hostmask = new IrcHostmask(source);
        this.command = command;
        if (target != null) {
            String[] dummy = target.split(" ", 2);
            this.target = dummy[0];
            this.targetParams = (dummy.length == 2 ? dummy[1] : null);
        } else {
            this.target = this.targetParams = null;
        }
        if (message == null) {
            this.message = "";
        } else {
            this.message = message.trim();
        }
        this.server = server;
    }

    public String getRaw() {
        return raw;
    }

    public String getSource() {
        return source;
    }

    public String getCommand() {
        return command;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetParams() {
        return targetParams;
    }

    public String getMessage() {
        return message;
    }

    public boolean hasMessage() {
        return message != null && !message.isEmpty();
    }

    public IrcServer getServer() {
        return server;
    }

    public IrcHostmask getHostmask() {
        return hostmask;
    }

    public boolean isDestChannel() {
        return getCommand().equalsIgnoreCase("PRIVMSG") && getTarget().startsWith("#");
    }

    public boolean isDestMe() {
        return getCommand().equalsIgnoreCase("PRIVMSG") && getTarget().equals(server.getUserProperties().getNickname());
    }

    private String getSendDest() {
        if (isDestChannel()) {
            return getTarget();
        } else {
            return getHostmask().getNick();
        }
    }

    public void sendChatf(String message, Object... format) {
        sendChat(String.format(message, format));
    }

    public void sendChat(String message) {
        server.getChannel().writeAndFlush("PRIVMSG " + getSendDest() + " :" + message);
    }

    public boolean isSpam() {
        return spam;
    }

    public void setSpam(boolean spam) {
        this.spam = spam;
    }
}
