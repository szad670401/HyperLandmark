package com.zeusee.zmobileapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.util.TimingLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by 海米 on 2018/11/28.
 */

public class STUtils {
    private static final String TIMING_LOG_TAG = "STUtils timing";
    private static RenderScript mRS = null;
    private static ScriptIntrinsicYuvToRGB mYuvToRgb = null;
    private static Allocation ain = null;
    private static Allocation aOut = null;
    private static Bitmap bitmap = null;

    public STUtils() {
    }

    public static int[] getBGRAImageByte(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if(image.getConfig().equals(Bitmap.Config.ARGB_8888)) {
            int[] imgData = new int[width * height];
            image.getPixels(imgData, 0, width, 0, 0, width, height);
            return imgData;
        } else {
            return null;
        }
    }

    @SuppressLint({"NewApi"})
    public static Bitmap NV21ToRGBABitmap(byte[] nv21, int width, int height, Context context) {
        TimingLogger timings = new TimingLogger("STUtils timing", "NV21ToRGBABitmap");
        Rect rect = new Rect(0, 0, width, height);

        try {
            Class.forName("android.renderscript.Element$DataKind").getField("PIXEL_YUV");
            Class.forName("android.renderscript.ScriptIntrinsicYuvToRGB");
            if(mRS == null) {
                mRS = RenderScript.create(context);
                mYuvToRgb = ScriptIntrinsicYuvToRGB.create(mRS, Element.U8_4(mRS));
                Type.Builder tb = new Type.Builder(mRS, Element.createPixel(mRS, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV));
                tb.setX(width);
                tb.setY(height);
                tb.setMipmaps(false);
                tb.setYuvFormat(17);
                ain = Allocation.createTyped(mRS, tb.create(), 1);
                timings.addSplit("Prepare for ain");
                Type.Builder tb2 = new Type.Builder(mRS, Element.RGBA_8888(mRS));
                tb2.setX(width);
                tb2.setY(height);
                tb2.setMipmaps(false);
                aOut = Allocation.createTyped(mRS, tb2.create(), 0);
                timings.addSplit("Prepare for aOut");
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                timings.addSplit("Create Bitmap");
            }

            ain.copyFrom(nv21);
            timings.addSplit("ain copyFrom");
            mYuvToRgb.setInput(ain);
            timings.addSplit("setInput ain");
            mYuvToRgb.forEach(aOut);
            timings.addSplit("NV21 to ARGB forEach");
            aOut.copyTo(bitmap);
            timings.addSplit("Allocation to Bitmap");
        } catch (Exception var10) {
            YuvImage yuvImage = new YuvImage(nv21, 17, width, height, (int[])null);
            timings.addSplit("NV21 bytes to YuvImage");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(rect, 90, baos);
            byte[] cur = baos.toByteArray();
            timings.addSplit("YuvImage crop and compress to Jpeg Bytes");
            bitmap = BitmapFactory.decodeByteArray(cur, 0, cur.length);
            timings.addSplit("Jpeg Bytes to Bitmap");
        }

        timings.dumpToLog();
        return bitmap;
    }

    public static Bitmap NV21ToRGBABitmap(byte[] nv21, int width, int height) {
        YuvImage yuvImage = new YuvImage(nv21, 17, width, height, (int[])null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
        byte[] cur = baos.toByteArray();
        return BitmapFactory.decodeByteArray(cur, 0, cur.length);
    }

    public static void drawFaceRect(Canvas canvas, Rect rect, int width, int height, boolean frontCamera) {
        if(canvas != null) {
            Paint paint = new Paint();
            paint.setColor(Color.rgb(255, 0, 127));
            int strokeWidth = Math.max(width / 240, 2);
            paint.setStrokeWidth((float)strokeWidth);
            if(frontCamera) {
                int left = rect.left;
                rect.left = width - rect.right;
                rect.right = width - left;
            }

            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rect, paint);
        }
    }

    public static void drawPoints(Canvas canvas, Paint paint, PointF[] points, float[] visibles, int width, int height, boolean frontCamera) {
        if(canvas != null) {
            int strokeWidth = Math.max(width / 240, 2);

            for(int i = 0; i < points.length; ++i) {
                PointF p = points[i];
                if(frontCamera) {
                    p.x = (float)width - p.x;
                }

                if((double)visibles[i] < 0.5D) {
                    paint.setColor(Color.rgb(255, 20, 20));
                } else {
                    paint.setColor(Color.rgb(57, 168, 243));
                }

                canvas.drawCircle(p.x, p.y, (float)strokeWidth, paint);
            }

            paint.setColor(Color.rgb(57, 138, 243));
        }
    }

    public static Rect RotateDeg90(Rect rect, int width, int height) {
        int left = rect.left;
        rect.left = height - rect.bottom;
        rect.bottom = rect.right;
        rect.right = height - rect.top;
        rect.top = left;
        return rect;
    }

    public static Rect RotateDeg270(Rect rect, int width, int height) {
        int left = rect.left;
        rect.left = rect.top;
        rect.top = width - rect.right;
        rect.right = rect.bottom;
        rect.bottom = width - left;
        return rect;
    }

    public static PointF RotateDeg90(PointF point, int width, int height) {
        float x = point.x;
        point.x = (float)height - point.y;
        point.y = x;
        return point;
    }

    public static PointF RotateDeg270(PointF point, int width, int height) {
        float x = point.x;
        point.x = point.y;
        point.y = (float)width - x;
        return point;
    }

    public static Bitmap getRotateBitmap(Bitmap bitmap, int rotation) {
        if(null != bitmap && !bitmap.isRecycled()) {
            Matrix matrix = new Matrix();
            matrix.postRotate((float)rotation);
            Bitmap cropBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            return cropBitmap;
        } else {
            return null;
        }
    }

    public static void recycleBitmap(Bitmap bitmap) {
        if(bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public static void copyModelIfNeed(String modelName, Context mContext) {
        String path = getModelPath(modelName, mContext);
        if(path != null) {
            File modelFile = new File(path);
            if(!modelFile.exists()) {
                try {
                    if(modelFile.exists()) {
                        modelFile.delete();
                    }

                    modelFile.createNewFile();
                    InputStream in = mContext.getApplicationContext().getAssets().open(modelName);
                    if(in == null) {
                        Log.e("MultiTrack106", "the src module is not existed");
                    }

                    OutputStream out = new FileOutputStream(modelFile);
                    byte[] buffer = new byte[4096];

                    int n;
                    while((n = in.read(buffer)) > 0) {
                        out.write(buffer, 0, n);
                    }

                    in.close();
                    out.close();
                } catch (IOException var8) {
                    modelFile.delete();
                }
            }
        }

    }

    public static String getModelPath(String modelName, Context mContext) {
        String path = null;
        File dataDir = mContext.getApplicationContext().getExternalFilesDir((String)null);
        if(dataDir != null) {
            path = dataDir.getAbsolutePath() + File.separator + modelName;
        }

        return path;
    }
}
