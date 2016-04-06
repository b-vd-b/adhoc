package client;

import datatype.BroadcastMessage;
import datatype.Packet;

import java.io.*;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.MulticastSocket;

public class Receiver implements Runnable {

    private boolean running = true;

    private Sender sender;
    private MulticastSocket socket;

    public Receiver(Sender sender, MulticastSocket socket) throws IOException {
        this.sender = sender;
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

                if (packet.getDestinationAddress().toString().equals(Inet4Address.getLocalHost().getHostAddress())) {
                    parsePacket(packet);
                } else {
                    packet.decreaseTimeToLive();
                    sender.sendPkt(packet.makeDatagramPacket(socket.getInetAddress(), socket.getPort()));
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void parsePacket(Packet packet) {
        // TODO: Add Packet parsing.
    }

    public void stopReceiver() {
        running = false;
    }

}
