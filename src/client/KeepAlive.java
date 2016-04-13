package client;

import datatype.BroadcastMessage;
import datatype.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.MulticastSocket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static util.Variables.MAXIMUM_RETRANSMIT_ATTEMPTS;
import static util.Variables.SLEEP;

class KeepAlive implements Runnable {

    private MulticastSocket mcSocket;
    private String nickname;
    private Client client;
    private PacketManager packetManager;

    /**
     * Sending a packet to let other clients know that this client is still in the network and reachable.
     * @param mcSocket the socket the thread must broadcast on
     */
    KeepAlive(MulticastSocket mcSocket, String nickname, Client client, PacketManager packetManager)  {
        this.mcSocket = mcSocket;
        this.nickname = nickname;
        this.client = client;
        this.packetManager = packetManager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                for (int i = 0; i < 5; i++) {
                    mcSocket.send(makeBroadcastPacket());
                    Thread.sleep(SLEEP);
                }

                List<Packet> toRemove = new ArrayList<>();

                // Retransmit unacknowledged packets up to three times.
                for (Packet packet : packetManager.getUnacknowledgedPackets().keySet()) {
                    int attempts = packetManager.getUnacknowledgedPackets().get(packet);

                    if (attempts == MAXIMUM_RETRANSMIT_ATTEMPTS && !client.getDestinations().containsKey(packet.getDestinationAddress())) {
                        toRemove.add(packet);
                    } else {
                        packetManager.getUnacknowledgedPackets().put(packet, attempts + 1);
                        mcSocket.send(packet.makeDatagramPacket());
                    }
                }

                for (Packet packet : toRemove) {
                    packetManager.getUnacknowledgedPackets().remove(packet);
                }
            } catch (InterruptedException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private DatagramPacket makeBroadcastPacket() throws IOException {
        Packet packet = new Packet(Client.LOCAL_ADDRESS, Client.LOCAL_ADDRESS, -1 , 3, new BroadcastMessage(nickname, client.getDestinations(), client.getEncryptionKeys(), client.getNextHop()));
        return packet.makeDatagramPacket();
    }
}
