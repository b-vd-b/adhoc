package client;

import datatype.Message;
import datatype.Packet;
import util.Checksum;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class Sender {

    private MulticastSocket multicastSocket;
    private PacketManager packetManager;

    Sender(MulticastSocket multicastSocket, PacketManager packetManager) {
        this.multicastSocket = multicastSocket;
        this.packetManager = packetManager;
    }

    void sendMessage(InetAddress destination, Message payload) throws IOException {
        Packet packet = new Packet(Client.LOCAL_ADDRESS, destination, packetManager.getSequenceNumber(destination), 3, payload, Checksum.getMessageChecksum(payload));
        packetManager.addSentPacket(packet);
        sendDatagramPacket(packet.makeDatagramPacket());
    }

    void sendDatagramPacket(DatagramPacket datagramPacket) throws IOException {
        multicastSocket.send(datagramPacket);
    }

}
