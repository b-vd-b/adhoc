package client;

import client.gui.ClientGUI;
import client.gui.LoginGUI;
import client.routing.NodeUpdater;
import datatype.*;
import util.Encryption;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Client {

    public static final String MULTICAST_ADDRESS = "228.1.1.1";
    public static final int PORT = 6789;
    private static final boolean DEBUG_MODE = false;

    static InetAddress LOCAL_ADDRESS;
    private static InetAddress GROUP_CHAT_ADDRESS;
    private static PacketManager packetManager;
    private static Sender sender;
    private ClientGUI clientGUI;
    private HashMap<InetAddress, String> lifeLongDests = new HashMap<>();
    private HashMap<InetAddress, PublicKey> encryptionKeys = new HashMap<>();
    private HashMap<InetAddress, String> destinations = new HashMap<>();
    private HashMap<InetAddress, InetAddress> nextHop = new HashMap<>();
    private HashMap<InetAddress, String> neighbours = new HashMap<>();
    private HashMap<InetAddress, String> lastRoundNeighbours = new HashMap<>();
    private Encryption encryption;

    private MulticastSocket mcSocket;

    public Client(String nickname) {
        encryption = new Encryption();

        InetAddress group;
        try {
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            mcSocket = new MulticastSocket(PORT);
            mcSocket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            encryptionKeys.put(InetAddress.getLocalHost(), encryption.getPublicKey());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        packetManager = new PacketManager();

        new Thread(new KeepAlive(mcSocket, nickname, this, packetManager)).start();

        sender = new Sender(mcSocket);

        try {
            new Thread(new Receiver(this, sender, mcSocket, packetManager)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new NodeUpdater(this)).start();
        clientGUI = new ClientGUI(nickname, this);
    }

    public static void main(String[] args) throws IOException {
        if (DEBUG_MODE) {
            Random random = new Random();
            LOCAL_ADDRESS = InetAddress.getByName(random.nextInt(256)
                    + "." + random.nextInt(256)
                    + "." + random.nextInt(256)
                    + "." + random.nextInt(256)
            );
        } else {
            LOCAL_ADDRESS = InetAddress.getLocalHost();
        }

        GROUP_CHAT_ADDRESS = InetAddress.getByName("192.168.5.0");
        new LoginGUI();
    }

    synchronized HashMap<InetAddress, String> getLifeLongDestinations() {
        return lifeLongDests;
    }

    HashMap<InetAddress, String> getDestinations() {
        return destinations;
    }

    public void sendGroupTextMessage(String message) throws IOException {
        Message message1 = new GroupTextMessage(message, "");
        Packet packet = new Packet(LOCAL_ADDRESS, GROUP_CHAT_ADDRESS, packetManager.getSequenceNumber(InetAddress.getByName(MULTICAST_ADDRESS)), 3, message1);
        sender.sendDatagramPacket(packet.makeDatagramPacket());
    }

    public void sendPrivateTextMessage(String message, String nickname) throws IOException {
        String encryptedMessage = encryption.encryptMessage(message, encryptionKeys.get(clientGUI.getClients().get(nickname)));
        Message message1 = new PrivateTextMessage(true, encryptedMessage, "");
        Packet packet = new Packet(LOCAL_ADDRESS, clientGUI.getClients().get(nickname), packetManager.getSequenceNumber(InetAddress.getByName(MULTICAST_ADDRESS)), 3, message1);
        packetManager.addSentPacket(packet);
        sender.sendDatagramPacket(packet.makeDatagramPacket());
    }

    synchronized void addNeighbour(InetAddress address, BroadcastMessage message) throws UnknownHostException {
        //Add the neighbour to the lastRoundNeighbours HashMap
        if (!lastRoundNeighbours.containsKey(address)) {
            lastRoundNeighbours.put(address, message.getNickname());
        } else return;

        //Add the neighbour to the lifeLongDests if it isn't already
        if (!lifeLongDests.containsKey(address)) {
            lifeLongDests.put(address, message.getNickname());
        }

        //Add the neighbour to the destination, nextHop and publicKeys HashMap if it isn't already
        if (!destinations.containsKey(address)) {
            destinations.put(address, message.getNickname());
            nextHop.put(address, address);
            clientGUI.addClient(message.getNickname(), address);
        }

        //Add the destinations of this neighbour to our own destinations with the next hop set to the neighbour
        message.getDestinations().keySet().stream().filter(e -> !destinations.containsKey(e)).filter(e -> !e.equals(Client.LOCAL_ADDRESS)).forEach(e -> {
            destinations.put(e, message.getDestinations().get(e));
            nextHop.put(e, address);
        });

        //Add public keys of the neighbours of the received neighbour in own HashMap
        message.getPublicKeys().keySet().stream().filter(e -> !encryptionKeys.containsKey(e)).forEach(e -> {
            encryptionKeys.put(e, message.getPublicKeys().get(e));
        });
        /*
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
        lock.unlock();*/
    }

    public synchronized void updateNeighbours() {
        List<InetAddress> droppedNeighbours = new ArrayList<>();
        //Remove dropped neighbours from last transmission if they didn't broadcast
        neighbours.keySet().stream().filter(e -> !lastRoundNeighbours.containsKey(e)).forEach(e -> {
            destinations.remove(e);
            nextHop.remove(e);
            encryptionKeys.remove(e);
            droppedNeighbours.add(e);
        });

        List<InetAddress> toRemoveDestinations = new ArrayList<>();
        //Remove all the destinations that were associated with the dropped neighbours
        for (InetAddress e : nextHop.keySet()) {
            toRemoveDestinations.addAll(droppedNeighbours.stream().filter(i -> nextHop.get(e).equals(i)).map(i -> e).collect(Collectors.toList()));
        }

        for (InetAddress e : toRemoveDestinations) {
            clientGUI.removeClient(destinations.get(e));
            destinations.remove(e);
            nextHop.remove(e);
            encryptionKeys.remove(e);
        }

        //Set the last round neighbours to the current round neighbours
        neighbours.clear();
        neighbours.putAll(lastRoundNeighbours);
        lastRoundNeighbours.clear();

        /*lock.lock();
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
        lock.unlock();*/
    }

    ClientGUI getClientGUI() {
        return clientGUI;
    }

    public HashMap<InetAddress, PublicKey> getEncryptionKeys() {
        return encryptionKeys;
    }

    public Encryption getEncryption() {
        return encryption;
    }
}