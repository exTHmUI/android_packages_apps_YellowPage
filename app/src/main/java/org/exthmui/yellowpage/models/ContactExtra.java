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

public class ContactExtra {
    public String data;
    public int type;
    public String label;

    public ContactExtra() {
    }

    public ContactExtra(String data, int type, String label) {
        this.data = data;
        this.type = type;
        this.label = label;
    }

    @Override
    public String toString()
    {
        return "{" + "\"data1\": \"" + this.data + "\", " + "\"data2\": \"" + this.type + "\", " +  "\"data3\": \"" + this.label + "\"}";
    }
}
