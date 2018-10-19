package com.example.android.inventoryapp2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp2.data.InventoryContract;
import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound ) to the views yet.
     *
     * @param context   The app context
     * @param cursor    The cursor is already moved to the correct position
     * @param viewGroup The parent to which the new view is attached to
     * @return the newly created list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    /**
     * This method binds the product data to the given list item layout
     *
     * @param view    Existing view, returned earliet by newView() method
     * @param context The app context
     * @param cursor  The cursor is already moved to the correct row
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        //Find individual views that we want to modify in the list item layout
        TextView productNameTextView = (TextView) view.findViewById(R.id.text_view_product_name);
        TextView productPriceTextView = (TextView) view.findViewById(R.id.text_view_product_price);
        TextView productQuantityTextView = (TextView) view.findViewById(R.id.text_view_product_quantity);

        Button saleButton = (Button) view.findViewById(R.id.button_sale);

        //Find the columns of the product attributes
        int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        //Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(productNameColumnIndex);
        String productPrice = cursor.getString(productPriceColumnIndex);
        final String productQuantity = cursor.getString(productQuantityColumnIndex);

        //Update the TextViews with the attributes for the current product
        productNameTextView.setText(productName);
        productPriceTextView.setText(productPrice);
        productQuantityTextView.setText(productQuantity);

        //Get the current product URI
        int productIdColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int productId = cursor.getInt(productIdColumnIndex);
        final Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, productId);

        //Set onClick Listener on sale button
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int productQuantityInteger = Integer.parseInt(productQuantity);

                //Decrease the quantity of the product by 1
                if (productQuantityInteger > 0) {
                    productQuantityInteger -= 1;
                } else {
                    productQuantityInteger = 0;
                }
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantityInteger);

                //Pass the Content Resolver the updated product quantity
                int rowsAffected = context.getContentResolver()
                        .update(currentProductUri, values, null, null);

            }
        });
    }
}
