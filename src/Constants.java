public class Constants {
    /**
     * The base name of the remote object
     * Each Value Manager remote object's name will be: ValueManagerX, where X is the id of the remote object
     * passed as a main program argument to the {@link Site} and {@link ValueManager}
     */
    public static final String REMOTE_OBJ_NAME = "ValueManager";
    // host
    public static final String SERVER_HOST = "localhost";

    // commands entered by the user
    public static final char PRINT = 'P';
    public static final char WRITE = 'W';
    public static final char QUIT = 'Q';
    public static final char LOOKUP = 'L';

    // the message to be displayed if the user enters the wrong command
    public static final String UNKNOWN_COMMAND = "Unknown command";
}
