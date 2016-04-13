package datatype;

public class FileTransferMessage extends Message {

    private String fileName;
    private int length;
    private int offset;
    private int fragmentSize;
    private byte[] fragment;

    public FileTransferMessage(String fileName, int length, int offset, int fragmentSize, byte[] fragment) {
        this.fileName = fileName;
        this.length = length;
        this.offset = offset;
        this.fragmentSize = fragmentSize;
        this.fragment = fragment;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return offset;
    }

    public int getFragmentSize() {
        return fragmentSize;
    }

    public byte[] getFragment() {
        return fragment;
    }
}
