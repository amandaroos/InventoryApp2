package com.example.android.inventoryapp2;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    private static final int PRODUCT_DETAIL_LOADER = 0;

    private Uri currentProductURi;

    private EditText mProductNameEditText;
    private EditText mProductPriceEditText;
    private EditText mProductQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNumberEditText;

    private Button mIncreaseInventoryButton;
    private Button mDecreaseInventoryButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        currentProductURi = intent.getData();

        if (currentProductURi == null) {
            //This is a new product
            setTitle(R.string.detail_activity_label_add_product);
        } else {
            setTitle(R.string.detail_activity_label_edit_product);
        }

        //Find all relevant views
        mProductNameEditText = (EditText) findViewById(R.id.edit_text_product_name);
        mProductPriceEditText = (EditText) findViewById(R.id.edit_text_product_price);
        mProductQuantityEditText = (EditText) findViewById(R.id.edit_text_product_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_text_product_supplier_name);
        mSupplierPhoneNumberEditText = (EditText) findViewById(R.id.edit_text_product_supplier_phone_number);

        mIncreaseInventoryButton = (Button) findViewById(R.id.button_increase_inventory);
        mDecreaseInventoryButton = (Button) findViewById(R.id.button_decrease_inventory);

        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);
        mProductQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);

        //Set default values
        mProductPriceEditText.setText("0");
        mProductQuantityEditText.setText("0");

        mIncreaseInventoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean increase = true;
                changeProductQuantity(increase);
            }
        });

        mDecreaseInventoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If increase is false, the quantity will decrease
                boolean increase = false;
                changeProductQuantity(increase);
            }
        });
    }

    public void changeProductQuantity(boolean increase) {

        int productQuantityInteger = Integer.parseInt(mProductQuantityEditText.getText().toString());

        //if increase is true, increase quantity
        //otherwise, if it is greater than 0, decrease the quantity
        if (increase) {
            productQuantityInteger += 1;
        } else {
            if (productQuantityInteger > 0) {
                productQuantityInteger -= 1;
            }
        }

        mProductQuantityEditText.setText(String.valueOf(productQuantityInteger));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                //Open dialog to warn user of the deletion
                //TODO showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                //TODO showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Get user input from detail activity and update the product in database
    private void saveProduct() {
        //Get product data from user input
        String productNameString = mProductNameEditText.getText().toString().trim();
        String productPriceString = mProductPriceEditText.getText().toString().trim();
        String productQuantityString = mProductQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();

        //Prevent crash when saving a blank detail page
        //Check if this is supposed to be a new product and check if all the fields are blank
        if (currentProductURi == null &&
                TextUtils.isEmpty(productNameString) &&
                TextUtils.isEmpty(productPriceString) &&
                TextUtils.isEmpty(productQuantityString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneNumberString)) {
            //Since no fields were modified, we can return early without creating a new product.
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceString);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantityString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString);

        if (currentProductURi != null) {
            //pass the update product information to the content resolver
            int rowsAffected = getContentResolver().update(currentProductURi, values, null, null);

            //Show a toast message depending on whether or not hte update was successful
            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.detail_activity_update_product_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.detail_activity_update_product_successful,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            //Insert the new row using the InventoryProvider
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, R.string.detail_activity_insert_product_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.detail_activity_insert_product_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Define a projection that specifies the columns from the table we care about
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader(this,
                currentProductURi,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Check to see if the Cursor is empty
        if (cursor.getCount() > 0) {
            //Set Cursor to the 0th position
            cursor.moveToFirst();

            //Find the columns of the product attributes
            int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int suppliePhoneNumberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            //Read the product attributes from the Cursor for the current product
            String productName = cursor.getString(productNameColumnIndex);
            String productPrice = cursor.getString(productPriceColumnIndex);
            String productQuantity = cursor.getString(productQuantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = cursor.getString(suppliePhoneNumberColumnIndex);

            //Populate views with extracted data
            mProductNameEditText.setText(productName);
            mProductPriceEditText.setText(productPrice);
            mProductQuantityEditText.setText(productQuantity);
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(supplierPhoneNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Set each attribute to an empty string
        mProductNameEditText.setText("");
        mProductPriceEditText.setText("");
        mProductQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneNumberEditText.setText("");
    }
}
