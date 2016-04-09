package datatype;

import java.net.InetAddress;
import java.security.PublicKey;
import java.util.HashMap;

public class BroadcastMessage extends Message {

    private String nickname;
    private HashMap<InetAddress,String> destinations;
    private PublicKey publicKey;

    public BroadcastMessage(String nickname, HashMap<InetAddress,String> dest, PublicKey publicKey) {
        destinations = new HashMap<>();
        this.destinations = dest;
        this.nickname = nickname;
        this.publicKey = publicKey;
    }

    public String getNickname() {
        return nickname;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public HashMap<InetAddress,String> getDestinations() { return destinations; }
}
