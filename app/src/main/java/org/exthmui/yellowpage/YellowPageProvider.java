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
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import org.exthmui.yellowpage.helpers.YellowPageDbHelper;
import org.exthmui.yellowpage.misc.Constants;
import org.exthmui.yellowpage.models.ContactData;
import org.exthmui.yellowpage.models.ContactExtra;

import java.util.ArrayList;
import java.util.List;

public class YellowPageProvider extends ContentProvider {

    public static final String TAG = "YellowPageProvider";

    public static final String PROVIDER_NAME = "org.exthmui.yellowpage.YellowPageProvider";

    public static final String PROVIDER_PATH_FORWARD = "forward";
    public static final String PROVIDER_PATH_REVERSE = "reverse";

    public static final int PROVIDER_CODE_FORWARD = 0;
    public static final int PROVIDER_CODE_REVERSE = 1;

    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, PROVIDER_PATH_FORWARD, PROVIDER_CODE_FORWARD);
        uriMatcher.addURI(PROVIDER_NAME, PROVIDER_PATH_REVERSE, PROVIDER_CODE_REVERSE);
    }

    private static YellowPageDbHelper yellowPageDbHelper;
    private SharedPreferences sharedPreferences;

    public YellowPageProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        yellowPageDbHelper = new YellowPageDbHelper(this.getContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (!sharedPreferences.getBoolean(Constants.KEY_YELLOWPAGE_ENABLED, true)) return null;
        MatrixCursor cursor = new MatrixCursor(Constants.YellowPageData.DATA_PROJECTION);
        List<ContactData> contactDataList = new ArrayList<>();
        selection = selection.replace(" ", "");
        switch (uriMatcher.match(uri)) {
            case PROVIDER_CODE_FORWARD:
                contactDataList.addAll(yellowPageDbHelper.getDataListByName(selection));
                contactDataList.addAll(yellowPageDbHelper.getDataListByPhone(selection));
                break;
            case PROVIDER_CODE_REVERSE:
                contactDataList.add(yellowPageDbHelper.getDataByPhone(selection));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        for (ContactData data : contactDataList) {
            if (data == null) continue;
            Object[] row = new Object[Constants.YellowPageData.DATA_PROJECTION.length];
            row[Constants.YellowPageData.COLUMN_ID] = data.getId();
            row[Constants.YellowPageData.COLUMN_NAME] = data.getName();
            row[Constants.YellowPageData.COLUMN_AVATAR] = data.getPhotoURL();
            row[Constants.YellowPageData.COLUMN_PHONE_JSON] = buildJsonArray(data.getPhoneNumbers());
            row[Constants.YellowPageData.COLUMN_WEBSITE_JSON] = buildJsonArray(data.getWebsites());
            row[Constants.YellowPageData.COLUMN_ADDRESS_JSON] = buildJsonArray(data.getAddresses());
            cursor.addRow(row);
        }

        try {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return cursor;
    }

    private String buildJsonArray(List<ContactExtra> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        boolean flag = false;
        for (ContactExtra extra : list) {
            if (flag) stringBuilder.append(", ");
            flag = true;
            stringBuilder.append(extra);
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
