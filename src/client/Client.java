package client;

import client.routing.NodeUpdater;
import datatype.BroadcastMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Client {

    public static final String INETADDRESS = "228.1.1.1";
    public static final int PORT = 6789;

    private String nickname;
    private HashMap<InetAddress, String> destinations = new HashMap<>();
    private HashMap<InetAddress, InetAddress> nextHop = new HashMap<>();
    private HashMap<InetAddress, String> neighbours = new HashMap<>();
    private HashMap<InetAddress, String> lastRoundNeighbours = new HashMap<>();

    private MulticastSocket mcSocket;
    private ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        Client client = new Client("hoi");
    }

    public HashMap<InetAddress, String> getDestinations() {
        return destinations;
    }

    public Client(String nickname) {
        this.nickname = nickname;
        InetAddress group;
        try {
            group = InetAddress.getByName(INETADDRESS);
            mcSocket = new MulticastSocket(PORT);
            mcSocket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new KeepAlive(mcSocket, this.nickname, this)).start();

        Sender sender = new Sender(mcSocket);

        try {
            new Thread(new Receiver(this, sender, mcSocket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new NodeUpdater(this)).start();
    }

    public void addNeighbour(InetAddress address, BroadcastMessage message) throws UnknownHostException {
        this.lastRoundNeighbours.put(address, message.getNickname());
        for (InetAddress e : message.getDestinations().keySet()) {
            if (!destinations.containsKey(e)) {
                if (!e.equals(InetAddress.getLocalHost())) {
                    destinations.put(address, message.getNickname());
                    nextHop.put(e, address);
                }
            }
        }
        for (InetAddress e : nextHop.keySet()) {
            if (!message.getDestinations().containsKey(e)) {
                if (nextHop.get(e).equals(address)) {
                    destinations.remove(e);
                    nextHop.remove(e);
                }
            }
        }
    }

    public void updateNeighbours() {
        lock.lock();
        neighbours.clear();
        neighbours.putAll(lastRoundNeighbours);
        lastRoundNeighbours.clear();
        for (InetAddress e : neighbours.keySet()) {
            if (!destinations.containsKey(e)) {
                destinations.put(e, neighbours.get(e));
                nextHop.put(e, e);
            }
        }
        for (InetAddress e : destinations.keySet()) {
            if (!neighbours.containsKey(e)) {
                System.out.println(nextHop.get(e) == e);
                if (nextHop.get(e).equals(e)) {
                    destinations.remove(e);
                    nextHop.remove(e);
                }
            }
        }
        lock.unlock();
    }

}