/*
 * Project: SpotTicket
 * Copyright 2025 Olgun Yılmaz
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

package com.olgunyilmaz.spotticket.helper;

import android.content.Context;

import com.olgunyilmaz.spotticket.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DisplayHelper {
    private final Context context;

    public DisplayHelper(Context context) {
        this.context = context;
    }

    public String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1)).append(" ");
            }
        }

        return result.toString().trim();
    }

    public String formatDate(String inputDate) {
        inputDate = inputDate.replace(context.getString(R.string.date_text)+" ", "").trim();

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/M/yyyy", Locale.getDefault());

        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = inputFormat.parse(inputDate);

            if(date != null){
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            System.out.println(e.getLocalizedMessage());
        }
        return "";
    }

}
