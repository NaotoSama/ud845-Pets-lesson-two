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
package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Database helper for Pets app. Manages database creation and version management.
 */
public class PetDbHelper extends SQLiteOpenHelper {  // Create a class that extends from SQLiteOpenHelper

    public static final String LOG_TAG = PetDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "shelter.db";  // Create a constant for database name

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1; // Create a constant for database version


    /**
     * Constructs a new instance of {@link PetDbHelper}.
     *
     * @param context of the app
     */
    public PetDbHelper(Context context) {   // Create a constructor for PetDbHelper. 把上方的資料庫名稱和和資料庫版本常數導入constructor
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Because we're subclassing from another class, we call the parent constructor via "super",
        // so we can take in the first parameter "context" from what was passed in, and the other parameters are the database name,
        // a cursor factory which we can just set to null to use the default, and then the database version.
    }


    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {  //Set up the constructor for PetDbHelper via onCreate method which executes the SQL_CREATE_PETS_TABLE statement.
        // Create a String that contains the SQL statement to create the pets table
        // Notice that this statement will heavily use constants from the contract in order to ensure consistency and avoid errors.
        // 對照參考: CREATE TABLE pets (_id INTEGER, name TEXT, breed TEXT, gender INTEGER, weight INTEGER); 把此SQLite語法透過concatenation轉換成JAVA可用的String
        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + PetEntry.TABLE_NAME + " ("
                + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetEntry.COLUMN_PET_BREED + " TEXT, "
                + PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);
        //execSQL method must not be used with any SELECT statements, and that's because this method doesn't return any actual data.
        // It's simply designed for executing statements that modify the database configuration and structure.
        // To summarize, this method takes in different SQL statements such as create table and it executes them.
    }


    /**
     * This is called when the database needs to be upgraded.
     * What it does is simply dropping the database table and recreating it.
     */
    // Notice in onUpgrade method, it executes this SQL statement: SQL_DELETE_ENTRIES.
    // And this constant is defined as DROP TABLE IF EXISTS and the name of the table (  "DROP TABLE IF EXISTS" + OOOEntry.TABLE_NAME;  )
    // And then, it creates a new table. The purpose of the onUpgrade method is that,
    // it gives you the opportunity to update the database file based on changes that you've made to the structure in your code.
    // So for example, if we add a column maybe a height column to our pets table, we can implement the database version.
    // And this update information is passed to the helper and onUpgrade let's you then execute additional SQL statements
    // to modify the database file so that our app is using the most recent information.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here, meaning there is no need to write the SQL_DELETE_ENTRIES statement here.
    }
}