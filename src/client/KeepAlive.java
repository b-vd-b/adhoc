package client;

import datatype.BroadcastMessage;
import datatype.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.MulticastSocket;

public class KeepAlive implements Runnable {

    private boolean running = true;
    private MulticastSocket mcSocket;
    private static final int SLEEP = 1000;
    private String nickname;
    private Client client;
    private PacketManager packetManager;

    /**
     * Sending a packet to let other clients know that this client is still in the network and reachable.
     * @param mcSocket the socket the thread must broadcast on
     */
    public KeepAlive(MulticastSocket mcSocket, String nickname, Client client)  {
        this.mcSocket = mcSocket;
        this.nickname = nickname;
        this.client = client;
    }

    @Override
    public void run() {
        while (running) {
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

    public void stopKeepAlive() {
        running = false;
    }

    public DatagramPacket makeBroadcastPacket() throws IOException {
        Packet packet = new Packet(Inet4Address.getLocalHost(), Inet4Address.getLocalHost(), -1 , 3, new BroadcastMessage(nickname, client.getDestinations()));
        return packet.makeDatagramPacket();
    }
}
