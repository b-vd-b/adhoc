package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Client {

    private static final String INETADDRESS= "224.1.1.1";
    private static final int PORT = 6789;

    private String nickname;
    protected HashMap<InetAddress, String> destinations = new HashMap<>();
    protected List<InetAddress> neighbours = new ArrayList<>();
    private Thread keepAlive;
    private Thread receiver;
    private Sender sender;
    private MulticastSocket mcSocket;

    public static void main(String[] args) {
        Client client = new Client("hoi");
    }

    public Client(String nickname) {
        this.nickname = nickname;
        InetAddress group = null;
        try {
            group = InetAddress.getByName(INETADDRESS);
            mcSocket = new MulticastSocket(PORT);
            mcSocket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }

        keepAlive = new Thread(new KeepAlive(mcSocket, this.nickname));
        keepAlive.start();

        sender = new Sender(mcSocket);

        try {
            receiver = new Thread(new Receiver(sender, mcSocket
            ));
            receiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}