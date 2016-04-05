package datatype;

public class BroadcastMessage extends Message {

    private String ip;
    private String nickname;

    public BroadcastMessage(String ip, String nickname) {
        this.ip = ip;
        this.nickname = nickname;
    }

    public String getIp() {
        return ip;
    }

    public String getNickname() {
        return nickname;
    }
}
