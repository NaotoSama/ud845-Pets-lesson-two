/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 * Set the CatalogActivity to implement the LoaderManager interface to use loaders to automatically
 * store and update cursor data in the background instead of the main thread to prevent app hangs.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> { // <Cursor> means telling to the loader to return a cursor

    // To initialize a loader, first make an integer constant loader called "PET_LOADER"
    private static final int PET_LOADER = 0;  // "0" is just an arbitrary choice. It's ok to set it up as any other integer.

    // Since further down we'll be using an adapter for all callback methods, create an instance of that class.
    PetCursorAdapter mCursorAdapter;  // This will be the adapter for the list view.

    private Cursor data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        // Find the ListView which will be populated with the pet data
        ListView petListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);


        // Set up an adapter to create a list item for each row of pet data in the cursor.
        // There is no pet data yet until the loader finishes, so pass in null for the cursor.
        mCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        // Start the loader.
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }


    /**
     * Helper method to insert "hardcoded pet data" into the database. For debugging purposes only.
     *
     * In the CatalogActivity, when a user clicks on the “Insert Dummy Pet” menu item,
     * we will call the ContentResolver insert() method with the pet content URI and a ContentValues object (containing info about Toto).
     * This insertPet() method is called from the onOptionsItemSelected() method when the menu item is clicked on.
     */
    private void insertPet() { //此method純粹是測試用，看看用戶按“Insert Dummy Pet”時會不會正常輸入ToTo狗的hardcoded資訊。
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);
        //put的意思是先配對欄位和數值

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
        //insert的是意思是把配對的欄位和數值植入表格
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This creates options menu in the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED };

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,    // Return a new CursorLoader
                PetEntry.CONTENT_URI,   //The ContentURI of the words table
                projection,             //The columns to return for each row
                null,          //Selection criteria
                null,       //Selection criteria
                null);         //The sort order for the returned row
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update PetCursorAdapter with the new cursor containing updated pet data.
        mCursorAdapter.swapCursor(data); //Swap for new cursor
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // The callback called when the data needs to be deleted.
        mCursorAdapter.swapCursor(null);
    }

}
