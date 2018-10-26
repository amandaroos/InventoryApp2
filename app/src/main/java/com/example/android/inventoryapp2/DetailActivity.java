package com.example.android.inventoryapp2;

import android.app.AlertDialog;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private boolean mIgnoreNextTextChange = false;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    private static final int PRODUCT_DETAIL_LOADER = 0;

    private Uri mCurrentProductURi;

    private EditText mProductNameEditText;
    private EditText mProductPriceEditText;
    private EditText mProductQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNumberEditText;

    private Button mIncreaseInventoryButton;
    private Button mDecreaseInventoryButton;
    private Button mCallSupplierButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentProductURi = intent.getData();

        if (mCurrentProductURi == null) {
            //This is a new product
            setTitle(R.string.detail_activity_label_add_product);
        } else {
            setTitle(R.string.detail_activity_label_edit_product);

            getLoaderManager().initLoader(PRODUCT_DETAIL_LOADER, null, this);
        }

        //Find all relevant views
        mProductNameEditText = (EditText) findViewById(R.id.edit_text_product_name);
        mProductPriceEditText = (EditText) findViewById(R.id.edit_text_product_price);
        mProductQuantityEditText = (EditText) findViewById(R.id.edit_text_product_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_text_product_supplier_name);
        mSupplierPhoneNumberEditText = (EditText) findViewById(R.id.edit_text_product_supplier_phone_number);

        mIncreaseInventoryButton = (Button) findViewById(R.id.button_increase_inventory);
        mDecreaseInventoryButton = (Button) findViewById(R.id.button_decrease_inventory);
        mCallSupplierButton = (Button) findViewById(R.id.button_call_supplier);

        //Set OnTouchListeners on EditText fields
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);
        mProductQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);

        //Set default value
        mProductPriceEditText.setText(R.string.zeroCurrency);

        //Set on click listeners
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

        mCallSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callSupplier();
            }
        });
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
                if (isValidData() == 1) {
                    //save the new product information entered by the user
                    saveProduct();
                    finish();
                }
                //When isValidData() returns 0, do nothing. isValidData() displays a toast prompting
                //the user to enter product name
                //When isValidData() returns -1, do nothing. The functions displays a toast prompting
                //the user to enter produt details
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
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
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                mCurrentProductURi,
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


            //Format the price
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            productPrice = formatter.format(Double.parseDouble(productPrice) / 100);

            //Populate views with extracted data
            mProductNameEditText.setText(productName);
            mProductPriceEditText.setText(productPrice);
            mProductQuantityEditText.setText(productQuantity);
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(supplierPhoneNumber);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        //Listen for changes to price text so the formatting can be updated to match the changes
        mProductPriceEditText.addTextChangedListener(priceWatcher);
        mProductPriceEditText.setSelection(mProductPriceEditText.getText().length());
        //Listen quantity text changes so the user can be warned when the quantity is too large
        mProductQuantityEditText.addTextChangedListener(quantityWatcher);
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        //Prevent the TextWatchers from being triggered when no longer needed
        mProductPriceEditText.removeTextChangedListener(priceWatcher);
        mProductQuantityEditText.removeTextChangedListener(quantityWatcher);
        super.onDestroy();
    }

    private TextWatcher priceWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (mIgnoreNextTextChange) {
                mIgnoreNextTextChange = false;
                return;
            } else {
                mIgnoreNextTextChange = true;
            }

            try {
                String totalStr = mProductPriceEditText.getText().toString();
                totalStr = totalStr.replaceAll("[$.,]", "");
                totalStr = getResources().getString(R.string.currency_symbol)
                        + String.format(Locale.getDefault(), "%.2f", Integer.parseInt(totalStr) / 100.00);
                mProductPriceEditText.setText(totalStr);
                mProductPriceEditText.setSelection(mProductPriceEditText.getText().length());
            } catch (NumberFormatException e) {
                Toast.makeText(getBaseContext(), R.string.large_number_error_toast, Toast.LENGTH_LONG).show();
                mProductPriceEditText.setText(R.string.zeroCurrency);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private TextWatcher quantityWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (mIgnoreNextTextChange) {
                mIgnoreNextTextChange = false;
                return;
            } else {
                mIgnoreNextTextChange = true;
            }

            try {
                String totalStr = mProductQuantityEditText.getText().toString();
                totalStr = String.valueOf(Integer.parseInt(totalStr));
                mProductQuantityEditText.setSelection(mProductQuantityEditText.getText().length());
            } catch (NumberFormatException e) {
                Toast.makeText(getBaseContext(), R.string.large_number_error_toast, Toast.LENGTH_LONG).show();
                mProductQuantityEditText.setText(R.string.detail_activity_default_quantity);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Set each attribute to an empty string
        mProductNameEditText.setText("");
        mProductPriceEditText.setText("");
        mProductQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneNumberEditText.setText("");
    }

    @Override
    public void onBackPressed() {
        //If product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Use clicked the "Discard" button, close the current activity
                        finish();
                        ;
                    }
                };
        //Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void changeProductQuantity(boolean increase) {

        String quantityString = mProductQuantityEditText.getText().toString();
        int productQuantityInteger;

        if (quantityString.isEmpty()){
            productQuantityInteger = 0;
        } else {
         productQuantityInteger =Integer.parseInt(quantityString);
        }
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

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * @return int -1 if ok to finish activity without saving, 0 if product name is missing, and
     * 1 if ok to save and finish activity
     */
    public int isValidData() {

        String productNameString = mProductNameEditText.getText().toString().trim();
        String productQuantityString = mProductQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();

        //Clean up the price String
        String productPriceString = mProductPriceEditText.getText().toString().trim()
                .replaceAll("[$,.]", "");

        //Check if this is supposed to be a new product and check if all the fields are blank
        //or if the fields ar equal to their default values
        if (mCurrentProductURi == null &&
                TextUtils.isEmpty(productNameString) ||
                productPriceString.equals("000") ||
                productQuantityString.isEmpty() ||
                TextUtils.isEmpty(supplierNameString) ||
                TextUtils.isEmpty(supplierPhoneNumberString)) {

            Toast.makeText(getBaseContext(), R.string.detail_activity_empty_field_warning,
                    Toast.LENGTH_SHORT).show();

            return -1;
        } else if (productPriceString.equals("000") &&
                productQuantityString.equals("0") &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneNumberString)) {
            //Product details are required
            Toast.makeText(getBaseContext(), R.string.detail_activity_product_name_required,
                    Toast.LENGTH_SHORT).show();
            return 0;
        }

        //Product is ok to save
        return 1;
    }

    //Get user input from detail activity and update the product in database
    private void saveProduct() {
        //Get product data from user input
        String productNameString = mProductNameEditText.getText().toString().trim();
        String productQuantityString = mProductQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();

        //Clean up the price String
        String productPriceString = mProductPriceEditText.getText().toString().trim()
                .replaceAll("[$,.]", "");

        //Set default Quantity
        if (productQuantityString.isEmpty()){
            productQuantityString = String.valueOf(R.string.detail_activity_default_quantity);
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceString);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantityString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString);

        if (mCurrentProductURi != null) {
            //pass the update product information to the content resolver
            int rowsAffected = getContentResolver().update(mCurrentProductURi, values, null, null);

            //Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.detail_activity_update_product_failed,
                        Toast.LENGTH_SHORT).show();
            } else if (mProductHasChanged){
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

    public void deleteProduct() {
        //Only preform the delete if this is an existing product
        if (mCurrentProductURi != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductURi, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.detail_activity_delete_product_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.detail_activity_delete_product_successful,
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    public void callSupplier() {
        String phoneNumber = mSupplierPhoneNumberEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel: " + phoneNumber));
        startActivity(intent);
    }
}