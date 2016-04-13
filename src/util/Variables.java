package util;

/**
 * Created by bvdb on 13-4-2016.
 */
public class Variables {


    /**
     * The name of the program
     */
    public static final String PROGRAM_NAME = "Awesome ad hoc chat!";

    /**
     * This is the maximum file size allowed for file transfers used by GroupChatGUI and PrivateChatGUI.
     */
    public static final long MAXIMUM_FILE_SIZE = 500000000;

    /**
     * The maximum amount of times a packet will be resend when a destination is not longer known.
     */
    public static final int MAXIMUM_RETRANSMIT_ATTEMPTS = 3;

    /**
     * The address used to broadcast on.
     */
    public static final String MULTICAST_ADDRESS = "228.1.1.1";

    /**
     * Port number used to broadcast on.
     */
    public static final int PORT = 6789;

    /**
     * The time between broadcast announcements.
     */
    public static final int SLEEP = 1000;

    /**
     * Sending a packet to let other clients know that this client is still in the network and reachable.
     * @param mcSocket the socket the thread must broadcast on
     */

}
