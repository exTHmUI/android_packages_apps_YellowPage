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

package org.exthmui.yellowpage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import org.exthmui.yellowpage.helpers.PhoneNumberTagDbHelper;
import org.exthmui.yellowpage.misc.Constants;

public class PhoneNumberTagProvider extends ContentProvider {

    public static final String TAG = "PhoneNumberTagProvider";

    public static final String PROVIDER_NAME = "org.exthmui.yellowpage.PhoneNumberTagProvider";

    public static final String PROVIDER_PATH_QUERY = "query";
    public static final String PROVIDER_PATH_EDIT = "edit";

    public static final int PROVIDER_CODE_QUERY = 0;
    public static final int PROVIDER_CODE_EDIT = 1;

    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, PROVIDER_PATH_QUERY, PROVIDER_CODE_QUERY);
        uriMatcher.addURI(PROVIDER_NAME, PROVIDER_PATH_EDIT, PROVIDER_CODE_EDIT);
    }

    PhoneNumberTagDbHelper phoneNumberTagDbHelper;
    private SharedPreferences sharedPreferences;

    public PhoneNumberTagProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) == PROVIDER_CODE_EDIT) {
            return phoneNumberTagDbHelper.deleteData(selection);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) == PROVIDER_CODE_EDIT) {
            if (phoneNumberTagDbHelper.insertData(values) != -1) {
                return Constants.PHONE_NUMBER_TAG_PROVIDER_URI_QUERY;
            }
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        phoneNumberTagDbHelper = new PhoneNumberTagDbHelper(this.getContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (!sharedPreferences.getBoolean(Constants.KEY_CALLER_ID_ENABLED, true)) return null;
        if (uriMatcher.match(uri) == PROVIDER_CODE_QUERY) {
            return phoneNumberTagDbHelper.queryRaw(selection);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (uriMatcher.match(uri) == PROVIDER_CODE_EDIT) {
            return phoneNumberTagDbHelper.updateData(values);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
}
