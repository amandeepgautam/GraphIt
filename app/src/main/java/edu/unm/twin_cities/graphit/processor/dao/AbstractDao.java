package edu.unm.twin_cities.graphit.processor.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import lombok.Data;

/**
 * Created by aman on 19/8/15.
 */
@Data
public abstract class AbstractDao {

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase sqLiteDatabase = null;


    /**
     * Tag string for logging purposes.
     */
    private final String tag;

    private final String tableName;


    AbstractDao(final Context context, String tag, String tableName) {
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.tag = tag;
        this.tableName = tableName;
    }

    public void open() throws SQLiteException {
        this.sqLiteDatabase = databaseHelper.getWritableDatabase();
    }

    public void close() {
        this.databaseHelper.close();
    }

    public SQLiteDatabase getSqLiteDatabase() {
        return sqLiteDatabase;
    }
}
