package xxx.moparisthebest.irclib.properties;

public class NetworkProperties {

    private String host;
    private int port;
    private boolean ssl;

    public NetworkProperties(String host, int port, boolean ssl) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isSSL() {
        return ssl;
    }
}
