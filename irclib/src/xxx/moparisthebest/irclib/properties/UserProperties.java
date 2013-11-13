package xxx.moparisthebest.irclib.properties;

public class UserProperties {

    private String nickname, altnick, ident, realname, password, host, hostmask;
    private String[] homeChannels;

    public UserProperties(String nickname, String altnick, String ident, String realname, String password, String... homeChannels) {
        this.nickname = nickname;
        this.altnick = altnick;
        this.ident = ident;
        this.realname = realname;
        this.password = password;
        this.homeChannels = homeChannels;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAltnick() {
        return altnick;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getPassword() {
        return password;
    }

    public String[] getHomeChannels() {
        return homeChannels;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
