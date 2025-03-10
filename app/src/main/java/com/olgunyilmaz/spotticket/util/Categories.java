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

package com.olgunyilmaz.spotticket.util;

import android.content.Context;

import com.olgunyilmaz.spotticket.R;

import java.util.HashMap;
import java.util.Map;

public class Categories {
    private Context context;

    public Categories(Context context) {
        this.context = context;
    }

    public Map<String, String> CATEGORIES = new HashMap<>();

    public void loadCategories() { // all categories and their ticketmaster id's

        if (context != null) {
            CATEGORIES.put(context.getString(R.string.all_categories), "");

            // music
            CATEGORIES.put(context.getString(R.string.music), "KZFzniwnSyZfZ7v7nJ");
            CATEGORIES.put(context.getString(R.string.rock), "KnvZfZ7vAeA");
            CATEGORIES.put(context.getString(R.string.pop_music), "KnvZfZ7vAev");
            CATEGORIES.put(context.getString(R.string.hip_hop), "KnvZfZ7vAv1");
            CATEGORIES.put(context.getString(R.string.classical_music), "KnvZfZ7vAeJ");
            CATEGORIES.put(context.getString(R.string.jazz), "KnvZfZ7vAvE");
            CATEGORIES.put(context.getString(R.string.blues), "KnvZfZ7vAvd");
            CATEGORIES.put(context.getString(R.string.folk), "KnvZfZ7vAva");
            CATEGORIES.put(context.getString(R.string.rb), "KnvZfZ7vAee");
            CATEGORIES.put(context.getString(R.string.electro), "KnvZfZ7vAvF");

            // sports
            CATEGORIES.put(context.getString(R.string.sports), "KZFzniwnSyZfZ7v7nE");
            CATEGORIES.put(context.getString(R.string.basketball), "KnvZfZ7vAde");
            CATEGORIES.put(context.getString(R.string.ice_hockey), "KnvZfZ7vAdI");
            CATEGORIES.put(context.getString(R.string.tennis), "KnvZfZ7vAAv");
            CATEGORIES.put(context.getString(R.string.baseball), "KnvZfZ7vAdv");
            CATEGORIES.put(context.getString(R.string.mma), "KnvZfZ7vA7k");

            // theater
            CATEGORIES.put(context.getString(R.string.theater_art), "KZFzniwnSyZfZ7v7na");
            CATEGORIES.put(context.getString(R.string.theater), "KnvZfZ7v7na");
            CATEGORIES.put(context.getString(R.string.opera), "KnvZfZ7v7n1");
            CATEGORIES.put(context.getString(R.string.musical), "KnvZfZ7v7l1");
            CATEGORIES.put(context.getString(R.string.dancing), "KnvZfZ7v7nI");

            // family events
            CATEGORIES.put(context.getString(R.string.family), "KZFzniwnSyZfZ7v7n1");
            CATEGORIES.put(context.getString(R.string.disney), "KnvZfZ7vA1n");

            // movie
            CATEGORIES.put(context.getString(R.string.movie), "KZFzniwnSyZfZ7v7nn");

            // fest
            CATEGORIES.put(context.getString(R.string.food), "KnvZfZ7vAeF");
            CATEGORIES.put(context.getString(R.string.cultural), "KnvZfZ7vAeE");

            // other
            CATEGORIES.put(context.getString(R.string.conference), "KnvZfZ7vAe7");
            CATEGORIES.put(context.getString(R.string.entertainment), "KZFzniwnSyZfZ7v7nX");
            CATEGORIES.put(context.getString(R.string.exhibition), "KZFzniwnSyZfZ7v7nY");

        }
    }
} 