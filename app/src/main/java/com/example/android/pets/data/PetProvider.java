package com.example.android.pets.data;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;  //If this is the first Log statement you’ve added to the provider, make sure you add this import statement to the top of your provider file, so it knows what Log class you’re referring to.

import java.net.URI;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    /** URI matcher code for the content URI for the whole pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet (a single row) in the pets table */
    private static final int PET_ID = 101;

    /**
     * Create a UriMatcher object to match a content URI to the above corresponding matcher codes.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     * UriMatcher的存在目的是建立一套URI格式，合乎格式的URI才會被處理
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH); // The "s" from sUriMatcher denotes that this is a static object.

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider.
        // All paths added to the UriMatcher have a corresponding code to return when a match is found.

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS); //括號中的項目分別為(content authority, 表格名稱,整個表格的matcher code)
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID); //(content authority, 列的名稱,列的matcher code), #字號是integer wild card
    }


    /** Tag for the log messages */
    //Since we’ll be logging multiple times throughout this file,
    // it would be ideal to create a log tag as a global constant variable,
    // so all log messages from the PetProvider will have the same log tag identifier when you are reading the system logs.
    public static final String LOG_TAG = PetProvider.class.getSimpleName();


    /** Database helper object */
    private PetDbHelper mDbHelper;


    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Create and initialize a PetDbHelper object to gain access to the pets database.
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }


    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor= null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(
                        PetContract.PetEntry.TABLE_NAME,   // The table to query
                        projection,            // The columns to return. If we write "null" here, then all the columns will be selected by default
                        selection,                  // The columns for the WHERE clause
                        selectionArgs,                  // The values for the WHERE clause
                        null,                  // Don't group the rows
                        null,                  // Don't filter by row groups
                        sortOrder);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set Notification URL on the cursor so we know what content URI the cursor was created for.
        // If the data at this URL changes, then we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:  //Only the PETS case is supported for insertion because it doesn't make sense to insert a new method into a single row where a pet already exists. So we'll only be inserting a new pet in the pets case because we're performing this operation on the whole table.
                return insertPet(uri, contentValues); //Within the PETS case, call the insertPet helper method 
            default:    //Any other match, or perhaps no match, will just fall into the default case, and an exception will be thrown.
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /** This is a helper method for the insert method above.
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {    //Insert a new pet into the pets database table with the given ContentValues
        SQLiteDatabase database = mDbHelper.getWritableDatabase(); // Get writeable database  //Should it be a readable or writeable database? Well, we are editing the data source by adding a new pet, so we need to write changes to the database.

        // Insert the new pet with the given values
        // Once we have a writeable database object, we can call the insert() method on it, passing in the pet table name and the ContentValues object.
        // The return value is the ID of the new row that was just created, in the form of a long data type (which can store numbers larger than the int data type).
        long id = database.insert(PetContract.PetEntry.TABLE_NAME, null, values);

        // Based on the ID, we can determine if the database operation went smoothly or not.
        // If the ID is equal to -1, then we know the insertion failed. Otherwise, the insertion was successful.
        // Hence, we add this check in the code. If the insertion failed, we log an error message using Log.e() and also return a null URI.
        // That way, if a class tries to insert a pet, but receives a null URI, they’ll know that something went wrong.
        if (id == -1) {                                               // If the ID is -1, then the insertion failed.
            Log.e(LOG_TAG, "Failed to insert row for " + uri);   // Log an error,
            return null;                                              // and return null.

        // If the insertion was successful, then we can add the row ID to the end of the pet URI
        // (using the ContentUris.withAppendedId() method) to create a pet URI specific for the new pet, and have it returned.
        return ContentUris.withAppendedId(uri, id); // Once we know the ID of the new row in the table, return the new URI with the ID appended to the end of it
    }


    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return 0;
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }


    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }


    /**
     * The purpose of this method is to return a String that describes the type of the data stored at the input Uri.
     * This String is known as the MIME type, which can also be referred to as content type.
     * One use case where this functionality is important is if you’re sending an intent with a URI set on the data field.
     * The Android system will check the MIME type of that URI to determine which app component on the device can best handle your request.
     * (If the URI happens to be a content URI, then the system will check with the corresponding ContentProvider to ask for the MIME type using the getType() method.)
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetContract.PetEntry.CONTENT_MIME_DIRECTORY_TYPE;
                //“content://com.example.android.pets/pets/”, which is the PETS case, which references the entire pets table.
                // Basically it represents a list of pets. In MIME type terms, this is known as a directory of data.
            case PET_ID:
                return PetContract.PetEntry.CONTENT_MIME_ITEM_TYPE;
                //“content://com.example.android.pets/pets/#”, which is the PETS_ID case,  which represents a single pet.
                // In MIME type terms, a single row of data is an item of data.
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}