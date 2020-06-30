/*
 * Copyright (C) 2020 The exTHmUI Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.exthmui.yellowpage.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.exthmui.yellowpage.models.PhoneNumberInfo;

public class PhoneNumberTagDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "phonenumber_tags.db";

    private static final int DATABASE_VERSION = 1;

    public static class DataEntry implements BaseColumns {
        static final String TABLE_NAME = "data";
        static final String COLUMN_NAME_NUMBER = "number";
        static final String COLUMN_NAME_TAG = "tag";
        static final String COLUMN_NAME_TYPE = "type";
    }

    private static final String SQL_CREATE_DATA_ENTRIES =
            "CREATE TABLE " + DataEntry.TABLE_NAME + " (" +
                    DataEntry._ID + " INTEGER PRIMARY KEY," +
                    DataEntry.COLUMN_NAME_NUMBER + " TEXT," +
                    DataEntry.COLUMN_NAME_TAG + " TEXT," +
                    DataEntry.COLUMN_NAME_TYPE + " INTEGER)";

    private static final String SQL_DELETE_DATA_ENTRIES =
            "DROP TABLE IF EXISTS " + DataEntry.TABLE_NAME;


    public PhoneNumberTagDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DATA_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_DATA_ENTRIES);
        db.execSQL(SQL_CREATE_DATA_ENTRIES);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_DATA_ENTRIES);
        db.execSQL(SQL_CREATE_DATA_ENTRIES);
    }

    public PhoneNumberInfo query(String number) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(DataEntry.TABLE_NAME, null, DataEntry.COLUMN_NAME_NUMBER + " = " + number, null, null, null, null);
        PhoneNumberInfo info = null;
        if (cursor.moveToNext()) {
            info = new PhoneNumberInfo();
            info.number = number;
            info.tag = cursor.getString(cursor.getColumnIndex(DataEntry.COLUMN_NAME_TAG));
            info.type = cursor.getInt(cursor.getColumnIndex(DataEntry.COLUMN_NAME_TYPE));
        }
        cursor.close();
        return info;
    }

    public Cursor queryRaw(String number) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(DataEntry.TABLE_NAME, null, DataEntry.COLUMN_NAME_NUMBER + " = " + number, null, null, null, null);
    }

    public long insertData(String number, String tag, int type) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DataEntry.TABLE_NAME, DataEntry.COLUMN_NAME_NUMBER + " = " + number, null);
        ContentValues values = new ContentValues();
        values.put(DataEntry.COLUMN_NAME_NUMBER, number);
        values.put(DataEntry.COLUMN_NAME_TAG, tag);
        values.put(DataEntry.COLUMN_NAME_TYPE, type);
        return db.insert(DataEntry.TABLE_NAME, null, values);
    }

    public long insertData(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DataEntry.TABLE_NAME, DataEntry.COLUMN_NAME_NUMBER + " = " + values.getAsString(DataEntry.COLUMN_NAME_NUMBER), null);
        return db.insert(DataEntry.TABLE_NAME, null, values);
    }

    public int updateData(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(DataEntry.TABLE_NAME, values, DataEntry.COLUMN_NAME_NUMBER + " = " + values.getAsString(DataEntry.COLUMN_NAME_NUMBER), null);
    }

    public int deleteData(String number) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(DataEntry.TABLE_NAME, DataEntry.COLUMN_NAME_NUMBER + " = " + number, null);
    }

    public long getDataCount()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + DataEntry.TABLE_NAME, null);
        cursor.moveToFirst();
        long result = cursor.getLong(0);
        cursor.close();
        return result;
    }
}
