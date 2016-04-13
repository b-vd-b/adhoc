package datatype;

public class FileTransferMessage extends Message {

    private String fileName;
    private int offset;
    private byte[] fragment;
    private int fileLength;
    private int totalPackets;

    public FileTransferMessage(String fileName, int offset, byte[] fragment, int fileLength, int totalPackets) {
        this.fileName = fileName;
        this.offset = offset;
        this.fragment = fragment;
        this.fileLength = fileLength;
        this.totalPackets = totalPackets;
    }

    public int getTotalPackets() {
        return totalPackets;
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

    public int getFileLength() {
        return fileLength;
    }
}
