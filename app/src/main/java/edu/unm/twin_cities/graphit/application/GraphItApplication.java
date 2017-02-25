package edu.unm.twin_cities.graphit.application;

import android.app.Application;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResourceManager;
import lombok.Data;

/**
 * Created by aman on 20/10/15.
 */
@Data
public class GraphItApplication extends Application {
    private RemoteConnectionResourceManager connectionManager = new RemoteConnectionResourceManager();

}