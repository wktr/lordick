package xxx.moparisthebest.irclib.properties;

public class UserProperties {

    private String nickname, ident, realname, password;
    private String[] homeChannels;

    public UserProperties(String nickname, String ident, String realname, String password, String... homeChannels) {
        this.nickname = nickname;
        this.ident = ident;
        this.realname = realname;
        this.password = password;
        this.homeChannels = homeChannels;
    }

    public String getIdent() {
        return ident;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRealname() {
        return realname;
    }

    public String[] getHomeChannels() {
        return homeChannels;
    }

    public String getPassword() {
        return password;
    }
}
