package edu.umn.twin_cities;

import lombok.Getter;

/**
 * A class enumerating the errors which can happen during server client
 * interaction.
 */
public enum ErrorCode {
    /**
     * Server sends back this code when user tries to browse a file. Ideally
     * a file should be requested for transfer.
     */
    NOT_A_DIR("The resource specified is not a directory."),
    /**
     * Server sends back the the code when the requested path does not exists.
     */
    RESOURCE_NOT_EXISTS("The resource does not exist."),
    /**
     * Server sends back this code when the requested resource is not a file.
     */
    NOT_A_FILE("The resource is not a file."),
    /**
     * Server sends when MD5 computation fails.
     */
    MD5_ERROR("Unable to compute MD5 "),
    /**
     * Unable to fetch address from the remote device.
     */
    MAC_ADDRESS_UNAVILABLE("Unable to fetch Mac Address from the device");

    @Getter
    private String errorMsg;

    ErrorCode(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
