package com.example.android.inventoryapp2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

//Manages database creation and version management
public class InventoryDbHelper extends SQLiteOpenHelper {

    //Name of the database file
    private static final String DATABASE_NAME = "inventory.db";

    //Database version. If you change the database schema, you must change the database version.
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create a String that contains the SQL statement to create the inventory table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + "("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_SUPPLIER_NAME + " TEXT, "
                + InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT);";

        //Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    //This is called when the database needs to be updated
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //The database is still at version 1, so there's nothing to do here
    }
}
