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
    private PacketManager packetManager;

    Receiver(Client client, Sender sender, MulticastSocket socket, PacketManager packetManager) throws IOException {
        this.client = client;
        this.sender = sender;
        this.socket = socket;
        this.packetManager = packetManager;
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

                // Ignore all packets with invalid fields.
                if (isInvalidPacket(packet)) {
                    continue;
                }

                // Ignore all packets sent by own host.
                if (packet.getSourceAddress().equals(Inet4Address.getLocalHost())) {
                    continue;
                }

                // Parse broadcast message by adding destinations to known destinations list.
                if (message instanceof BroadcastMessage) {
                    client.addNeighbour(packet.getSourceAddress(), ((BroadcastMessage) message));
                    continue;
                }

                // Ignore packets that have been received earlier and are retransmitted by neighbours.
                if (packetManager.isKnownPacket(packet)) {
                    continue;
                } else {
                    packetManager.addReceivedPacket(packet);
                }

                // Parse packet if it has arrived at final destination, retransmit packet if not.
                if (packet.getDestinationAddress().equals(Inet4Address.getLocalHost()) || packet.getPayload() instanceof GroupTextMessage) {
                    parsePacket(packet);
                } else {
                    retransmitPacket(packet);
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void parsePacket(Packet packet) throws IOException {
        // TODO: Add payload parsing.
        Message message = packet.getPayload();

        if (message instanceof AckMessage) {
            packetManager.parseAcknowledgement(packet);
        } else if (message instanceof GroupTextMessage) {
            String nickname = client.getLifeLongDestinations().get(packet.getSourceAddress());
            String msg = ((GroupTextMessage) message).getMessage();
            client.getClientGUI().newGroupMessage(nickname, msg);
            retransmitPacket(packet);
        } else if (message instanceof PrivateTextMessage) {
            String nickname = client.getLifeLongDestinations().get(packet.getSourceAddress());
            String msg = ((PrivateTextMessage) message).getMessage();
            client.getClientGUI().newPrivateMessage(nickname, msg);
            acknowledgePacket(packet);
        }
    }

    private void acknowledgePacket(Packet packet) throws IOException {
        long sequenceNumber = packet.getSequenceNumber();
        long acknowledgementNumber = packet.getSequenceNumber() + packet.getLength();
        Message message = new AckMessage(acknowledgementNumber);
        Packet acknowledgementPacket = new Packet(Inet4Address.getLocalHost(), packet.getSourceAddress(), sequenceNumber, 3, message);

        sender.sendPkt(acknowledgementPacket.makeDatagramPacket());
    }

    private boolean isInvalidPacket(Packet packet) {
        boolean isInvalid = false;

        if (packet.getSourceAddress() == null) {
            isInvalid = true;
        }
        if (packet.getDestinationAddress() == null) {
            isInvalid = true;
        }

        return isInvalid;
    }

    private void retransmitPacket(Packet packet) throws IOException {
        packet.decreaseTimeToLive();

        // Only send packets that are still allowed to be retransmitted.
        if (packet.getTimeToLive() > 0) {
            sender.sendPkt(packet.makeDatagramPacket());
        }
    }

    public void stopReceiver() {
        running = false;
    }

}
