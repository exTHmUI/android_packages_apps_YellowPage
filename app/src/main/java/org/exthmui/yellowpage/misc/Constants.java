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

package org.exthmui.yellowpage.misc;

import android.net.Uri;

public class Constants {

    public static final class YellowPageData {
        public static final int COLUMN_ID = 0;
        public static final int COLUMN_NAME = 1;
        public static final int COLUMN_AVATAR = 2;
        public static final int COLUMN_PHONE_JSON = 3;
        public static final int COLUMN_WEBSITE_JSON = 4;
        public static final int COLUMN_ADDRESS_JSON = 5;

        public static final String[] DATA_PROJECTION =
            new String[] {
                "id",
                "name",
                "avatar",
                "phone_json",
                "website_json",
                "address_json"
            };
    };

    public static final class PhoneNumberTagData {
        public static final int TYPE_NORMAL = 0;
        public static final int TYPE_CUSTOM_BLACK = Integer.MIN_VALUE;
        public static final int TYPE_SERVICE = 1;

        public static final int COLUMN_NUMBER = 1;
        public static final int COLUMN_TAG = 2;
        public static final int COLUMN_TYPE = 3;

        public static final String[] DATA_PROJECTION =
            new String[] {
                    "number",
                    "tag",
                    "type"
            };
    }

    // Preferences
    public static final String KEY_YELLOWPAGE_ENABLED = "yellowpage_enabled";
    public static final String KEY_YELLOWPAGE_UPDATE_DATABASE = "update_yellowpage_database";
    public static final String KEY_YELLOWPAGE_DATABASE_STATUS = "yellowpage_database_status";
    public static final String KEY_YELLOWPAGE_DATABASE_VERSION = "yellowpage_database_version";
    public static final String KEY_YELLOWPAGE_CONTRIBUTORS = "yellowpage_contributors";
    public static final String KEY_CALLER_ID_ENABLED = "caller_id_and_spam_enabled";
    public static final String KEY_CALLER_ID_BLOCK_BY_TAG = "caller_id_and_spam_block_by_tag";
    public static final String KEY_CALLER_ID_BLOCK_TAGS = "caller_id_and_spam_block_tags";
    public static final String KEY_CALLER_ID_NO_BLOCK_REPEAT = "caller_id_and_spam_no_block_repeat";
    public static final String KEY_CALLER_ID_UPDATE_DATABASE = "update_spam_database";
    public static final String KEY_CALLER_ID_DATABASE_STATUS = "caller_id_and_spam_database_status";
    public static final String KEY_CALLER_ID_DATABASE_VERSION = "caller_id_and_spam_database_version";

    public static final String YELLOWPAGE_DATA_UPDATE_URL = "https://cjybyjk.github.io/yellowpage_data.json";
    public static final String YELLOWPAGE_CONTRIBUTORS_URL = "https://github.com/exthmui/YellowPage_data/graphs/contributors";

    public static final String NOTIFICATION_CHANNEL_DATABASE_STATUS = "database_status";


    public static final Uri YELLOWPAGE_PROVIDER_URI = Uri.parse("content://org.exthmui.yellowpage.YellowPageProvider");
    public static final Uri YELLOWPAGE_PROVIDER_URI_FORWARD = Uri.withAppendedPath(YELLOWPAGE_PROVIDER_URI, "forward");
    public static final Uri YELLOWPAGE_PROVIDER_URI_REVERSE = Uri.withAppendedPath(YELLOWPAGE_PROVIDER_URI, "reverse");

    public static final Uri PHONE_NUMBER_TAG_PROVIDER_URI = Uri.parse("content://org.exthmui.yellowpage.PhoneNumberTagProvider");
    public static final Uri PHONE_NUMBER_TAG_PROVIDER_URI_EDIT = Uri.withAppendedPath(YELLOWPAGE_PROVIDER_URI, "edit");
    public static final Uri PHONE_NUMBER_TAG_PROVIDER_URI_QUERY = Uri.withAppendedPath(YELLOWPAGE_PROVIDER_URI, "query");

}
