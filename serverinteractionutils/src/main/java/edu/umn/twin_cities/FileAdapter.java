package edu.umn.twin_cities;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Java native File has lot of dynamic calculation based on the operating
 * system, which in this case changes as file object is transferred ove
 * the network. This class encapsulates the data useful in application
 * context from a java File object for transfer over the network.
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
public class FileAdapter implements Serializable {

    /**
     * Name of the resource.
     */
    private String name;

    /**
     * Path of the resource.
     */
    private String path;

    /**
     * The last time resource was modified.
     */
    private long lastModified;

    /**
     * Type of resource.
     */
    private ResourceType resourceType;

    public enum ResourceType {
        /**
         * Resource is a directory.
         */
        DIRECTORY,
        /**
         * Resource is abstract representation of File.
         */
        FILE,
        /**
         * Captures the fact that File.canRead() returns false.
         * This will be primarily due to permission conflicts.
         */
        UNKNOWN;
    }
}
