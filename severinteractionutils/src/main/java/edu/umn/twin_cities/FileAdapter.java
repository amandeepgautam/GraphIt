package edu.umn.twin_cities;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by aman on 18/9/15.
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
     * If the resource is a file
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
         * This will be promary due to permission conflicts.
         */
        UNKNOWN;
    }
}
