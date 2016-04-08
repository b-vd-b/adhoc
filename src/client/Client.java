package client;

import client.gui.ClientGUI;
import client.gui.LoginGUI;
import client.routing.NodeUpdater;
import datatype.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class Client {

    public static final String INETADDRESS = "228.1.1.1";
    public static final int PORT = 6789;

    private ClientGUI clientGUI;
    private HashMap<InetAddress, String> lifeLongDests = new HashMap<>();
    private HashMap<InetAddress, String> destinations = new HashMap<>();
    private HashMap<InetAddress, InetAddress> nextHop = new HashMap<>();
    private HashMap<InetAddress, String> neighbours = new HashMap<>();
    private HashMap<InetAddress, String> lastRoundNeighbours = new HashMap<>();
    private static PacketManager packetManager;
    private static Sender sender;
    private ReentrantLock lock = new ReentrantLock();

    private MulticastSocket mcSocket;

    public static void main(String[] args) throws IOException {
        new LoginGUI();

        //Client client = new Client("hoi");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String message = scanner.nextLine();
            //System.out.println(message);
            Message message1 = new PrivateTextMessage(false, message, "");
            Packet packet = new Packet(Inet4Address.getLocalHost(), InetAddress.getByName("192.168.5.0"), packetManager.getSequenceNumber(InetAddress.getByName(INETADDRESS)), 4, message1);
            packetManager.addSentPacket(packet);
            sender.sendPkt(packet.makeDatagramPacket());
        }
    }

    HashMap<InetAddress, String> getLifeLongDestinations() {
        return lifeLongDests;
    }

    HashMap<InetAddress, String> getDestinations() {
        return destinations;
    }

    public void sendGroupTextMessage(String message) throws IOException {
        Message message1 = new GroupTextMessage(message, "");
        Packet packet = new Packet(Inet4Address.getLocalHost(), InetAddress.getByName("192.168.5.0"), packetManager.getSequenceNumber(InetAddress.getByName(INETADDRESS)), 4, message1);
        sender.sendPkt(packet.makeDatagramPacket());
    }

    public void sendPrivateTextMessage(String message, String nickname) throws IOException {
        Message message1 = new PrivateTextMessage(false, message, "");
        Packet packet = new Packet(Inet4Address.getLocalHost(), clientGUI.getClients().get(nickname), packetManager.getSequenceNumber(InetAddress.getByName(INETADDRESS)), 4, message1);
        packetManager.addSentPacket(packet);
        sender.sendPkt(packet.makeDatagramPacket());
    }

    public Client(String nickname) {
        InetAddress group;
        try {
            group = InetAddress.getByName(INETADDRESS);
            mcSocket = new MulticastSocket(PORT);
            mcSocket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }

        packetManager = new PacketManager();

        new Thread(new KeepAlive(mcSocket, nickname, this, packetManager)).start();

        sender = new Sender(mcSocket, packetManager);


        try {
            new Thread(new Receiver(this, sender, mcSocket, packetManager)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new NodeUpdater(this)).start();
        clientGUI = new ClientGUI(nickname, this);
    }

    void addNeighbour(InetAddress address, BroadcastMessage message) throws UnknownHostException {
        lock.lock();
        this.lastRoundNeighbours.put(address, message.getNickname());
        for (InetAddress e : message.getDestinations().keySet()) {
            if (!destinations.containsKey(e)) {
                if (!e.equals(InetAddress.getLocalHost())) {
                    destinations.put(address, message.getNickname());
                    lifeLongDests.put(address, message.getNickname());
                    nextHop.put(e, address);
                }
            }
        }

        List<InetAddress> toRemove = new ArrayList<>();
        nextHop.keySet().stream().filter(e -> !message.getDestinations().containsKey(e)).filter(e -> nextHop.get(e).equals(address)).forEach(toRemove::add);

        for (InetAddress e : toRemove) {
            destinations.remove(e);
            nextHop.remove(e);
        }
        lock.unlock();
    }

    public void updateNeighbours() {
        lock.lock();
        neighbours.clear();
        neighbours.putAll(lastRoundNeighbours);
        lastRoundNeighbours.clear();
        neighbours.keySet().stream().filter(e -> !destinations.containsKey(e)).forEach(e -> {
            destinations.put(e, neighbours.get(e));
            lifeLongDests.put(e, neighbours.get(e));
            if (!clientGUI.getClients().containsKey(lifeLongDests.get(e))) {
                clientGUI.addClient(lifeLongDests.get(e), e);
            }
            nextHop.put(e, e);
        });

        List<InetAddress> toRemove = new ArrayList<>();
        destinations.keySet().stream().filter(e -> !neighbours.containsKey(e)).forEach(e -> {
            if (nextHop.get(e).equals(e)) {
                toRemove.add(e);
            }
        });

        for (InetAddress e : toRemove) {
            destinations.remove(e);
            clientGUI.removeClient(lifeLongDests.get(e));
            nextHop.remove(e);
        }
        lock.unlock();
    }

    ClientGUI getClientGUI(){
        return clientGUI;
    }
}