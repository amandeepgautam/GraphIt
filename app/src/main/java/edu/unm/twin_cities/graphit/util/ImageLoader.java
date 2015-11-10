package edu.unm.twin_cities.graphit.util;

import edu.umn.twin_cities.FileAdapter.ResourceType;
import edu.unm.twin_cities.graphit.R;

/**
 *
 */
public class ImageLoader {

    /**
     * Get the icon for the type of file. Returns -1, if no valid image is found.
     * @param resourceType type of resource..
     * @return a integer identifier for the image resouce of -1.
     */
    static public Integer getFileTypeIcon(ResourceType resourceType) {
        if (resourceType == ResourceType.DIRECTORY)
            return R.drawable.folder;
        else if (resourceType == ResourceType.FILE)
            return R.drawable.file;
        else if (resourceType == ResourceType.UNKNOWN)
            return R.drawable.unknown;
        return -1;
    }
}
