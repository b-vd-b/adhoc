package client.routing;

import client.Client;

public class NodeUpdater implements Runnable {

    private Client client;
    private boolean running = true;

    public NodeUpdater(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (running) {
            for (int i = 0; i < 3; i++) {
                try {
                    this.wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                client.updateNeighbours();
            }
        }
    }

    public void stopNodeUpdater() {
        running = false;
    }
}
