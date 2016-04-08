package client.routing;

import client.Client;

public class NodeUpdater implements Runnable {

    private Client client;

    public NodeUpdater(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.updateNeighbours();
        }
    }
}
