package client;

import datatype.Message;
import datatype.Packet;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Sender {

    //VARIABLES

    MulticastSocket msc;

    //CONSTRUCTOR

    public Sender(MulticastSocket msc) {
        this.msc = msc;
    }

    //METHODS

    public void sendMsg(Message msg, long sqnr, int ttl, InetAddress address, int port) {
        Packet pkt = new Packet(sqnr,ttl,msg);
        DatagramPacket dgp = pkt.makeDatagramPacket(address, port);
        try {
            msc.send(dgp);
        } catch (IOException ex) {}
    }

    public void sendPkt(DatagramPacket dgp) {
        try {
            msc.send(dgp);
        } catch (IOException ex) {}
    }

}
