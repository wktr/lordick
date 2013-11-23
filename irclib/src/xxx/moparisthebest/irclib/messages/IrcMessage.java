package xxx.moparisthebest.irclib.messages;

import io.netty.channel.Channel;

public class IrcMessage {

    private final String raw, prefix, type, destination, destParams;
    private String message;
    private final Channel channel;
    private IrcHostmask hostmask;

    public IrcMessage(String raw, String prefix, String type, String destination, String message, Channel channel) {
        this.raw = raw;
        this.prefix = prefix;
        hostmask = new IrcHostmask(prefix);
        this.type = type;
        if (destination != null) {
            String[] dummy = destination.split(" ", 2);
            this.destination = dummy[0];
            this.destParams = (dummy.length == 2 ? dummy[1] : null);
        } else {
            this.destination = this.destParams = null;
        }
        this.message = message;
        this.channel = channel;
    }

    public String getRaw() {
        return raw;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getType() {
        return type;
    }

    public String getDestination() {
        return destination;
    }

    public String getDestParams() {
        return destParams;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDestChannel() {
        return getType().equalsIgnoreCase("PRIVMSG") && getDestination().startsWith("#");
    }

    public Channel getChannel() {
        return channel;
    }

    public IrcHostmask getHostmask() {
        return hostmask;
    }

    public void sendChatf(String message, Object... format) {
        sendChat(String.format(message, format));
    }

    public void sendChat(String message) {
        channel.writeAndFlush("PRIVMSG " + destination + " :" + message);
    }
}
