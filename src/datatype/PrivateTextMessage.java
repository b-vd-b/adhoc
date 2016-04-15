package datatype;

public class PrivateTextMessage extends Message {

    private boolean encrypted;
    private String message;

    public PrivateTextMessage(boolean encrypted, String message) {
        this.encrypted = encrypted;
        this.message = message;
    }

    public boolean isEncrypted() { return encrypted; }

    public String getMessage() {
        return message;
    }
}
