package client;

import datatype.Message;
import datatype.Packet;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Sender {

    //VARIABLES

    private MulticastSocket msc;
    private PacketManager packetManager;

    //CONSTRUCTOR

    public Sender(MulticastSocket msc, PacketManager packetManager) {
        this.msc = msc;
        this.packetManager = packetManager;
    }

    //METHODS

    public void sendMessage(Message msg, long sqnr, int ttl, InetAddress address, int port) throws IOException {
        Packet pkt = new Packet(null, null, 0, 0, msg);
        DatagramPacket dgp = pkt.makeDatagramPacket();
        try {
            msc.send(dgp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPkt(DatagramPacket dgp) {
        try {
            msc.send(dgp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
