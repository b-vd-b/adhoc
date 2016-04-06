package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class KeepAlive implements Runnable {

    private boolean running = true;
    private MulticastSocket mcSocket;
    private static final int SLEEP = 1000;

    /**
     * Constructor with standard socketport 500.
     */
    public KeepAlive() {
        try {
            mcSocket = new MulticastSocket(500);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor with given port number.
     * @param port
     */
    public KeepAlive(int port) {
        try {
            mcSocket = new MulticastSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                this.wait(SLEEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //mcSocket.send();
        }

    }

    public void stopKeepAlive() {
        running = false;
    }
}
