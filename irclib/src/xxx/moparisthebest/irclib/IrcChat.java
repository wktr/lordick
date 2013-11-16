package xxx.moparisthebest.irclib;

public class IrcChat {

    private String raw, prefix, type, destination, destParams, message;

    public IrcChat(String raw, String prefix, String type, String destination, String message) {
        this.raw = raw;
        this.prefix = prefix;
        this.type = type;
        if (destination != null) {
            String[] dummy = destination.split(" ", 2);
            this.destination = dummy[0];
            this.destParams = (dummy.length == 2 ? dummy[1] : null);
        } else {
            this.destination = this.destParams = null;
        }
        this.message = message;
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

    public boolean isChannel() {
        return getType().equalsIgnoreCase("PRIVMSG") && getDestination().startsWith("#");
    }
}
