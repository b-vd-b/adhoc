package datatype;

public class BroadcastMessage extends Message {

    private String nickname;

    public BroadcastMessage(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
