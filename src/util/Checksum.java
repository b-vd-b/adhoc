package util;

import java.util.zip.CRC32;

public class Checksum {

    public static long getCrcValue(byte[] bytes) {
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return crc32.getValue();
    }

    public static long getCrcValue(String message) {
        return getCrcValue(message.getBytes());
    }

}
