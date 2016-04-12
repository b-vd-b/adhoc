package datatype;

import java.net.InetAddress;
import java.security.PublicKey;
import java.util.HashMap;

public class BroadcastMessage extends Message {

    private String nickname;
    private HashMap<InetAddress,String> destinations;
    private HashMap<InetAddress, PublicKey> publicKey;
    private HashMap<InetAddress, InetAddress> nextHop;

    public BroadcastMessage(String nickname, HashMap<InetAddress,String> dest, HashMap<InetAddress, PublicKey> publicKey, HashMap<InetAddress, InetAddress> nextHop) {
        destinations = new HashMap<>();
        this.destinations = dest;
        this.nickname = nickname;
        this.publicKey = publicKey;
        this.nextHop = nextHop;
    }

    public String getNickname() {
        return nickname;
    }

    public HashMap<InetAddress, PublicKey> getPublicKeys() {
        return publicKey;
    }

    public HashMap<InetAddress,String> getDestinations() { return destinations; }

    public HashMap<InetAddress, InetAddress> getNextHop() {
        return nextHop;
    }
}
