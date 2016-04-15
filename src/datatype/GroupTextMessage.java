package datatype;

public class GroupTextMessage extends Message {

    private String message;

    public GroupTextMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
