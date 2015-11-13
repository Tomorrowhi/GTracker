package com.bct.gpstracker.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.bct.gpstracker.common.Constants;

/**
 * Created by hh on 2015/7/26 0026.
 *
 */
public class MediaUtil {
    public static Bitmap scaleToSettingSize(byte[] bytes) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        float settingSize = Constants.MAX_PIC_WIDTH_HEIGHT;
        try {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            int w = options.outWidth;
            int h = options.outHeight;
            options.inJustDecodeBounds = false;
            if (w > settingSize || h > settingSize) {
                Float scale;
                if (w > h) {
                    scale = w / settingSize;
                } else {
                    scale = h / settingSize;
                }
                options.inSampleSize = scale.intValue();
            }
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            w = options.outWidth;
            h = options.outHeight;
            if (w > settingSize || h > settingSize) {
                Float scale;
                if (w > h) {
                    scale = settingSize / w;
                } else {
                    scale = settingSize / h;
                }
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG,"缩放图片失败！",e);
            bitmap=null;
        }
        return bitmap;
    }

    /**
     * 得到amr的时长
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static long getAmrDuration(String path) throws IOException {
        long duration = -1;
        int[] packedSize = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };
        RandomAccessFile randomAccessFile = null;
        try {
            File file=new File(path);
            randomAccessFile = new RandomAccessFile(file, "rw");
            long length = file.length();//文件的长度
            int pos = 6;//设置初始位置
            int frameCount = 0;//初始帧数
            int packedPos = -1;
            /////////////////////////////////////////////////////
            byte[] datas = new byte[1];//初始数据值
            while (pos <= length) {
                randomAccessFile.seek(pos);
                if (randomAccessFile.read(datas, 0, 1) != 1) {
                    duration = length > 0 ? ((length - 6) / 650) : 0;
                    break;
                }
                packedPos = (datas[0] >> 3) & 0x0F;
                pos += packedSize[packedPos] + 1;
                frameCount++;
            }
            /////////////////////////////////////////////////////
            duration += frameCount * 20;//帧数*20
        } finally {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
        return duration;
    }
}
