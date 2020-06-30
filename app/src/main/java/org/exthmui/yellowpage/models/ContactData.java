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

package org.exthmui.yellowpage.models;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class ContactData {
    private long id;
    private String mName;
    private String mPhotoURL;
    private List<ContactExtra> mPhoneNumbers;
    private List<ContactExtra> mAddresses;
    private List<ContactExtra> mWebsites;

    public ContactData() {
        mPhoneNumbers = new ArrayList<>();
        mAddresses = new ArrayList<>();
        mWebsites = new ArrayList<>();
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPhotoURL(String url) {
        mPhotoURL = url;
    }

    public String getName() {
        return mName;
    }

    public String getPhotoURL() {
        return mPhotoURL;
    }

    public void addPhoneNumber(String number, int type, String label) {
        ContactExtra data = new ContactExtra(number, type, label);
        mPhoneNumbers.add(data);
    }

    public void addWebsite(String url, int type, String label) {
        mWebsites.add(new ContactExtra(url, type, label));
    }

    public void addAddress(String formattedAddress, int type, String label) {
        mAddresses.add(new ContactExtra(formattedAddress, type, label));
    }

    public List<ContactExtra> getPhoneNumbers() {
        return mPhoneNumbers;
    }

    public List<ContactExtra> getAddresses() {
        return mAddresses;
    }

    public List<ContactExtra> getWebsites() {
        return mWebsites;
    }

    @Override
    public int hashCode() {
        return (id + "," + mName + "," + mPhotoURL).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContactData) {
            ContactData otherData = (ContactData) obj;
            return (this.id == otherData.id &&
                    TextUtils.equals(this.mName, otherData.mName) &&
                    TextUtils.equals(this.mPhotoURL, otherData.mPhotoURL));
        }
        return false;
    }
}
