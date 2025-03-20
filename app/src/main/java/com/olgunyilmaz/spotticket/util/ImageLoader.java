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
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ImageLoader { // makes image saving operations
    private final int maxSize;
    private final Uri imageUri;
    @NonNull
    private final Context context;

    public ImageLoader(@NonNull Context context, Uri imageUri, int maxSize) {
        this.imageUri = imageUri;
        this.context = context;
        this.maxSize = maxSize;
    }

    private Bitmap makeImgSmall(int maxSize) { // make smaller it for memory reduce and faster user experiment
        try {
            Bitmap img = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);

            Bitmap rotatedBitmap = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(),
                    getImageMatrix(imageUri), true);

            int width = rotatedBitmap.getWidth();
            int height = rotatedBitmap.getHeight();
            double bitmapRatio = (double) width / (double) height;

            if (bitmapRatio >= 1) { // Landscape
                width = maxSize;
                height = (int) (width / bitmapRatio);
            } else { // Portrait
                height = maxSize;
                width = (int) (height * bitmapRatio);
            }

            return Bitmap.createScaledBitmap(rotatedBitmap, width, height, true);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private Matrix getImageMatrix(Uri imageUri) throws IOException { // matrix operations
        ExifInterface exif = new ExifInterface(Objects.requireNonNull(context.getContentResolver().openInputStream(imageUri)));
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            case ExifInterface.ORIENTATION_UNDEFINED:
                break;
        }
        return matrix;

    }

    public Uri getResizedImageUri() { // bitmap to uri
        Bitmap bitmap = makeImgSmall(maxSize);
        File file = new File(context.getCacheDir(), "small_image.jpg");
        try {
            FileOutputStream out = new FileOutputStream(file);
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            UserManager.getInstance().profileImage = new CircleTransform().transform(bitmap); // save it
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return Uri.fromFile(file);
    }
}