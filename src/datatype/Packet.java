package datatype;

import client.Client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public class Packet implements Serializable {

    private InetAddress sourceAddress;
    private InetAddress destinationAddress;
    private long sequenceNumber;
    private int timeToLive;
    private Message payload;

    public Packet(InetAddress sourceAddress, InetAddress destinationAddress, long sequenceNumber, int timeToLive, Message payload) {
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.sequenceNumber = sequenceNumber;
        this.timeToLive = timeToLive;
        this.payload = payload;
    }

    public Packet(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        Packet packet = (Packet) is.readObject();
        this.sourceAddress = packet.getSourceAddress();
        this.destinationAddress = packet.getDestinationAddress();
        this.sequenceNumber = packet.getSequenceNumber();
        this.timeToLive = packet.getTimeToLive();
        this.payload = packet.getPayload();
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    public InetAddress getDestinationAddress() {
        return destinationAddress;
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

    public void decreaseTimeToLive() {
        timeToLive--;
    }

    public int getLength() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream);
        objectOutput.writeObject(this.getPayload());
        return byteArrayOutputStream.toByteArray().length;
    }

    public DatagramPacket makeDatagramPacket() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(this);
        byte[] buf = Arrays.copyOf(bos.toByteArray(),bos.toByteArray().length);
        out.close();

        return new DatagramPacket(buf, buf.length, InetAddress.getByName(Client.INETADDRESS), Client.PORT);
    }
}
