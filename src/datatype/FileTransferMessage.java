package datatype;

public class FileTransferMessage extends Message {

    private String fileName;
    private int offset;
    private byte[] fragment;

    public FileTransferMessage(String fileName, int offset, byte[] fragment) {
        this.fileName = fileName;
        this.offset = offset;
        this.fragment = fragment;
    }

    public String getFileName() {
        return fileName;
    }

    public int getOffset() {
        return offset;
    }

    public byte[] getFragment() {
        return fragment;
    }
}
