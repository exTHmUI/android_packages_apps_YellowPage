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

import org.exthmui.yellowpage.models.PhoneNumberInfo;
import org.exthmui.yellowpage.utils.JsonUtil;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BaiduLookup implements PhoneNumberLookup {
    @Override
    public PhoneNumberInfo lookup(Context context, String number) {
        PhoneNumberInfo info = new PhoneNumberInfo();
        info.number = number;
        JSONObject params = new JSONObject();
        try {
            /* I don't know how to generate this data */
            params.put("data", "81e56802d9e92f6eda62bb8595b259e892808401724a14a7d838f76e460bb3ae8841afc7aa141f49db7f42dec22724e43759b42ef5157299c4b443f9d1e5fe1bce7063098f6b0c405d2dd797714e57158c8a869a9275918a5c82be878c8a583c150edadbca7e3a31ac007211eecfb3783fe0e171bddf1b6e91c115544935537f");
            params.put("key_id", "14");
            params.put("sign", "794c7ec2");
            params.put("page", 1);
            params.put("size", 10);
            params.put("search", number);

            // 构造请求
            URL url = new URL("https://haoma.baidu.com/api/v1/search");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept","application/json, text/plain, */*");
            connection.setRequestProperty("Accept-Language","en-US,en;q=0.8,zh-Hans-CN;q=0.5,zh-Hans;q=0.3");
            connection.setRequestProperty("Accept-Encoding","deflate");
            connection.setRequestProperty("Content-Type","application/json;charset=utf-8");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:77.0) Gecko/20100101 Firefox/77.0");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(1000);
            OutputStream os = connection.getOutputStream();
            os.write(params.toString().getBytes());
            os.close();
            // 处理搜索结果
            InputStream in = connection.getInputStream();
            JSONObject json = new JSONObject(JsonUtil.InputStreamToString(in));
            info.tag = json.getJSONObject("data")
                            .getJSONObject(number)
                            .getJSONArray("reports")
                            .getJSONObject(0)
                            .getString("name");
            PhoneNumberInfo.getTypeByTag(context, info);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    @Override
    public boolean checkRegion(long code) {
        return code == 86;
    }
}
