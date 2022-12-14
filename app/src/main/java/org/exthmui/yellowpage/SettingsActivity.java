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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.exthmui.yellowpage.misc.Constants;
import org.exthmui.yellowpage.services.DatabaseManageService;

public class SettingsActivity extends AppCompatActivity {

    public static final String POST_NOTIFICATIONS="android.permission.POST_NOTIFICATIONS";
    private final DataManageConn dataManageConn = new DataManageConn();
    private DatabaseManageService.DatabaseManager mDataBaseManager;
    private final DatabaseStatusListener yellowPageDbListener = new DatabaseStatusListener();
    private SettingsFragment settingsFragment;

    private Preference yellowPageDbStatus;
    private Preference callerIdDbStatus;
    private Preference updateYellowPage;
    private Preference yellowPageContributors;

    private class DatabaseStatusListener extends DatabaseManageService.DatabaseStatusListener {
        public Preference preference;

        public void onStatusChanged(int status) {
            if (preference == null) return;

            final int resId;
            switch (status) {
                case DatabaseManageService.DATABASE_STATUS_CHECKING_UPDATE:
                    resId = R.string.database_checking_update;
                    break;
                case DatabaseManageService.DATABASE_STATUS_NO_UPDATES:
                    resId = R.string.database_no_updated_version;
                    break;
                case DatabaseManageService.DATABASE_STATUS_UPDATED:
                    resId = R.string.database_updated;
                    break;
                case DatabaseManageService.DATABASE_STATUS_UPDATE_FAILED:
                    resId = R.string.database_update_failed;
                    break;
                case DatabaseManageService.DATABASE_STATUS_UPDATING:
                    resId = R.string.database_updating;
                    break;
                default:
                    return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    preference.setSummary(resId);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        requestNotifiPerm(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent dbManageService = new Intent(this, DatabaseManageService.class);
        startService(dbManageService);
        bindService(dbManageService, dataManageConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (settingsFragment == null && fragment instanceof SettingsFragment) {
            settingsFragment = (SettingsFragment) fragment;
            updateView();
        }
    }

    private void updateView() {
        if (settingsFragment == null || mDataBaseManager == null) return;

        yellowPageDbStatus = settingsFragment.findPreference(Constants.KEY_YELLOWPAGE_DATABASE_STATUS);
        yellowPageContributors = settingsFragment.findPreference(Constants.KEY_YELLOWPAGE_CONTRIBUTORS);
        updateYellowPage = settingsFragment.findPreference(Constants.KEY_YELLOWPAGE_UPDATE_DATABASE);
        callerIdDbStatus = settingsFragment.findPreference(Constants.KEY_CALLER_ID_DATABASE_STATUS);

        yellowPageDbListener.preference = yellowPageDbStatus;

        yellowPageContributors.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri content_url = Uri.parse(Constants.YELLOWPAGE_CONTRIBUTORS_URL);
                intent.setData(content_url);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            }
        });

        updateYellowPage.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mDataBaseManager.updateYellowPageData();
                return true;
            }
        });

        yellowPageDbStatus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                yellowPageDbStatus.setSummary(getString(R.string.database_status,mDataBaseManager.getYellowPageDataCount()));
                return true;
            }
        });

        yellowPageDbStatus.setSummary(getString(R.string.database_status,mDataBaseManager.getYellowPageDataCount()));
        callerIdDbStatus.setSummary(getString(R.string.database_status,mDataBaseManager.getPhoneNumberDataCount()));

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    @Override
    protected void onDestroy() {
        if (dataManageConn != null) {
            mDataBaseManager.setYellowPageDbListener(null);
            unbindService(dataManageConn);
        }
        super.onDestroy();
    }

    private class DataManageConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDataBaseManager = (DatabaseManageService.DatabaseManager) iBinder;
            mDataBaseManager.setYellowPageDbListener(yellowPageDbListener);
            updateView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDataBaseManager.setYellowPageDbListener(null);
        }
    }

    private static void requestNotifiPerm(Activity activity){
        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(activity, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions( activity,new String[]{POST_NOTIFICATIONS},100);
            }
        }
    }
}