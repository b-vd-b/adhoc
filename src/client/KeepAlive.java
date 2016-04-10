package client;

import datatype.BroadcastMessage;
import datatype.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.MulticastSocket;
import java.security.PublicKey;

class KeepAlive implements Runnable {

    private MulticastSocket mcSocket;
    private static final int SLEEP = 500;
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

                // Retransmit unacknowledged packets.
                for (Packet packet : packetManager.getUnacknowledgedPackets()) {
                    mcSocket.send(packet.makeDatagramPacket());
                }
            } catch (InterruptedException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private DatagramPacket makeBroadcastPacket() throws IOException {
        Packet packet = new Packet(Client.LOCAL_ADDRESS, Client.LOCAL_ADDRESS, -1 , 3, new BroadcastMessage(nickname, client.getDestinations(), client.getEncryptionKeys()));
        return packet.makeDatagramPacket();
    }
}
