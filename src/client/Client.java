package client;

import client.gui.ClientGUI;
import client.gui.LoginGUI;
import client.routing.NodeUpdater;
import datatype.*;
import util.Encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static util.Variables.MULTICAST_ADDRESS;
import static util.Variables.PORT;

public class Client {


    private static final boolean DEBUG_MODE = false;

    static InetAddress LOCAL_ADDRESS;
    private static InetAddress GROUP_CHAT_ADDRESS;
    private static PacketManager packetManager;
    private static Sender sender;
    private ClientGUI clientGUI;
    private HashMap<InetAddress, PublicKey> encryptionKeys = new HashMap<>();
    private HashMap<InetAddress, String> destinations = new HashMap<>();
    private HashMap<InetAddress, InetAddress> nextHop = new HashMap<>();
    private HashMap<InetAddress, String> neighbours = new HashMap<>();
    private HashMap<InetAddress, String> lastRoundNeighbours = new HashMap<>();
    private Encryption encryption;

    private MulticastSocket mcSocket;

    public Client(String nickname) {
        encryption = new Encryption();
        clientGUI = new ClientGUI(nickname, this);
        InetAddress group;
        try {
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            mcSocket = new MulticastSocket(PORT);
            mcSocket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }

        encryptionKeys.put(LOCAL_ADDRESS, encryption.getPublicKey());

        packetManager = new PacketManager();

        new Thread(new KeepAlive(mcSocket, nickname, this, packetManager)).start();

        sender = new Sender(mcSocket, packetManager);

        try {
            new Thread(new Receiver(this, sender, mcSocket, packetManager)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new NodeUpdater(this)).start();

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

    HashMap<InetAddress, String> getDestinations() {
        return destinations;
    }

    public void sendGroupTextMessage(String contents) throws IOException {
        Message message = new GroupTextMessage(contents, "");
        for (InetAddress destination : clientGUI.getClients().values()) {
            sender.sendMessage(destination, message);
        }
    }

    public void sendPrivateTextMessage(String contents, String nickname) throws IOException {
        String encryptedMessage;
        try {
            encryptedMessage = encryption.encryptMessage(contents, encryptionKeys.get(clientGUI.getClients().get(nickname)));
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            clientGUI.newPrivateMessage("SYSTEM", "Sorry something went wrong, please try again later");
            return;
        }
        Message message = new PrivateTextMessage(true, encryptedMessage, "");
        InetAddress destination = clientGUI.getClients().get(nickname);
        sender.sendMessage(destination, message);
    }
    //todo: implement how to send a file to the group
    public void sendGroupFileMessage(File file, String fileName){

    }
    //todo: implement how to send a file privately
    public void sendPrivateFileMessage(String nickname, File file, String fileName){

    }

    synchronized void addNeighbour(InetAddress address, BroadcastMessage message) throws UnknownHostException {
        //Add the neighbour to the lastRoundNeighbours HashMap
        if (!lastRoundNeighbours.containsKey(address)) {
            lastRoundNeighbours.put(address, message.getNickname());
        }

        if (!destinations.containsKey(address)) {
            clientGUI.addClient(message.getNickname(), address);
        }
        //Add the neighbour to the destination and nextHop HashMap if it isn't already
        destinations.put(address, message.getNickname());
        nextHop.put(address, LOCAL_ADDRESS);

        encryptionKeys.put(address, message.getPublicKeys().get(address));

        //Add the destinations of this neighbour to our own destinations with the next hop set to the neighbour
        for (InetAddress e : message.getDestinations().keySet()) {
            if (!destinations.containsKey(e)) {
                if (!e.equals(LOCAL_ADDRESS)) {
                    if (!message.getNextHop().get(e).equals(LOCAL_ADDRESS)) {
                        destinations.put(e, message.getDestinations().get(e));
                        nextHop.put(e, address);
                        clientGUI.addClient(message.getDestinations().get(e), e);
                        encryptionKeys.put(e, message.getPublicKeys().get(e));
                    }
                }
            }
        }

        //Delete the neighbours that aren't reachable anymore through this neighbour if there are any
        List<InetAddress> localDestinations = new ArrayList<>();
        for (InetAddress e : nextHop.keySet()) {
            if (nextHop.get(e).equals(address)) {
                localDestinations.add(e);
            }
        }

        for (InetAddress e : localDestinations) {
            if (!message.getDestinations().containsKey(e)) {
                clientGUI.removeClient(destinations.get(e));
                destinations.remove(e);
                nextHop.remove(e);
                encryptionKeys.remove(e);
            }
        }
    }

    public synchronized void updateNeighbours() {
        //Put all the dropped neighbours in a list
        List<InetAddress> droppedNeighbours = new ArrayList<>();
        for (InetAddress e : neighbours.keySet()) {
            if (!lastRoundNeighbours.containsKey(e)) {
                droppedNeighbours.add(e);
            }
        }
        //Remove all the destinations that were associated with the dropped neighbours
        List<InetAddress> toRemoveDestinations = new ArrayList<>();
        for (InetAddress e : destinations.keySet()) {
            if (droppedNeighbours.contains(e) || droppedNeighbours.contains(nextHop.get(e))) {
                toRemoveDestinations.add(e);
            }
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
    }

    ClientGUI getClientGUI() {
        return clientGUI;
    }

    synchronized HashMap<InetAddress, PublicKey> getEncryptionKeys() {
        return encryptionKeys;
    }

    Encryption getEncryption() {
        return encryption;
    }

    public HashMap<InetAddress, InetAddress> getNextHop() {
        return nextHop;
    }
}