package com.example.lmfag.utility;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TransformBitmap {

    public static Bitmap fixRotation(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();
        try {
            // Extract metadata.
            Metadata metadata = ImageMetadataReader.readMetadata(new BufferedInputStream(new ByteArrayInputStream(imageData)), imageData.length);

            try {
                // Get the EXIF orientation.
                final ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                    final int exifOrientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

                    /* Work on exifOrientation */
                    try {
                        final Matrix bitmapMatrix = new Matrix();
                        switch (exifOrientation) {
                            case 1:
                                break;  // top left
                            case 2:
                                bitmapMatrix.postScale(-1, 1);
                                break;  // top right
                            case 3:
                                bitmapMatrix.postRotate(180);
                                break;  // bottom right
                            case 4:
                                bitmapMatrix.postRotate(180);
                                bitmapMatrix.postScale(-1, 1);
                                break;  // bottom left
                            case 5:
                                bitmapMatrix.postRotate(90);
                                bitmapMatrix.postScale(-1, 1);
                                break;  // left top
                            case 6:
                                bitmapMatrix.postRotate(90);
                                break;  // right top
                            case 7:
                                bitmapMatrix.postRotate(270);
                                bitmapMatrix.postScale(-1, 1);
                                break;  // right bottom
                            case 8:
                                bitmapMatrix.postRotate(270);
                                break;  // left bottom
                            default:
                                break;  // Unknown
                        }

                        // Create new bitmap.
                        return Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), bitmapMatrix, false);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }
        return imageBitmap;
    }

    public static Bitmap RotateNegative90(Bitmap imageBitmap) {
        final Matrix bitmapMatrix = new Matrix();
        bitmapMatrix.postRotate(270);
        return Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), bitmapMatrix, false);
    }

    public static Bitmap RotateBy90(Bitmap imageBitmap) {
        final Matrix bitmapMatrix = new Matrix();
        bitmapMatrix.postRotate(90);
        return Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), bitmapMatrix, false);
    }

    public static Bitmap flipHorizontal(Bitmap imageBitmap) {
        final Matrix bitmapMatrix = new Matrix();
        bitmapMatrix.postScale(-1, 1);
        return Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), bitmapMatrix, false);
    }

    public static Bitmap flipVertical(Bitmap imageBitmap) {
        final Matrix bitmapMatrix = new Matrix();
        bitmapMatrix.postScale(1, -1);
        return Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), bitmapMatrix, false);
    }
}
