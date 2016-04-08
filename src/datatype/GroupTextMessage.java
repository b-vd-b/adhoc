package datatype;

public class GroupTextMessage extends Message {

    private String message;
    private String checksum;

    public GroupTextMessage(String message, String checksum) {
        this.message = message;
        this.checksum = checksum;
    }

    public String getMessage() {
        return message;
    }

    public String getChecksum() {
        return checksum;
    }
}
