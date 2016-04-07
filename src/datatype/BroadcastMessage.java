package datatype;

import java.net.InetAddress;
import java.util.HashMap;

public class BroadcastMessage extends Message {

    private String nickname;
    private HashMap<InetAddress,String> destinations;

    public BroadcastMessage(String nickname, HashMap<InetAddress,String> dest) {
        destinations = new HashMap<InetAddress,String>();
        this.destinations = dest;
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public HashMap<InetAddress,String> getDestinations() { return destinations; }
}
