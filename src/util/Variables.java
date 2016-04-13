package util;

/**
 * Created by bvdb on 13-4-2016.
 */
public class Variables {

    /**
     * This is the maximum file size allowed for file transfers used by GroupChatGUI and PrivateChatGUI.
     */
    public static final long MAXIMUM_FILE_SIZE = 500000000;

    /**
     * The maximum amount of times a packet is resent .
     */
    public static final int MAXIMUM_RETRANSMIT_ATTEMPTS = 3;

    /**
     * Sending a packet to let other clients know that this client is still in the network and reachable.
     * @param mcSocket the socket the thread must broadcast on
     */
    public static final String MULTICAST_ADDRESS = "228.1.1.1";

    /**
     * Sending a packet to let other clients know that this client is still in the network and reachable.
     * @param mcSocket the socket the thread must broadcast on
     */
    public static final int PORT = 6789;

    /**
     * Sending a packet to let other clients know that this client is still in the network and reachable.
     * @param mcSocket the socket the thread must broadcast on
     */
    public static final int SLEEP = 1000;

    /**
     * Sending a packet to let other clients know that this client is still in the network and reachable.
     * @param mcSocket the socket the thread must broadcast on
     */

}
