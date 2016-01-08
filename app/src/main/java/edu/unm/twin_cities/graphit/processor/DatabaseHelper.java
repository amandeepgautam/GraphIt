package edu.unm.twin_cities.graphit.processor;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.unm.twin_cities.graphit.processor.model.SensorType.SensorTypeID;
import lombok.Getter;

/**
 * Created by aman on 10/8/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper databaseHelperInstance;
    private static final String DATABASE_NAME = "ExperimentData";
    private static final int DATABASE_VERSION = 4;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
                + Fields.SENSOR_TYPE_DESCRIPTION.getFieldName() + " TEXT" + ")";

        final String CREATE_TABLE_DEVICE = "CREATE TABLE " + Table.DEVICE.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + " TEXT  PRIMARY KEY, "
                + Fields.DEVICE_LABEL.getFieldName() + " TEXT)";

        final String CREATE_TABLE_SENSOR = "CREATE TABLE " + Table.SENSOR.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + " TEXT REFERENCES " + Table.DEVICE.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + "), "
                + Fields.SENSOR_ID.getFieldName() + " TEXT, "
                + Fields.SENSOR_LABEL.getFieldName() + " TEXT NOT NULL, "
                + Fields.SENSOR_DESCRIPTION.getFieldName() + " TEXT, "
                + Fields.SENSOR_TYPE_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.SENSOR_TYPE.getTableName() + "("
                + Fields.SENSOR_TYPE_ID.getFieldName() + "), "
                + Fields.CREATED_AT.getFieldName() + " INTEGER NOT NULL, "
                + "PRIMARY KEY (" + Fields.DEVICE_ID.getFieldName() + ","
                + Fields.SENSOR_ID.getFieldName() + ") )";

        final String CREATE_TABLE_READING = "CREATE TABLE " + Table.READING.getTableName() + "("
                + Fields.SENSOR_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.SENSOR.getTableName() + "("
                + Fields.SENSOR_ID.getFieldName() + "), "
                + Fields.READING.getFieldName() + " REAL NOT NULL, "
                + Fields.MEASUREMENT_UNIT.getFieldName() + " TEXT, "
                + Fields.TIMESTAMP.getFieldName() + " INTEGER NOT NULL, "
                + "PRIMARY KEY (" + Fields.SENSOR_ID.getFieldName() + ", "
                + Fields.TIMESTAMP.getFieldName() + ") )";

        final String CREATE_TABLE_DEVICE_SENSOR_MAP = "CREATE TABLE " + Table.DEVICE_SENSOR.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.DEVICE.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + "), "
                + Fields.SENSOR_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.SENSOR.getTableName() + "("
                + Fields.SENSOR_ID.getFieldName() + "), "
                + Fields.SENSOR_FILE_LOC.getFieldName() + " TEXT, "
                + "PRIMARY KEY (" + Fields.DEVICE_ID.getFieldName() + ", "
                + Fields.SENSOR_ID.getFieldName() + ") )";

        try {
            db.execSQL(CREATE_TABLE_SENSOR_TYPE);
            Log.i(TAG, "Created table: " + Table.SENSOR_TYPE.getTableName());

            db.execSQL("INSERT INTO " + Table.SENSOR_TYPE.getTableName() + "(" + Fields.SENSOR_TYPE_ID.getFieldName() + ", " + Fields.SENSOR_TYPE_DESCRIPTION.getFieldName() +
                    ") VALUES ('" + SensorTypeID.DEFAULT.name() + "', 'Device type for devices with no specified device types')");
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
        SENSOR("sensor"),
        SENSOR_TYPE("sensor_type"),
        READING("reading"),
        DEVICE("device"),
        DEVICE_SENSOR("device_sensor");

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
        READING("reading"),
        TIMESTAMP("timestamp"),
        MEASUREMENT_UNIT("measurement_unit"),
        DEVICE_ID("device_id"),
        DEVICE_LABEL("device_label"),
        SENSOR_FILE_LOC("sensor_file_location");

        @Getter
        private final String fieldName;

        Fields(final String fieldName) {
            this.fieldName = fieldName;
        }
    }
}
