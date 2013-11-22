package xxx.moparisthebest.irclib;

public interface IrcMessageHandler {

    public boolean shouldHandle(IrcMessage message);

    public void handle(IrcMessage message);

}
