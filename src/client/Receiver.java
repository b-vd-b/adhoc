package client;

import datatype.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.security.InvalidKeyException;

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
                if (packet.getSourceAddress().equals(Client.LOCAL_ADDRESS)) {
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
                if (packet.getDestinationAddress().equals(Client.LOCAL_ADDRESS)) {
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
        String nickname = client.getDestinations().get(packet.getSourceAddress());
        String msg;
        if (message instanceof AckMessage) {
            packetManager.parseAcknowledgement(packet);
        } else if (message instanceof GroupTextMessage) {
            msg = ((GroupTextMessage) message).getMessage();
            client.getClientGUI().newGroupMessage(nickname, msg);
            acknowledgePacket(packet);
        } else if (message instanceof PrivateTextMessage) {
            if (((PrivateTextMessage) message).isEncrypted()) {
                try {
                    msg = client.getEncryption().decryptMessage(((PrivateTextMessage) message).getMessage());
                } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                    client.getClientGUI().newPrivateMessage("SYSTEM", "Received a message from: " + client.getDestinations().get(packet.getSourceAddress()) + " But unfortunately this message was malformed and can not be recovered");
                    return;
                }
            } else {
                msg = ((PrivateTextMessage) message).getMessage();
            }
            client.getClientGUI().newPrivateMessage(nickname, msg);
            acknowledgePacket(packet);
        } else if (message instanceof GroupFileMessage){

        } else if (message instanceof PrivateFileMessage){

        }
    }

    private void acknowledgePacket(Packet packet) throws IOException {
        long sequenceNumber = packet.getSequenceNumber();
        long acknowledgementNumber = packet.getSequenceNumber() + packet.getLength();
        Message message = new AckMessage(acknowledgementNumber);
        Packet acknowledgementPacket = new Packet(Client.LOCAL_ADDRESS, packet.getSourceAddress(), sequenceNumber, 3, message);

        sender.sendDatagramPacket(acknowledgementPacket.makeDatagramPacket());
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
            sender.sendDatagramPacket(packet.makeDatagramPacket());
        }
    }

    public void stopReceiver() {
        running = false;
    }

}
