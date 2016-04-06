package client;

import datatype.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.MulticastSocket;

class Receiver implements Runnable {

    private boolean running = true;

    private Client client;
    private Sender sender;
    private MulticastSocket socket;

    Receiver(Client client, Sender sender, MulticastSocket socket) throws IOException {
        this.client = client;
        this.sender = sender;
        this.socket = socket;
    }

    @Override
    public void run() {
        while (running) {
            try {
                byte[] buffer = new byte[4096];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagramPacket);

                Packet packet = new Packet(buffer);
                Message message = packet.getPayload();

                if (message instanceof BroadcastMessage) {
                    client.addNeighbour(packet.getSourceAddress(), ((BroadcastMessage) message).getNickname());
                }

                if (packet.getDestinationAddress().equals(Inet4Address.getLocalHost())) {
                    parsePacket(packet);
                } else {
                    packet.decreaseTimeToLive();
                    sender.sendPkt(packet.makeDatagramPacket());
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void parsePacket(Packet packet) throws IOException {
        // TODO: Add payload parsing.
        Message message = packet.getPayload();

        if (message instanceof TextMessage) {
            System.out.println(((TextMessage) message).getMessage());
            acknowledgePacket(packet);
        }
    }

    private void acknowledgePacket(Packet packet) throws IOException {
        // TODO: Add acknowledgement number calculation.
        long sequenceNumber = packet.getSequenceNumber();
        long acknowledgementNumber = packet.getSequenceNumber() + 1;
        Message message = new AckMessage(acknowledgementNumber);
        Packet acknowledgementPacket = new Packet(packet.getSourceAddress(), packet.getDestinationAddress(), sequenceNumber, 3, message);

        sender.sendPkt(acknowledgementPacket.makeDatagramPacket());
    }

    public void stopReceiver() {
        running = false;
    }

}
