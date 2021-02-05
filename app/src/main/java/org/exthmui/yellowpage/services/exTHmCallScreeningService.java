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

import android.content.SharedPreferences;
import android.telecom.Call;
import android.telecom.CallScreeningService;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.exthmui.yellowpage.R;
import org.exthmui.yellowpage.helpers.PhoneNumberTagDbHelper;
import org.exthmui.yellowpage.lookup.BaiduLookup;
import org.exthmui.yellowpage.lookup.PhoneNumberLookup;
import org.exthmui.yellowpage.lookup.Safe360Lookup;
import org.exthmui.yellowpage.lookup.SogouLookup;
import org.exthmui.yellowpage.misc.Constants;
import org.exthmui.yellowpage.models.PhoneNumberInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class exTHmCallScreeningService extends CallScreeningService {

    private static final String TAG = "exTHmCallScreeningService";

    private PhoneNumberTagDbHelper phoneNumberTagDbHelper;
    private final PhoneNumberLookup[] phoneNumberLookups = {new SogouLookup(), new BaiduLookup(), new Safe360Lookup()};
    private SharedPreferences sharedPreferences;
    private final Map<String, Long> mRecentRejectPhoneNumber = new HashMap<>();

    private long REPEAT_LIMIT_TIME = 60*20*1000; // 20 minutes

    public exTHmCallScreeningService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        phoneNumberTagDbHelper = new PhoneNumberTagDbHelper(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private PhoneNumberInfo lookupInfoOnline(String number, long countryCode) {
        PhoneNumberInfo info = null;
        for (PhoneNumberLookup phoneNumberLookup : phoneNumberLookups) {
            if (countryCode == 0 || phoneNumberLookup.checkRegion(countryCode)) {
                info = phoneNumberLookup.lookup(this, number);
                if (info.type != Constants.PhoneNumberTagData.TYPE_NORMAL) {
                    break;
                }
            }
        }
        return info;
    }

    private boolean shouldBlock(PhoneNumberInfo info) {
        if (sharedPreferences.getBoolean(Constants.KEY_CALLER_ID_NO_BLOCK_REPEAT, true)) {
            long currentTimeStamp = System.currentTimeMillis();
            if (currentTimeStamp - mRecentRejectPhoneNumber.getOrDefault(info.number, 0L) <= REPEAT_LIMIT_TIME) {
                mRecentRejectPhoneNumber.put(info.number, currentTimeStamp);
                return false;
            }
            mRecentRejectPhoneNumber.put(info.number, currentTimeStamp);
        }
        if (info.type < 0) {
            if (sharedPreferences.getBoolean(Constants.KEY_CALLER_ID_BLOCK_BY_TAG, false)) {
                Set<String> needRejects = sharedPreferences.getStringSet(Constants.KEY_CALLER_ID_BLOCK_TAGS, null);
                String[] spamValues = getResources().getStringArray(R.array.block_tag_values);
                if (needRejects == null) return false;
                return spamValues.length >= -info.type && needRejects.contains(spamValues[-info.type - 1]);
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onScreenCall(@NonNull final Call.Details callDetails) {
        if (!sharedPreferences.getBoolean(Constants.KEY_CALLER_ID_ENABLED, true)) return;
        String number = callDetails.getHandle().toString().substring(4);
        final long countryCode = getCountryCode(number);;
        try {
            number = URLDecoder.decode(number, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        number = number.replaceAll("-", "").replaceAll(" ", "");
        if (number.startsWith("+")) {
            number = number.substring(1);
        }
        final CallResponse.Builder builder = new CallResponse.Builder();
        final String finalNumber = number;
        new Thread(new Runnable() {
            @Override
            public void run() {
                PhoneNumberInfo info = null;
                try {
                    info = phoneNumberTagDbHelper.query(finalNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (info == null) {
                    info = lookupInfoOnline(finalNumber, countryCode);
                    if (info.type != Constants.PhoneNumberTagData.TYPE_NORMAL) {
                        phoneNumberTagDbHelper.insertData(finalNumber, info.tag, info.type);
                    }
                }
                /*
                builder.setDisallowCall(false)      // 阻止来电传入
                        .setRejectCall(false)       // 拒接来电 （仅当 disallowCall 时有效）
                        .setSkipNotification(false) // 不显示未接来电通知（仅当 disallowCall 时有效）
                        .setSkipCallLog(false)      // 阻止到达通话记录 （仅当 disallowCall 时有效）
                        .setSilenceCall(false);     // 不响铃 （仅当 disallowCall 为 false 时有效）
                 */
                if (shouldBlock(info)) {
                    builder.setDisallowCall(true);
                    builder.setRejectCall(true);
                    builder.setSkipNotification(true);
                }
                respondToCall(callDetails,builder.build());
            }
        }).start();
    }

    private long getCountryCode(String number) {
        if (number.charAt(0) == '+') {
            String[] country_codes = getResources().getStringArray(R.array.country_codes);
            for (String code : country_codes) {
                if (number.startsWith("+" + code)) {
                    return Long.parseLong(code);
                }
            }
        }
        return 0;
    }
}
