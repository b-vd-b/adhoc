package datatype;

public class Packet {

    private long sequenceNumber;
    private int timeToLive;
    private Message payload;

    public Packet(long sequenceNumber, int timeToLive, Message payload) {
        this.sequenceNumber = sequenceNumber;
        this.timeToLive = timeToLive;
        this.payload = payload;
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
}
