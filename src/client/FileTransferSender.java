package client;

import datatype.FileTransferMessage;
import datatype.Message;
import util.Checksum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class FileTransferSender implements Runnable {

    private File file;
    private InetAddress destination;
    private Sender sender;

    public FileTransferSender(MulticastSocket multicastSocket, PacketManager packetManager, File file, InetAddress destination) {
        this.file = file;
        this.destination = destination;
        sender = new Sender(multicastSocket, packetManager);
    }

    @Override
    public void run() {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            int read;
            byte[] buffer = new byte[512];

            int offset = 0;
            int size = fileInputStream.available();
            int totalPackets = (int) Math.ceil(size / 512D);

            while ((read = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                if (read != buffer.length) {
                    buffer = Arrays.copyOfRange(buffer, 0, read);
                }

                Message fileTransfer = new FileTransferMessage(file.getName(), offset, buffer, size, totalPackets, Checksum.getCrcValue(buffer));
                offset += read;
                sender.sendMessage(destination, fileTransfer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
