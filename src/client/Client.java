package client;

import client.routing.NodeUpdater;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Client {

    public static final String INETADDRESS= "224.1.1.1";
    public static final int PORT = 6789;

    private String nickname;
    protected HashMap<InetAddress, String> ipToNicknames = new HashMap<>();
    private List<InetAddress> neighbours = new ArrayList<>();
    private List<InetAddress> lastRoundNeighbours = new ArrayList<>();

    private MulticastSocket mcSocket;
    private ReentrantLock lock = new ReentrantLock();

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

        new Thread(new KeepAlive(mcSocket, this.nickname)).start();

        Sender sender = new Sender(mcSocket);

        try {
            new Thread(new Receiver(this, sender, mcSocket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new NodeUpdater(this)).start();
    }

    public void addNeighbour(InetAddress address, String nickname) {
        this.lastRoundNeighbours.add(address);
    }

    public void updateNeighbours() {
        lock.lock();
        neighbours = lastRoundNeighbours;
        lastRoundNeighbours.clear();
        lock.unlock();
    }

}