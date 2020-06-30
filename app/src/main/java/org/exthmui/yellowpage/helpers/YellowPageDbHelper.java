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

import org.exthmui.yellowpage.models.ContactData;
import org.exthmui.yellowpage.models.ContactExtra;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class YellowPageDbHelper extends SQLiteOpenHelper {

    public static final String YELLOWPAGE_DATABASE = "yellowpage.db";

    private static final int DATABASE_VERSION = 1;

    public static class ContactsEntry implements BaseColumns {
        static final String TABLE_NAME = "contacts";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_PHOTO_URL = "photo_url";
    }

    public static class PhoneNumberEntry implements BaseColumns {
        static final String TABLE_NAME = "phone_numbers";
        static final String COLUMN_NAME_CONTACT = "contact";
        static final String COLUMN_NAME_NUMBER = "number";
        static final String COLUMN_NAME_TYPE = "type";
        static final String COLUMN_NAME_LABEL = "label";
    }

    public static class WebsiteEntry implements BaseColumns {
        static final String TABLE_NAME = "websites";
        static final String COLUMN_NAME_CONTACT = "contact";
        static final String COLUMN_NAME_URL = "url";
        static final String COLUMN_NAME_TYPE = "type";
        static final String COLUMN_NAME_LABEL = "label";
    }

    public static class AddressEntry implements BaseColumns {
        static final String TABLE_NAME = "address";
        static final String COLUMN_NAME_FORMATTED_ADDRESS = "formatted_addr";
        static final String COLUMN_NAME_CONTACT = "contact";
        static final String COLUMN_NAME_TYPE = "type";
        static final String COLUMN_NAME_LABEL = "label";
    }

    private static final String SQL_CREATE_CONTACT_ENTRIES =
            "CREATE TABLE " + ContactsEntry.TABLE_NAME + " (" +
                    ContactsEntry._ID + " INTEGER PRIMARY KEY," +
                    ContactsEntry.COLUMN_NAME_NAME + " TEXT," +
                    ContactsEntry.COLUMN_NAME_PHOTO_URL + " TEXT)";

    private static final String SQL_DELETE_CONTACT_ENTRIES =
            "DROP TABLE IF EXISTS " + ContactsEntry.TABLE_NAME;

    private static final String SQL_CREATE_PHONE_ENTRIES =
            "CREATE TABLE " + PhoneNumberEntry.TABLE_NAME + " (" +
                    PhoneNumberEntry.COLUMN_NAME_NUMBER + " TEXT PRIMARY KEY," +
                    PhoneNumberEntry.COLUMN_NAME_CONTACT + " INTEGER NOT NULL," +
                    PhoneNumberEntry.COLUMN_NAME_TYPE + " INTEGER," +
                    PhoneNumberEntry.COLUMN_NAME_LABEL + " TEXT)";

    private static final String SQL_DELETE_PHONE_ENTRIES =
            "DROP TABLE IF EXISTS " + PhoneNumberEntry.TABLE_NAME;

    private static final String SQL_CREATE_WEBSITE_ENTRIES =
            "CREATE TABLE " + WebsiteEntry.TABLE_NAME + " (" +
                    WebsiteEntry._ID + " INTEGER PRIMARY KEY," +
                    WebsiteEntry.COLUMN_NAME_CONTACT + " INTEGER NOT NULL," +
                    WebsiteEntry.COLUMN_NAME_URL + " TEXT," +
                    WebsiteEntry.COLUMN_NAME_TYPE + " INTEGER," +
                    WebsiteEntry.COLUMN_NAME_LABEL + " TEXT)";

    private static final String SQL_DELETE_WEBSITE_ENTRIES =
            "DROP TABLE IF EXISTS " + WebsiteEntry.TABLE_NAME;

    private static final String SQL_CREATE_ADDRESS_ENTRIES =
            "CREATE TABLE " + AddressEntry.TABLE_NAME + " (" +
                    AddressEntry._ID + " INTEGER PRIMARY KEY," +
                    AddressEntry.COLUMN_NAME_CONTACT + " INTEGER NOT NULL," +
                    AddressEntry.COLUMN_NAME_FORMATTED_ADDRESS + " TEXT," +
                    AddressEntry.COLUMN_NAME_TYPE + " INTEGER," +
                    AddressEntry.COLUMN_NAME_LABEL + " TEXT)";

    private static final String SQL_DELETE_ADDRESS_ENTRIES =
            "DROP TABLE IF EXISTS " + AddressEntry.TABLE_NAME;

    public YellowPageDbHelper(Context context) {
        super(context, YELLOWPAGE_DATABASE, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CONTACT_ENTRIES);
        db.execSQL(SQL_CREATE_ADDRESS_ENTRIES);
        db.execSQL(SQL_CREATE_PHONE_ENTRIES);
        db.execSQL(SQL_CREATE_WEBSITE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_CONTACT_ENTRIES);
        db.execSQL(SQL_DELETE_ADDRESS_ENTRIES);
        db.execSQL(SQL_DELETE_PHONE_ENTRIES);
        db.execSQL(SQL_DELETE_WEBSITE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public ContactData getContactDataByID(long id) {
        SQLiteDatabase db = getReadableDatabase();
        ContactData data = getBaseContactData(db, id);
        fillPhoneNumberData(db, data);
        fillAddressData(db, data);
        fillWebsiteData(db, data);
        return data;
    }

    private ContactData getBaseContactData(SQLiteDatabase db, long id)
    {
        Cursor cursorContacts = db.query(ContactsEntry.TABLE_NAME, null, ContactsEntry._ID + " = " + id, null, null, null, null);
        if (cursorContacts.moveToPosition(0)) {
            ContactData data = new ContactData();
            data.setId(id);
            data.setName(cursorContacts.getString(cursorContacts.getColumnIndex(ContactsEntry.COLUMN_NAME_NAME)));
            data.setPhotoURL(cursorContacts.getString(cursorContacts.getColumnIndex(ContactsEntry.COLUMN_NAME_PHOTO_URL)));
            cursorContacts.close();
            return data;
        }
        cursorContacts.close();
        return null;
    }

    private void fillPhoneNumberData(SQLiteDatabase db, ContactData data)
    {
        Cursor cursorPhones = db.query(PhoneNumberEntry.TABLE_NAME, null, PhoneNumberEntry.COLUMN_NAME_CONTACT + " = " + data.getId(), null, null, null, null);
        while (cursorPhones.moveToNext()) {
            String number = cursorPhones.getString(cursorPhones.getColumnIndex(PhoneNumberEntry.COLUMN_NAME_NUMBER));
            int type = cursorPhones.getInt(cursorPhones.getColumnIndex(PhoneNumberEntry.COLUMN_NAME_TYPE));
            String label = cursorPhones.getString(cursorPhones.getColumnIndex(PhoneNumberEntry.COLUMN_NAME_LABEL));
            data.addPhoneNumber(number, type, label);
        }
        cursorPhones.close();
    }

    private void fillAddressData(SQLiteDatabase db, ContactData data)
    {
        Cursor cursorAddress = db.query(AddressEntry.TABLE_NAME, null, AddressEntry.COLUMN_NAME_CONTACT + " = " + data.getId(), null, null, null, null);
        while (cursorAddress.moveToNext()) {
            String formatted = cursorAddress.getString(cursorAddress.getColumnIndex(AddressEntry.COLUMN_NAME_FORMATTED_ADDRESS));
            int type = cursorAddress.getInt(cursorAddress.getColumnIndex(AddressEntry.COLUMN_NAME_TYPE));
            String label = cursorAddress.getString(cursorAddress.getColumnIndex(AddressEntry.COLUMN_NAME_LABEL));
            data.addAddress(formatted, type, label);
        }
        cursorAddress.close();
    }

    private void fillWebsiteData(SQLiteDatabase db, ContactData data)
    {
        Cursor cursorWebsites = db.query(WebsiteEntry.TABLE_NAME, null, WebsiteEntry.COLUMN_NAME_CONTACT + " = " + data.getId(), null, null, null, null);
        while (cursorWebsites.moveToNext()) {
            String url = cursorWebsites.getString(cursorWebsites.getColumnIndex(WebsiteEntry.COLUMN_NAME_URL));
            int type = cursorWebsites.getInt(cursorWebsites.getColumnIndex(WebsiteEntry.COLUMN_NAME_TYPE));
            String label = cursorWebsites.getString(cursorWebsites.getColumnIndex(WebsiteEntry.COLUMN_NAME_LABEL));
            data.addWebsite(url, type, label);
        }
        cursorWebsites.close();
    }

    public List<ContactData> getDataListByPhone(String phoneNumber)
    {
        SQLiteDatabase db = getReadableDatabase();
        String selection = PhoneNumberEntry.COLUMN_NAME_NUMBER + " LIKE '" + phoneNumber + "%'";
        Cursor cursor = db.query(PhoneNumberEntry.TABLE_NAME, null, selection, null, null, null, null);
        List<ContactData> dataList = new ArrayList<>();
        Set<Long> addedId = new TreeSet<>();
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(PhoneNumberEntry.COLUMN_NAME_CONTACT);
            long id = cursor.getLong(index);
            if (addedId.contains(id)) continue;
            dataList.add(getContactDataByID(id));
            addedId.add(id);
        }
        cursor.close();
        return dataList;
    }

    public ContactData getDataByPhone(String phoneNumber)
    {
        long id = findDataIdByPhoneNumber(phoneNumber);
        if (id != -1) {
            return getContactDataByID(id);
        } else {
            return null;
        }
    }

    public long findDataIdByPhoneNumber(String phoneNumber)
    {
        SQLiteDatabase db = getReadableDatabase();
        String selection = PhoneNumberEntry.COLUMN_NAME_NUMBER + " = '" + phoneNumber + "'";
        Cursor cursor = db.query(PhoneNumberEntry.TABLE_NAME, null, selection, null, null, null, null);
        if (cursor.moveToPosition(0)) {
            long id = cursor.getLong(cursor.getColumnIndex(PhoneNumberEntry.COLUMN_NAME_CONTACT));
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }

    public List<ContactData> getDataListByName(String name)
    {
        SQLiteDatabase db = getReadableDatabase();
        String selection = "" + ContactsEntry.COLUMN_NAME_NAME + " LIKE '%" + name + "%'";
        Cursor cursor = db.query(ContactsEntry.TABLE_NAME, null, selection, null, null, null, null);
        List<ContactData> dataList = new ArrayList<>();
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(ContactsEntry._ID);
            dataList.add(getContactDataByID(cursor.getLong(index)));
        }
        cursor.close();
        return dataList;
    }

    public long insertContactData(ContactData contactData) {
        SQLiteDatabase db = getWritableDatabase();
        // remove data if exists
        removeContactDataWithUnknownId(contactData);
        // do insert
        ContentValues values = new ContentValues();
        values.put(ContactsEntry.COLUMN_NAME_NAME, contactData.getName());
        values.put(ContactsEntry.COLUMN_NAME_PHOTO_URL, contactData.getPhotoURL());
        long rowId = db.insert(ContactsEntry.TABLE_NAME, null, values);
        // insert phone numbers
        for (ContactExtra phone : contactData.getPhoneNumbers()) {
            ContentValues phoneValues = new ContentValues();
            phoneValues.put(PhoneNumberEntry.COLUMN_NAME_CONTACT, rowId);
            phoneValues.put(PhoneNumberEntry.COLUMN_NAME_NUMBER, phone.data);
            phoneValues.put(PhoneNumberEntry.COLUMN_NAME_TYPE, phone.type);
            phoneValues.put(PhoneNumberEntry.COLUMN_NAME_LABEL, phone.label);
            db.insert(PhoneNumberEntry.TABLE_NAME, null, phoneValues);
        }
        // insert websites
        for (ContactExtra website : contactData.getWebsites()) {
            ContentValues webValues = new ContentValues();
            webValues.put(WebsiteEntry.COLUMN_NAME_CONTACT, rowId);
            webValues.put(WebsiteEntry.COLUMN_NAME_URL, website.data);
            webValues.put(WebsiteEntry.COLUMN_NAME_TYPE, website.type);
            webValues.put(WebsiteEntry.COLUMN_NAME_LABEL, website.label);
            db.insert(WebsiteEntry.TABLE_NAME, null, webValues);
        }
        // insert addresses
        for (ContactExtra address : contactData.getAddresses()) {
            ContentValues addrValues = new ContentValues();
            addrValues.put(AddressEntry.COLUMN_NAME_CONTACT, rowId);
            addrValues.put(AddressEntry.COLUMN_NAME_FORMATTED_ADDRESS, address.data);
            addrValues.put(AddressEntry.COLUMN_NAME_TYPE, address.type);
            addrValues.put(AddressEntry.COLUMN_NAME_LABEL, address.label);
            db.insert(AddressEntry.TABLE_NAME, null, addrValues);
        }
        return rowId;
    }

    public void removeContactDataWithUnknownId(ContactData data) {
        for (ContactExtra s : data.getPhoneNumbers()) {
            long id = findDataIdByPhoneNumber(s.data);
            if (id != -1) {
                removeContactData(id);
                return;
            }
        }
    }

    public void removeContactData(long id)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(ContactsEntry.TABLE_NAME, ContactsEntry._ID + " = " + id, null);
        db.delete(PhoneNumberEntry.TABLE_NAME, PhoneNumberEntry.COLUMN_NAME_CONTACT + " = " + id, null);
        db.delete(AddressEntry.TABLE_NAME, AddressEntry.COLUMN_NAME_CONTACT + " = " + id, null);
        db.delete(WebsiteEntry.TABLE_NAME, WebsiteEntry.COLUMN_NAME_CONTACT + " = " + id, null);
    }

    public void cleanData()
    {
        SQLiteDatabase db = getWritableDatabase();
        onUpgrade(db, DATABASE_VERSION, DATABASE_VERSION);
    }

    public long getContactsCount()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + ContactsEntry.TABLE_NAME, null);
        cursor.moveToFirst();
        long result = cursor.getLong(0);
        cursor.close();
        return result;
    }

}
