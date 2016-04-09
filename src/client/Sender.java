package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

class Sender {

    private MulticastSocket multicastSocket;

    Sender(MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
    }

    void sendDatagramPacket(DatagramPacket datagramPacket) {
        try {
            multicastSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
