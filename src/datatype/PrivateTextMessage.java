package datatype;

public class PrivateTextMessage extends Message {

    private boolean encrypted;
    private String message;
    private String checksum;

    public PrivateTextMessage(boolean encrypted, String message, String checksum) {
        this.encrypted = encrypted;
        this.message = message;
        this.checksum = checksum;
    }

    public boolean isEncrypted() { return encrypted; }

    public String getMessage() {
        return message;
    }

    public String getChecksum() {
        return checksum;
    }
}
