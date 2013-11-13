package xxx.moparisthebest.irclib;

public class IrcChat {

    private String raw, prefix, type, destination, message;

    public IrcChat(String raw, String prefix, String type, String destination, String message) {
        this.raw = raw;
        this.prefix = prefix;
        this.type = type;
        this.destination = destination;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
