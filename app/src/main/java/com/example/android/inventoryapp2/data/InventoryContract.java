package com.example.android.inventoryapp2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

//API Contract for the Inventory App
public class InventoryContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private InventoryContract(){}

    //The "Content authority" is to the content provider as domain name is to website.
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    //Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    //the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventory/products/ is a valid path for
     * looking at product data. content://com.example.android.inventory/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCTS = "products";

    /**
     * Inner class that defines the constant values for the inventory database table.
     * Each entry in the table represents a single product
    */
    public static final class InventoryEntry implements BaseColumns {

        //The content Uri to access the product data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        //The MIME type of the {@link #CONTENT_URI} for a list of products
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +"/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        //The MIME type of the {@link #CONTENT_URI} for a single product
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +PATH_PRODUCTS;

        //Name of the database table for products
        public static final String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";

        /**
         * Price of the product
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Quantity of the product on hand
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Name of the supplier
         *
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_NAME = "supplier_name";

        /**
         * Phone number of supplier
         *
         * Type: INTEGER
         */
        public final static String COLUMN_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";
    }
}
