package edu.unm.twin_cities.graphit.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

import edu.unm.twin_cities.graphit.processor.dao.SensorTypeDao;
import edu.unm.twin_cities.graphit.processor.model.SensorType;
import edu.unm.twin_cities.graphit.processor.model.SensorType.SensorTypeID;
import lombok.Data;
import lombok.Getter;

/**
 * Created by aman on 10/8/15.
 */
@Data
public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper databaseHelperInstance;
    private static final String DATABASE_NAME = "ExperimentData";
    private static final int DATABASE_VERSION = 14;

    private Context context = null;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static DatabaseHelper getInstance(Context context) {
        if(databaseHelperInstance == null) {
            synchronized (DatabaseHelper.class) {
                if(databaseHelperInstance == null) {
                    databaseHelperInstance = new DatabaseHelper(context);
                }
            }
        }
        return databaseHelperInstance;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        final String CREATE_TABLE_SENSOR_TYPE = "CREATE TABLE " + Table.SENSOR_TYPE.getTableName() + "("
                + Fields.SENSOR_TYPE_ID.getFieldName() + " TEXT PRIMARY KEY, "
                + Fields.SENSOR_TYPE_LABEL.getFieldName() + " TEXT, "
                + Fields.SENSOR_TYPE_DESCRIPTION.getFieldName() + " TEXT,"
                + Fields.MEASUREMENT_UNIT.getFieldName() + " TEXT NOT NULL)";

        final String CREATE_TABLE_DEVICE = "CREATE TABLE " + Table.DEVICE.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + " TEXT  PRIMARY KEY, "
                + Fields.DEVICE_LABEL.getFieldName() + " TEXT)";

        final String CREATE_TABLE_SENSOR = "CREATE TABLE " + Table.SENSOR.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.DEVICE.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + "), "
                + Fields.SENSOR_ID.getFieldName() + " TEXT, "
                + Fields.SENSOR_LABEL.getFieldName() + " TEXT NOT NULL, "
                + Fields.SENSOR_DESCRIPTION.getFieldName() + " TEXT, "
                + Fields.SENSOR_DATA_FILE_PATH.getFieldName() + " TEXT NOT NULL, "
                + Fields.SENSOR_TYPE_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.SENSOR_TYPE.getTableName() + "("
                + Fields.SENSOR_TYPE_ID.getFieldName() + "), "
                + Fields.CREATED_AT.getFieldName() + " INTEGER NOT NULL, "
                + "PRIMARY KEY (" + Fields.DEVICE_ID.getFieldName() + ","
                + Fields.SENSOR_ID.getFieldName() + ") )";

        final String CREATE_TABLE_READING = "CREATE TABLE " + Table.READING.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.DEVICE.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + "), "
                + Fields.SENSOR_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.SENSOR.getTableName() + "("
                + Fields.SENSOR_ID.getFieldName() + "), "
                + Fields.READING.getFieldName() + " REAL NOT NULL, "
                + Fields.MEASUREMENT_UNIT.getFieldName() + " TEXT, "
                + Fields.TIMESTAMP.getFieldName() + " INTEGER NOT NULL, "
                + "PRIMARY KEY (" + Fields.DEVICE_ID.getFieldName() + ","
                + Fields.SENSOR_ID.getFieldName() + ", "
                + Fields.TIMESTAMP.getFieldName() + ") )";

        final String CREATE_TABLE_DEVICE_SENSOR_MAP = "CREATE TABLE " + Table.DEVICE_SENSOR.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.DEVICE.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + "), "
                + Fields.SENSOR_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.SENSOR.getTableName() + "("
                + Fields.SENSOR_ID.getFieldName() + "), "
                + Fields.SENSOR_FILE_LOC.getFieldName() + " TEXT, "
                + "PRIMARY KEY (" + Fields.DEVICE_ID.getFieldName() + ", "
                + Fields.SENSOR_ID.getFieldName() + ") )";

        final String CREATE_TABLE_USER_PREFERENCE = "CREATE TABLE " + Table.USER_PREF.getTableName() + "("
                + Fields.USER_PREF_KEY.getFieldName() + " TEXT PRIMARY KEY, "
                + Fields.USER_PREF_VALUE.getFieldName() + " TEXT NOT NULL" + ")";

        try {
            db.execSQL(CREATE_TABLE_SENSOR_TYPE);
            Log.i(TAG, "Created table: " + Table.SENSOR_TYPE.getTableName());

            //Provide bootstrap values.

            //TODO: revist behaviour of functions
            SensorTypeDao sensorTypeDao = new SensorTypeDao(context);
            List<ContentValues> sensorTypes = sensorTypeDao.initializeSensorType();
            for (ContentValues sensorType : sensorTypes) {
                db.insertWithOnConflict(Table.SENSOR_TYPE.getTableName(), null,
                        sensorType, SQLiteDatabase.CONFLICT_IGNORE);
            }

            Log.i(TAG, "Inserted a record for default device type, for the " +
                    "case where sensors does not have any predefined one form User.");

            db.execSQL(CREATE_TABLE_SENSOR);
            Log.i(TAG, "Created table: " + Table.SENSOR.getTableName());

            db.execSQL(CREATE_TABLE_READING);
            Log.i(TAG, "Created table: " + Table.READING.getTableName());

            db.execSQL(CREATE_TABLE_DEVICE);
            Log.i(TAG, "Created table: " + Table.DEVICE.getTableName());

            db.execSQL(CREATE_TABLE_DEVICE_SENSOR_MAP);
            Log.i(TAG, "Created table: " + Table.DEVICE_SENSOR.getTableName());

            db.execSQL(CREATE_TABLE_USER_PREFERENCE);
            Log.i(TAG, "Created table: " + Table.USER_PREF.getTableName());
        } catch (SQLException sqle) {
            Log.e(TAG, "Error in database creation", sqle);
            //TODO: Do something with the exception.
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        for(Table table : Table.values()) {
            db.execSQL("DROP TABLE IF EXISTS " + table.getTableName());
            Log.i(TAG, "Dropped Table: " + table.getTableName());
        }
    }

    /**
     * Enum containing all the table names for the application.
     */
    public enum Table {
        /** Sensor instance.**/
        SENSOR("sensor"),
        /** Store for type of sensor which could be available.**/
        SENSOR_TYPE("sensor_type"),
        /** A store the readings which have been collected. **/
        READING("reading"),
        /** Store for devices that host the sensors.**/
        DEVICE("device"),
        DEVICE_SENSOR("device_sensor"),
        /** A key value table for storing user based preference.**/
        USER_PREF("user_preference");

        @Getter
        private final String tableName;

        Table(final String tableName) {
            this.tableName  = tableName;
        }
    }

    /**
     * Enum containing the field names for all tables.
     */
    public enum Fields {
        SENSOR_TYPE_LABEL("sensor_type_label"),
        SENSOR_TYPE_ID("sensor_type_id"),
        SENSOR_TYPE_DESCRIPTION("sensor_type_description"),
        SENSOR_ID("sensor_id"),
        SENSOR_LABEL("sensor_label"),
        SENSOR_DESCRIPTION("sensor_description"),
        CREATED_AT("created_at"),
        SENSOR_DATA_FILE_PATH("sensor_data_file_path"),
        READING("reading"),
        TIMESTAMP("timestamp"),
        MEASUREMENT_UNIT("measurement_unit"),
        DEVICE_ID("device_id"),
        DEVICE_LABEL("device_label"),
        SENSOR_FILE_LOC("sensor_file_location"),
        USER_PREF_KEY("key"),
        USER_PREF_VALUE("value");

        @Getter
        private final String fieldName;

        Fields(final String fieldName) {
            this.fieldName = fieldName;
        }
    }
}
