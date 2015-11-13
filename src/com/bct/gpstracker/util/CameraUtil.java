package com.bct.gpstracker.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.bct.gpstracker.common.Constants;

/**
 * Created by Administrator on 2015/9/11 0011.
 */
public class CameraUtil {

    private Activity activity;
    private String phonoName;
    private String filePath;

    public CameraUtil(Activity activity) {
        this.activity = activity;
    }

    public void openCamera() {
        Intent intentFromCapture = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        String state = Environment
                .getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            phonoName = "bctPhoto.jpg";
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + phonoName;


            File f = new File(filePath);
            if (f.exists()) {
                f.delete();
            }


            intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath)));
            intentFromCapture.putExtra(MediaStore.Images.Media.ORIENTATION, 0);

//            intentFromCapture.putExtra(
//                    MediaStore.EXTRA_OUTPUT,
//                    Uri.fromFile(file));
            activity.startActivityForResult(intentFromCapture,
                    Constants.CAMERA_REQUEST_CODE);
        }
    }


//    public  void receiveResult() {
//        String state = Environment.getExternalStorageState();
//        if (state.equals(Environment.MEDIA_MOUNTED)) {
//
//            File tempFile = new File(phonoPath, phonoName);
//            startPhotoZoom(Uri.fromFile(tempFile));
//        } else {
//            Toast.makeText(
//                    activity, "未找到存储卡，无法存储照片",
//                    Toast.LENGTH_SHORT).show();
//        }
//    }

//    /**
//     * 裁剪图片方法实现
//     *
//     * @param uri
//     */
//    public  void startPhotoZoom(Uri uri) {
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(uri, "image/*");
//        // 设置裁剪
//        intent.putExtra("crop", "true");
//        // aspectX aspectY 是宽高的比例
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        // outputX outputY 是裁剪图片宽高
//        intent.putExtra("outputX", 340);
//        intent.putExtra("outputY", 340);
//        intent.putExtra("return-data", true);
//        activity.startActivityForResult(intent,  Constants.RESULT_REQUEST_CODE);
//    }

//    public void saveImage(Intent data) {
//        Bundle extras = data.getExtras();
//        if (extras != null) {
//            final Bitmap photo = extras.getParcelable("data");
//
//            File filePath = isExistsFilePath();
//
//
//            try {
//                File file = new File(filePath, phonoName);
//                FileOutputStream fileOutputStream = new FileOutputStream(file);
//                // 写入文件
//                boolean compress = photo.compress(Bitmap.CompressFormat.JPEG, 100,
//                        fileOutputStream);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }


//    private File isExistsFilePath() {
//        String filePath = Environment
//                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/bct";
//        File file = new File(filePath);
//        if (!file.exists()) {
//            file.mkdirs();
//        }
//        return file;
//    }

    public byte[] getPhono() {
        Bitmap bitmap = null;
        ByteArrayOutputStream baos = null;
        try {
            FileInputStream fis = new FileInputStream(filePath);
            bitmap = BitmapFactory.decodeStream(fis);
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != baos)
            return baos.toByteArray();
        else return null;
    }


}
