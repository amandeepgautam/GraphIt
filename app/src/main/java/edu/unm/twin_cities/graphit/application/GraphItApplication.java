package edu.unm.twin_cities.graphit.application;

import android.app.Application;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResouceManager;
import lombok.Data;

/**
 * Created by aman on 20/10/15.
 */
@Data
public class GraphItApplication extends Application {
    private RemoteConnectionResouceManager connectionManager = new RemoteConnectionResouceManager();

}