/**
 * Created by Andromeda on 11/4/16.
 */

public class User {

    String hostname;
    String connSpeed;

    public User(String username, String hostname, String connSpeed) {
        this.username = username;
        this.hostname = hostname;
        this.connSpeed = connSpeed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    String username;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getConnSpeed() {
        return connSpeed;
    }

    public void setConnSpeed(String connSpeed) {
        this.connSpeed = connSpeed;
    }
}
