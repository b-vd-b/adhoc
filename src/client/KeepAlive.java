package client;

import datatype.BroadcastMessage;
import datatype.Message;
import datatype.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class KeepAlive implements Runnable {

    private boolean running = true;
    private MulticastSocket mcSocket;
    private static final int SLEEP = 1000;
    private String nickname;

    /**
     * Sending a packet to let other clients know that this client is still in the network and reachable.
     * @param mcSocket the socket the thread must broadcast on
     */
    public KeepAlive(MulticastSocket mcSocket, String nickname)  {
        this.mcSocket = mcSocket;
        this.nickname = nickname;
    }

    @Override
    public void run() {
        while (running) {
            try {
                mcSocket.send(makeBroadcastPacket());
                this.wait(SLEEP);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopKeepAlive() {
        running = false;
    }

    public DatagramPacket makeBroadcastPacket() {
        Packet packet = new Packet(-1 , 3, new BroadcastMessage(nickname));
        return packet.makeDatagramPacket(mcSocket.getInetAddress(), mcSocket.getPort());
    }
}
