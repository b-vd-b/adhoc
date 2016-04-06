package client;

import datatype.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public class Sender implements Runnable {

    //VARIABLES

    //METHODS

    public void run() {
        //do things
    }

    public void sendMsg(Message msg, InetAddress address, int port) {
        Packet pkt = new Packet(sqnr,ttl,msg);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        byte[] buf = new byte[0];
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(pkt);
            buf = Arrays.copyOf(bos.toByteArray(),bos.toByteArray().length);
            out.close();
        } catch (IOException ex) {

        }
        DatagramPacket dgp = new DatagramPacket(buf,buf.length,address,port);

    }

}
