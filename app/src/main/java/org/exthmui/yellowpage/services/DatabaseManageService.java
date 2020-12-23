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

package org.exthmui.yellowpage.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.exthmui.yellowpage.R;
import org.exthmui.yellowpage.helpers.PhoneNumberTagDbHelper;
import org.exthmui.yellowpage.helpers.YellowPageDbHelper;
import org.exthmui.yellowpage.misc.Constants;
import org.exthmui.yellowpage.models.ContactData;
import org.exthmui.yellowpage.utils.JsonUtil;
import org.exthmui.yellowpage.utils.NotificationUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseManageService extends Service {

    private static final String TAG = "DatabaseManageService";
    private static final int NOTIFICATION_ID = TAG.hashCode();

    public static final int DATABASE_STATUS_NORMAL = 0;
    public static final int DATABASE_STATUS_CHECKING_UPDATE = 1;
    public static final int DATABASE_STATUS_NO_UPDATES = 2;
    public static final int DATABASE_STATUS_UPDATING = 3;
    public static final int DATABASE_STATUS_UPDATED = 4;
    public static final int DATABASE_STATUS_UPDATE_FAILED = 5;

    private YellowPageDbHelper yellowPageDbHelper;
    private PhoneNumberTagDbHelper phoneNumberTagDbHelper;

    private DatabaseStatusListener yellowPageDbListener;
    private DatabaseStatusListener phoneNumberTagDbListener;
    private final AtomicBoolean yellowPageDbIsProcessing = new AtomicBoolean(false);
    private final AtomicBoolean phoneNumberTagDbIsProcessing = new AtomicBoolean(false);

    private SharedPreferences sharedPreferences;

    public DatabaseManageService() {
    }

    @Override
    public void onCreate() {
        yellowPageDbHelper = new YellowPageDbHelper(this);
        phoneNumberTagDbHelper = new PhoneNumberTagDbHelper(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        NotificationUtil.createNotificationChannel(this, Constants.NOTIFICATION_CHANNEL_DATABASE_STATUS, getString(R.string.database_status_title), NotificationUtil.IMPORTANCE_DEFAULT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new DatabaseManager();
    }

    private void updateYellowPageDatabase() {
        if (yellowPageDbIsProcessing.compareAndSet(false, true)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    notifyListener(DATABASE_STATUS_CHECKING_UPDATE, yellowPageDbListener);
                    try {
                        JSONObject dataJson = JsonUtil.getJsonFromURL(Constants.YELLOWPAGE_DATA_UPDATE_URL + "?version=" + sharedPreferences.getLong(Constants.KEY_YELLOWPAGE_DATABASE_VERSION, 0));
                        int status = dataJson.getInt("status");
                        long version = dataJson.getLong("version");
                        if (status == 0) {
                            notifyListener(DATABASE_STATUS_UPDATING, yellowPageDbListener);
                            JSONArray jsonArray = dataJson.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                ContactData contactData = new ContactData();
                                JSONArray addressArray, phoneNumberArray, websiteArray;
                                contactData.setName(jsonObject.getString("name"));
                                contactData.setPhotoURL(jsonObject.getString("avatar"));
                                phoneNumberArray = jsonObject.getJSONArray("phone");
                                for (int j = 0; j < phoneNumberArray.length(); j++) {
                                    JSONObject object = phoneNumberArray.getJSONObject(j);
                                    contactData.addPhoneNumber(object.getString("number"), ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM, object.getString("label"));
                                }
                                try {
                                    addressArray = jsonObject.getJSONArray("address");
                                    for (int j = 0; j < addressArray.length(); j++) {
                                        JSONObject object = addressArray.getJSONObject(j);
                                        contactData.addAddress(object.getString("data"), ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM, object.getString("label"));
                                    }
                                } catch (JSONException e) {
                                    // do nothing
                                }
                                try {
                                    websiteArray = jsonObject.getJSONArray("website");
                                    for (int j = 0; j < websiteArray.length(); j++) {
                                        JSONObject object = websiteArray.getJSONObject(j);
                                        contactData.addWebsite(object.getString("url"), ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM, object.getString("label"));
                                    }
                                } catch (JSONException e) {
                                    // do nothing
                                }
                                yellowPageDbHelper.insertContactData(contactData);
                            }
                            sharedPreferences.edit().putLong(Constants.KEY_YELLOWPAGE_DATABASE_VERSION, version).apply();
                            notifyListener(DATABASE_STATUS_UPDATED, yellowPageDbListener);
                        } else if (status == 1) {
                            notifyListener(DATABASE_STATUS_NO_UPDATES, yellowPageDbListener);
                        } else {
                            notifyListener(DATABASE_STATUS_UPDATE_FAILED, yellowPageDbListener);
                        }
                    } catch (IOException | JSONException | NullPointerException e) {
                        e.printStackTrace();
                        notifyListener(DATABASE_STATUS_UPDATE_FAILED, yellowPageDbListener);
                    }
                    synchronized (yellowPageDbIsProcessing) {
                        yellowPageDbIsProcessing.set(false);
                    }
                }
            }).start();
        }
    }

    private void updatePhoneNumberTagDatabase() {

    }

    private void notifyListener(int status, DatabaseStatusListener listener) {
        String statusString = null;
        boolean indeterminate = false;
        switch (status) {
            case DATABASE_STATUS_CHECKING_UPDATE:
                statusString = getString(R.string.database_checking_update);
                indeterminate = true;
                break;
            case DATABASE_STATUS_UPDATING:
                statusString = getString(R.string.database_updating);
                indeterminate = true;
                break;
            case DATABASE_STATUS_UPDATE_FAILED:
                statusString = getString(R.string.database_update_failed);
                break;
            case DATABASE_STATUS_UPDATED:
                statusString = getString(R.string.database_updated);
                break;
        }
        if (!TextUtils.isEmpty(statusString)) {
            NotificationUtil.showNotification(this, null, Constants.NOTIFICATION_CHANNEL_DATABASE_STATUS, NOTIFICATION_ID, statusString, null, R.drawable.ic_cloud_download, 0, 0, indeterminate);
        } else {
            NotificationUtil.destroyNotification(this, NOTIFICATION_ID);
        }
        if (listener != null) {
            listener.onStatusChanged(status);
        }
    }


    public class DatabaseManager extends Binder {
        public void setYellowPageDbListener(DatabaseStatusListener listener) {
            yellowPageDbListener = listener;
        }

        public void updateYellowPageData() {
            updateYellowPageDatabase();
        }

        public long getYellowPageDataCount() {
            return yellowPageDbHelper.getContactsCount();
        }

        public void setPhoneNumberTagDbListener(DatabaseStatusListener listener) {
            phoneNumberTagDbListener = listener;
        }

        public void updatePhoneNumberTagData() {
            updatePhoneNumberTagDatabase();
        }

        public long getPhoneNumberDataCount() {
            return phoneNumberTagDbHelper.getDataCount();
        }
    }

    public abstract static class DatabaseStatusListener {
        public abstract void onStatusChanged(int status);
    }
}
