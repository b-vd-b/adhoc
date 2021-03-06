package util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Variables {


    /**
     * The name of the program
     */
    public static final String PROGRAM_NAME = "Awesome ad hoc chat!";

    /**
     * The location in which file downloads should be saved.
     */
    public static final Path DOWNLOADS_DIRECTORY = Paths.get(System.getProperty("user.home")).resolve("adhoc-downloads");
    
    /**
     * This is the maximum file size allowed for file transfers used by GroupChatGUI and PrivateChatGUI.
     */
    public static final long MAXIMUM_FILE_SIZE = 500000000;

    /**
     * The maximum amount of times a packet will be resend when a destination is not longer known.
     */
    public static final int MAXIMUM_RETRANSMIT_ATTEMPTS = 5;

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
