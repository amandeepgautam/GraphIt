package edu.unm.twin_cities.graphit.processor;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.unm.twin_cities.graphit.processor.dao.DeviceTypeDao;
import edu.unm.twin_cities.graphit.processor.model.DeviceType.DeviceTypeID;
import lombok.Getter;

/**
 * Created by aman on 10/8/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper databaseHelperInstance;
    private static final String DATABASE_NAME = "ExperimentData";
    private static final int DATABASE_VERSION = 2;

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
        final String CREATE_TABLE_DEVICE_TYPE = "CREATE TABLE " + Table.DEVICE_TYPE.getTableName() + "("
                + Fields.DEVICE_TYPE_ID.getFieldName() + " TEXT PRIMARY KEY, "
                + Fields.DEVICE_TYPE_LABEL.getFieldName() + " TEXT, "
                + Fields.DEVICE_TYPE_DESCRIPTION.getFieldName() + " TEXT" + ")";

        final String CREATE_TABLE_DEVICE_INFO = "CREATE TABLE " + Table.DEVICE_INFO.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + " TEXT PRIMARY KEY, "
                + Fields.DEVICE_LABEL.getFieldName() + " TEXT NOT NULL, "
                + Fields.DEVICE_DESCRIPTION.getFieldName() + " TEXT, "
                + Fields.DEVICE_TYPE_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.DEVICE_TYPE.getTableName() + "("
                + Fields.DEVICE_TYPE_ID.getFieldName() + "), "
                + Fields.CREATED_AT.getFieldName() + " INTEGER NOT NULL" + ")";

        final String CREATE_TABLE_DEVICE_READING = "CREATE TABLE " + Table.DEVICE_READING.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + " TEXT NOT NULL REFERENCES " + Table.DEVICE_INFO.getTableName() + "("
                + Fields.DEVICE_ID.getFieldName() + "), "
                + Fields.READING.getFieldName() + " REAL NOT NULL, "
                + Fields.MEASUREMENT_UNIT.getFieldName() + " TEXT, "
                + Fields.TIMESTAMP.getFieldName() + " INTEGER NOT NULL, "
                + "PRIMARY KEY (" + Fields.DEVICE_ID.getFieldName() + ", "
                + Fields.TIMESTAMP.getFieldName() + ") )";

        try {
            db.execSQL(CREATE_TABLE_DEVICE_TYPE);
            Log.i(TAG, "Created table: " + Table.DEVICE_TYPE.getTableName());

            db.execSQL("INSERT INTO " + Table.DEVICE_TYPE.getTableName() + "(" + Fields.DEVICE_TYPE_ID.getFieldName() + ", " + Fields.DEVICE_TYPE_DESCRIPTION.getFieldName() +
                    ") VALUES ('" + DeviceTypeID.DEFAULT.name() + "', 'Device type for devices with no specified device types')");
            Log.i(TAG, "Inserted a record for default device type, for the " +
                    "case where sensors does not have any predefined one form User.");

            db.execSQL(CREATE_TABLE_DEVICE_INFO);
            Log.i(TAG, "Created table: " + Table.DEVICE_INFO.getTableName());
            db.execSQL(CREATE_TABLE_DEVICE_READING);
            Log.i(TAG, "Created table: " + Table.DEVICE_READING.getTableName());
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
        DEVICE_INFO("device_info"),
        DEVICE_TYPE("device_type"),
        DEVICE_READING("device_reading");

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
        DEVICE_TYPE_LABEL("device_type_label"),
        DEVICE_TYPE_ID("device_type_id"),
        DEVICE_TYPE_DESCRIPTION("device_type_description"),
        DEVICE_ID("device_id"),
        DEVICE_LABEL("device_label"),
        DEVICE_DESCRIPTION("device_description"),
        CREATED_AT("created_at"),
        READING_ID("reading_id"),
        READING("reading"),
        TIMESTAMP("timestamp"),
        MEASUREMENT_UNIT("measurement_unit");

        @Getter
        private final String fieldName;

        Fields(final String fieldName) {
            this.fieldName = fieldName;
        }
    }
}
