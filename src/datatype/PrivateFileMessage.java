package datatype;

import java.io.File;

/**
 * Created by bvdb on 12-4-2016.
 */


public class PrivateFileMessage extends Message {

    private boolean encrypted;

    public PrivateFileMessage(boolean encrypted, byte[] data, String checksum){
        this.encrypted = encrypted;
    }

    public boolean isEncrypted() {
        return encrypted;
    }
}
