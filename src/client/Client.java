package client;

import client.gui.LoginGUI;
import client.gui.ClientGUI;
import client.routing.NodeUpdater;
import datatype.BroadcastMessage;
import datatype.Message;
import datatype.Packet;
import datatype.TextMessage;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class Client {

    public static final String INETADDRESS = "228.1.1.1";
    public static final int PORT = 6789;

    private ClientGUI clientGUI;
    private String nickname;
    private HashMap<InetAddress, String> lifeLongDests = new HashMap<>();
    private HashMap<InetAddress, String> destinations = new HashMap<>();
    private HashMap<InetAddress, InetAddress> nextHop = new HashMap<>();
    private HashMap<InetAddress, String> neighbours = new HashMap<>();
    private HashMap<InetAddress, String> lastRoundNeighbours = new HashMap<>();
    private static PacketManager packetManager;
    private static Sender sender;

    private MulticastSocket mcSocket;
    private ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws IOException {
        LoginGUI login = new LoginGUI();

        //Client client = new Client("hoi");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String message = scanner.nextLine();
            //System.out.println(message);
            Message message1 = new TextMessage(false, message, "");
            Packet packet = new Packet(Inet4Address.getLocalHost(), InetAddress.getByName("192.168.5.1"), packetManager.getSequenceNumber(InetAddress.getByName(INETADDRESS)), 4, message1);
            packetManager.addSentPacket(packet);
            sender.sendPkt(packet.makeDatagramPacket());
        }
    }

    public HashMap<InetAddress, String> getlifeLongDestinations() {
        return lifeLongDests;
    }

    public HashMap<InetAddress, String> getDestinations() {
        return destinations;
    }

    public void sendGroupTextMessage(String message) throws IOException {
        Message message1 = new TextMessage(false, message, "");
        Packet packet = new Packet(Inet4Address.getLocalHost(), InetAddress.getByName("192.168.5.1"), packetManager.getSequenceNumber(InetAddress.getByName(INETADDRESS)), 4, message1);
        packetManager.addSentPacket(packet);
        sender.sendPkt(packet.makeDatagramPacket());
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

        packetManager = new PacketManager();

        new Thread(new KeepAlive(mcSocket, this.nickname, this, packetManager)).start();

        sender = new Sender(mcSocket, packetManager);


        try {
            new Thread(new Receiver(this, sender, mcSocket, packetManager)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new NodeUpdater(this)).start();
        clientGUI = new ClientGUI(nickname, this);
    }

    public void addNeighbour(InetAddress address, BroadcastMessage message) throws UnknownHostException {
        this.lastRoundNeighbours.put(address, message.getNickname());
        for (InetAddress e : message.getDestinations().keySet()) {
            if (!destinations.containsKey(e)) {
                if (!e.equals(InetAddress.getLocalHost())) {
                    destinations.put(address, message.getNickname());
                    lifeLongDests.put(address, message.getNickname());
                    if(!clientGUI.getClients().containsKey(e)){
                        clientGUI.addClient(address, message.getNickname());
                    }
                    nextHop.put(e, address);
                }
            }
        }
        for (InetAddress e : nextHop.keySet()) {
            if (!message.getDestinations().containsKey(e)) {
                if (nextHop.get(e).equals(address)) {
                    destinations.remove(e);
                    clientGUI.removeClient(e);
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
                lifeLongDests.put(e, neighbours.get(e));
                if(!clientGUI.getClients().containsKey(e)){
                    clientGUI.addClient(e, neighbours.get(e));
                }
                nextHop.put(e, e);
            }
        }
        for (InetAddress e : destinations.keySet()) {
            if (!neighbours.containsKey(e)) {
                System.out.println(nextHop.get(e) == e);
                if (nextHop.get(e).equals(e)) {
                    destinations.remove(e);
                    clientGUI.removeClient(e);
                    nextHop.remove(e);
                }
            }
        }
        lock.unlock();
    }

    public ClientGUI getClientGUI(){
        return clientGUI;
    }
}