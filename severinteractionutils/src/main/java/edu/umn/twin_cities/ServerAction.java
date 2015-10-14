package edu.umn.twin_cities;

/**
 * Server action are the actions to which server can respond to. These
 * need to be explicitly implemented in the server.
 */
public enum ServerAction {
    /**
     * List the files in the current directory.
     */
    LIST_FILES_IN_DIR,
    /**
     * Transfer the specified file.
     */
    TRANSFER_FILE,
    /**
     * Change directory to the specified directory.
     */
    CHANGE_DIRECTORY,
    /**
     * Close the connection.
     */
    EXIT;
}
