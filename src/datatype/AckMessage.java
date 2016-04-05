package datatype;

public class AckMessage extends Message {

    private long ackNumber;

    public AckMessage(long ackNumber) {
        this.ackNumber = ackNumber;
    }

    public long getAckNumber() {
        return ackNumber;
    }
}
