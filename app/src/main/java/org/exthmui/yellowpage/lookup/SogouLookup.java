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

package org.exthmui.yellowpage.lookup;

import android.content.Context;
import android.text.TextUtils;

import org.exthmui.yellowpage.models.PhoneNumberInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SogouLookup implements PhoneNumberLookup {

    @Override
    public PhoneNumberInfo lookup(Context context, String number) {
        PhoneNumberInfo info = new PhoneNumberInfo();
        info.number = number;
        try {
            // 构造搜索请求
            URL url = new URL("https://www.sogou.com/web?query=" + number);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept","text/html, application/xhtml+xml, */*");
            connection.setRequestProperty("Accept-Language","en-US,en;q=0.8,zh-Hans-CN;q=0.5,zh-Hans;q=0.3");
            connection.setRequestProperty("Accept-Encoding","deflate");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:77.0) Gecko/20100101 Firefox/77.0");
            connection.setConnectTimeout(1000);
            connection.connect();
            // 处理搜索结果
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            // 取得响应中的标记
            while((line = reader.readLine()) != null){
                if (line.contains("tpl18139(")) break;
            }
            reader.close();
            in.close();
            if (!TextUtils.isEmpty(line)) {
                int pos = line.indexOf("号码通用户数据：");
                if (pos != -1) {
                    line = line.substring(pos + 8);
                    pos = line.indexOf("：");
                    line = pos > 0 ? line.substring(0, pos) : "";
                    info.tag = line;
                }
            }
            PhoneNumberInfo.getTypeByTag(context, info);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }
}
