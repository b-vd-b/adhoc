package client;

import datatype.BroadcastMessage;
import datatype.Packet;

import java.io.*;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class Receiver implements Runnable {

    private boolean running = true;

    private MulticastSocket socket;

    public Receiver(MulticastSocket socket) throws IOException {
        this.socket = socket;
    }

    @Override
    public void run() {
        if (running) {
            try {
                byte[] buffer = new byte[4096];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagramPacket);

                Packet packet = new Packet(buffer);

                if (packet.getPayload() instanceof BroadcastMessage) {
                    // TODO: Implement neighbour saving.
                }

                if (datagramPacket.getSocketAddress().equals(socket.getLocalSocketAddress())) {
                    // TODO: Parse packet payload.
                } else {
                    // TODO: Retransmit packet.
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopReceiver() {
        running = false;
    }

}
