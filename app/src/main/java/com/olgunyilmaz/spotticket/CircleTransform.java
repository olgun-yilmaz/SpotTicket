package com.olgunyilmaz.spotticket;

import com.squareup.picasso.Transformation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.squareup.picasso.Picasso;

public class CircleTransform implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        // Create the bitmap for the circular image
        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);
        if (squared != source) {
            source.recycle();
        }

        // Create a circular bitmap
        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());
        Canvas canvas = new Canvas(bitmap);

        // Create a circle shape to cut from the image
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        // Draw the circle and the image into it
        canvas.drawARGB(0, 0, 0, 0);
        Rect rect = new Rect(0, 0, size, size);
        RectF rectF = new RectF(rect);
        canvas.drawRoundRect(rectF, size / 2f, size / 2f, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(squared, rect, rect, paint);

        squared.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}
