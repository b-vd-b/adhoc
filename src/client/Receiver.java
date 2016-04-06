package client;

import datatype.*;

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
                Message message = packet.getPayload();

                if (message instanceof BroadcastMessage) {
                    // TODO: Implement neighbour saving.
                }

                if (packet.getDestinationAddress().equals(Inet4Address.getLocalHost())) {
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
        // TODO: Add payload parsing.
        Message message = packet.getPayload();

        if (message instanceof TextMessage) {
            System.out.println(((TextMessage) message).getMessage());
            acknowledgePacket(packet);
        }
    }

    public void acknowledgePacket(Packet packet) {
        // TODO: Add acknowledgement number calculation.
        long sequenceNumber = packet.getSequenceNumber();
        long acknowledgementNumber = packet.getSequenceNumber() + 1;
        Message message = new AckMessage(acknowledgementNumber);
        Packet acknowledgementPacket = new Packet(packet.getSourceAddress(), packet.getDestinationAddress(), sequenceNumber, 3, message);

        // TODO: Send packet acknowledgement.
    }

    public void stopReceiver() {
        running = false;
    }

}
