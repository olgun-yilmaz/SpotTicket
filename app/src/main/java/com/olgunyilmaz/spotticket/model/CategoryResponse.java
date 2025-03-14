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

package com.olgunyilmaz.spotticket.model;

public class CategoryResponse { // category model class -> id & name
    private final int imageID;

    public CategoryResponse(int imageID, String categoryName) {
        this.imageID = imageID;
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }


    public int getImageID() {
        return imageID;
    }

    private final String categoryName;
}
