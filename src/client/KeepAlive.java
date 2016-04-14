package client;

import datatype.BroadcastMessage;
import datatype.Message;
import datatype.Packet;
import util.Checksum;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
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
                List<Packet> toIncrement = new ArrayList<>();

                // Retransmit unacknowledged packets up to three times.
                for (Packet packet : packetManager.getUnacknowledgedPackets().keySet()) {
                    int attempts = packetManager.getUnacknowledgedPackets().get(packet);

                    if (attempts == MAXIMUM_RETRANSMIT_ATTEMPTS || !client.getDestinations().containsKey(packet.getDestinationAddress())) {
                        toRemove.add(packet);
                    } else {
                        toIncrement.add(packet);
                        mcSocket.send(packet.makeDatagramPacket());
                    }
                }

                if (toIncrement.size() > 0) {
                    for (Packet packet : toIncrement) {
                        if (packetManager.getUnacknowledgedPackets().get(packet) == null) {
                            continue;
                        }

                        int attempts = packetManager.getUnacknowledgedPackets().get(packet);
                        packetManager.getUnacknowledgedPackets().put(packet, attempts + 1);
                    }
                }

                if (toRemove.size() > 0) {
                    for (Packet packet : toRemove) {
                        packetManager.getUnacknowledgedPackets().remove(packet);
                    }
                }
            } catch (InterruptedException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private DatagramPacket makeBroadcastPacket() throws IOException {
        Message broadcastMessage = new BroadcastMessage(nickname, client.getDestinations(), client.getEncryptionKeys(), client.getNextHop());
        Packet packet = new Packet(Client.LOCAL_ADDRESS, Client.LOCAL_ADDRESS, -1 , 3, broadcastMessage, Checksum.getMessageChecksum(broadcastMessage));
        return packet.makeDatagramPacket();
    }
}
