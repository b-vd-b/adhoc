package datatype;

import java.io.File;

/**
 * Created by bvdb on 12-4-2016.
 */


public class PrivateFileMessage extends Message {

    private boolean encrypted;
    private File file;

    public PrivateFileMessage(boolean encrypted, File file, String checksum){
        this.encrypted = encrypted;
        this.file = file;
    }

    public boolean isEncrypted() {
        return encrypted;
    }
}
