package datatype;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public class Packet {

    private long sequenceNumber;
    private int timeToLive;
    private Message payload;

    public Packet(long sequenceNumber, int timeToLive, Message payload) {
        this.sequenceNumber = sequenceNumber;
        this.timeToLive = timeToLive;
        this.payload = payload;
    }

    public Packet(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        Packet packet = (Packet) is.readObject();
        this.sequenceNumber = packet.getSequenceNumber();
        this.timeToLive = packet.getTimeToLive();
        this.payload = packet.getPayload();
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public Message getPayload() {
        return payload;
    }

    public DatagramPacket makeDatagramPacket(InetAddress address, int port) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        byte[] buf = new byte[0];
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
            buf = Arrays.copyOf(bos.toByteArray(),bos.toByteArray().length);
            out.close();
        } catch (IOException ex) {

        }
        DatagramPacket dgp = new DatagramPacket(buf,buf.length,address,port);
        return null;
    }
}
