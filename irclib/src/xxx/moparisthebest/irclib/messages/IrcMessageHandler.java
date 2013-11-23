package xxx.moparisthebest.irclib.messages;

public interface IrcMessageHandler {

    public boolean shouldHandle(IrcMessage message);

    public void handle(IrcMessage message);

}
