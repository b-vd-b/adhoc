package client;

import datatype.*;
import util.Checksum;
import util.Variables;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.InvalidKeyException;
import java.util.HashMap;

class Receiver implements Runnable {

    private boolean running = true;

    private Client client;
    private Sender sender;
    private MulticastSocket socket;
    private PacketManager packetManager;

    private byte[] currentFile;
    private int filePacketCount;
    private HashMap<InetAddress, HashMap<Long, Packet>> queue;
    private HashMap<InetAddress, Long> lastSequenceNumbers;

    Receiver(Client client, Sender sender, MulticastSocket socket, PacketManager packetManager) throws IOException {
        this.client = client;
        this.sender = sender;
        this.socket = socket;
        this.packetManager = packetManager;
        this.queue = new HashMap<>();
        this.lastSequenceNumbers = new HashMap<>();
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

                if (Client.RANDOM_PACKET_DROP) {
                    double chance = Math.random();
                    if (chance < 0.2) {
                        continue;
                    }
                }

                //creating a pipeline
                //if (datagramPacket.getAddress().equals(InetAddress.getByName("192.168.5.1"))||datagramPacket.getAddress().equals(InetAddress.getByName("192.168.5.2"))){
                //     continue;
                // }
                if(Math.random()*10<1) {
                    continue;
                }
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

                // Packet payload changed during propagation, so drop it.
                if (packet.getChecksum() != Checksum.getMessageChecksum(message)) {
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
            queue.putIfAbsent(packet.getSourceAddress(), new HashMap<>());
            lastSequenceNumbers.putIfAbsent(packet.getSourceAddress(), (long) 0);
            System.out.println(lastSequenceNumbers.toString());
            System.out.println(queue.toString());
            System.out.println(packet.getSequenceNumber());
            System.out.println("Berekend: " + (lastSequenceNumbers.get(packet.getSourceAddress())));
            if (packet.getSequenceNumber() == lastSequenceNumbers.get(packet.getSourceAddress())) {
                System.out.println("Komt door de eerste if");
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
                lastSequenceNumbers.put(packet.getSourceAddress(), packet.getSequenceNumber() + packet.getLength());
            } else {
                HashMap<Long, Packet> tmp = queue.get(packet.getSourceAddress());
                tmp.put(packet.getSequenceNumber(), packet);
                queue.put(packet.getSourceAddress(), tmp);
            }

            checkPrivateQueue(packet.getSourceAddress());

        } else if (message instanceof GroupFileMessage){
            GroupFileMessage groupFileMessage = (GroupFileMessage) message;

            File file = Variables.DOWNLOADS_DIRECTORY.resolve(groupFileMessage.getFileName()).toFile();

            if (filePacketCount == 0) {
                currentFile = new byte[groupFileMessage.getFileLength()];
            }

            System.arraycopy(groupFileMessage.getFragment(), 0, currentFile, groupFileMessage.getOffset(), groupFileMessage.getFragment().length);
            filePacketCount++;

            if (filePacketCount == groupFileMessage.getTotalPackets()) {
                client.getClientGUI().newGroupMessage(nickname, "I have received: "+file.getName());
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(currentFile);
                fileOutputStream.flush();
                fileOutputStream.close();
                filePacketCount = 0;
            }

            acknowledgePacket(packet);
        } else if (message instanceof PrivateFileMessage){
            PrivateFileMessage privateFileMessage= (PrivateFileMessage) message;

            File file = Variables.DOWNLOADS_DIRECTORY.resolve(privateFileMessage.getFileName()).toFile();

            if (filePacketCount == 0) {
                currentFile = new byte[privateFileMessage.getFileLength()];
            }

            System.arraycopy(privateFileMessage.getFragment(), 0, currentFile, privateFileMessage.getOffset(), privateFileMessage.getFragment().length);
            filePacketCount++;

            if (filePacketCount == privateFileMessage.getTotalPackets()) {
                client.getClientGUI().newPrivateMessage(nickname, "I have received: "+file.getName());
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(currentFile);
                fileOutputStream.flush();
                fileOutputStream.close();
                filePacketCount = 0;
            }

            acknowledgePacket(packet);
        } /*else if (message instanceof FileTransferMessage) {
            FileTransferMessage fileTransfer = (FileTransferMessage) message;

            File file = Variables.DOWNLOADS_DIRECTORY.resolve(fileTransfer.getFileName()).toFile();

            if (filePacketCount == 0) {
                currentFile = new byte[fileTransfer.getFileLength()];
            }

            System.arraycopy(fileTransfer.getFragment(), 0, currentFile, fileTransfer.getOffset(), fileTransfer.getFragment().length);
            filePacketCount++;

            if (filePacketCount == fileTransfer.getTotalPackets()) {
                client.getClientGUI().newPrivateMessage(nickname, "I have received: "+file.getName());
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(currentFile);
                fileOutputStream.flush();
                fileOutputStream.close();
                filePacketCount = 0;
            }

            acknowledgePacket(packet);
        }*/
    }

    private void acknowledgePacket(Packet packet) throws IOException {
        long sequenceNumber = packet.getSequenceNumber();
        long acknowledgementNumber = packet.getSequenceNumber() + packet.getLength();
        Message message = new AckMessage(acknowledgementNumber);
        Packet acknowledgementPacket = new Packet(Client.LOCAL_ADDRESS, packet.getSourceAddress(), sequenceNumber, 3, message, Checksum.getMessageChecksum(message));

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

    private void checkPrivateQueue(InetAddress address) throws IOException {
        if (!queue.get(address).isEmpty()) {
            for (Long l : queue.get(address).keySet()) {
                Packet packet = queue.get(address).get(l);
                String msg;
                if (lastSequenceNumbers.get(address) + packet.getLength() == packet.getSequenceNumber()) {
                    if (((PrivateTextMessage) packet.getPayload()).isEncrypted()) {
                        try {
                            msg = client.getEncryption().decryptMessage(((PrivateTextMessage) packet.getPayload()).getMessage());
                        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                            client.getClientGUI().newPrivateMessage("SYSTEM", "Received a message from: " + client.getDestinations().get(packet.getSourceAddress()) + " But unfortunately this message was malformed and can not be recovered");
                            continue;
                        }
                    } else {
                        msg = ((PrivateTextMessage) packet.getPayload()).getMessage();
                    }
                    client.getClientGUI().newPrivateMessage(client.getDestinations().get(packet.getSourceAddress()), msg);
                    queue.get(packet.getSourceAddress()).remove(l);
                    lastSequenceNumbers.put(packet.getSourceAddress(), l);
                }
            }
            if (queue.get(address).containsKey(lastSequenceNumbers.get(address))) {
                checkPrivateQueue(address);
            }
        }
    }

}
