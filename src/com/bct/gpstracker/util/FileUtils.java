package com.bct.gpstracker.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Toast;

public class FileUtils {
    private static final int BUFF_SIZE = 1024; // 1k Byte

    /**
     * 获取选择的文件的路径
     *
     * @param context
     * @param uri     资源定位器
     * @return 路径
     */
    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }finally {
                if(cursor!=null&&!cursor.isClosed()){
                    cursor.close();
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param context
     * @param folderName
     * @param filename
     * @param content
     * @throws IOException
     */
    public static String saveFile(Context context, String folderName, String filename, String content) throws IOException {
        return saveFile(context, folderName, filename, content.getBytes());
    }

    public static String saveFile(Context context, String folderName, String filename, byte[] bytes) throws IOException {
        String path = folderName + "/" + filename;
        String savePath = context.getFilesDir() + "/" + path;
        File file = new File(savePath);//要输出的文件路径
        if (!file.getParentFile().exists()) {//文件不存在
            file.getParentFile().mkdirs();//创建文件夹
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
        fos.close();
        return path;
    }

    /**
     * @param context
     * @param folderName
     * @throws IOException
     */
    public static void saveFolder(Context context, String folderName) throws IOException {
        String savePath = context.getFilesDir() + "/" + folderName;
        File file = new File(savePath);//要输出的文件路径
        if (!file.exists()) {
            file.mkdir();
        }
    }


    /**
     * 保存内容到内部存储器中
     *
     * @param filename 文件名
     * @param content  内容
     */
    public static void saveFile(Context context, String filename, int uid, String content, String fileType) throws IOException {
        // FileOutputStream fos=context.openFileOutput(filename,
        // Context.MODE_PRIVATE);
        if (fileType.equals("mysubject")) {
            String savePath = context.getFilesDir() + "/" + uid + "mysubject/";
            File file = new File(savePath + filename);//要输出的文件路径
            if (!file.getParentFile().exists()) {//文件不存在
                file.getParentFile().mkdirs();//创建文件夹    
            }
            FileOutputStream fos = new FileOutputStream(file);

            fos.write(content.getBytes());
            fos.close();
        } else if (fileType.equals("operation")) {
            String savePath = context.getFilesDir() + "/" + uid + "operation/";
            File file = new File(savePath + filename);//要输出的文件路径
            if (!file.getParentFile().exists()) {//文件不存在
                file.getParentFile().mkdirs();//创建文件夹    
            }
            FileOutputStream fos = new FileOutputStream(file);

            fos.write(content.getBytes());
            fos.close();
        } else {
            File file = new File(context.getFilesDir(), filename);
            FileOutputStream fos = new FileOutputStream(file);

            fos.write(content.getBytes());
            fos.close();
        }
    }

    public static String getFilePath(Context context) {
        String savePath = context.getFilesDir() + "/file/";
        return savePath;
    }

//    public static String getLogPath(Context context){
//    	String savePath = context.getFilesDir() +"/file/";
//    	return savePath;
//    }

    /**
     * 读取文件
     *
     * @param context
     * @param floderName
     * @param fileName
     * @return
     */
    public static String readFile(Context context, String floderName, String fileName) {
        String content = null;
        String savePath = context.getFilesDir() + "/" + floderName + "/";
        StringBuffer stringBuffer = new StringBuffer();
        try {
            File file = new File(savePath + fileName);
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    stringBuffer.append(lineTxt);
                }
                read.close();
                content = stringBuffer.toString();
            } else {
//				Toast.makeText(context, "数据文件不存在或已被删除", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
//			Toast.makeText(context, "读取数据文件内容出错", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return content;
    }

    public static byte[] readFileAsBytes(Context context, String path) {
        byte[] bytes;
        try {
            File file = new File(context.getFilesDir() + "/" + path);
            if (file.isFile() && file.exists()) {
                FileInputStream fin = new FileInputStream(file);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int rc;
                while ((rc = fin.read(buffer, 0, buffer.length)) != -1) {
                    bout.write(buffer, 0, rc);
                }
                bytes = bout.toByteArray();
                bout.close();
                fin.close();
            } else {
                bytes = new byte[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            bytes = new byte[0];
        }
        return bytes;
    }

    /**
     * 通过文件名获取内容
     *
     * @param filename 文件名
     * @return 文件内容
     */
    public static String getContent(Context context, int uid, String filename, String fileType) {
        String content = null;
        if (fileType.equals("mysubject")) {
            String savePath = context.getFilesDir() + "/" + uid + "mysubject/";
            StringBuffer stringBuffer = new StringBuffer();
            try {
                File file = new File(savePath + filename);
                if (file.isFile() && file.exists()) { // 判断文件是否存在
                    InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while ((lineTxt = bufferedReader.readLine()) != null) {
                        stringBuffer.append(lineTxt);
                    }
                    read.close();
                    content = stringBuffer.toString();
                } else {
                    Toast.makeText(context, "数据文件不存在或已被删除", Toast.LENGTH_SHORT).show();
//    				WizardAlertDialog.showErrorDialog(context, "找不到数据文件", R.string.btn_ok);
                }
            } catch (Exception e) {
                Toast.makeText(context, "读取数据文件内容出错", Toast.LENGTH_SHORT).show();
//    			WizardAlertDialog.showErrorDialog(context, "读取文件内容出错", R.string.btn_ok);
                e.printStackTrace();
            }
            return content;
        } else if (fileType.equals("operation")) {
            String savePath = context.getFilesDir() + "/" + uid + "operation/";
            StringBuffer stringBuffer = new StringBuffer();
            try {
                File file = new File(savePath + filename);
                if (file.isFile() && file.exists()) { // 判断文件是否存在
                    InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while ((lineTxt = bufferedReader.readLine()) != null) {
                        stringBuffer.append(lineTxt);
                    }
                    read.close();
                    content = stringBuffer.toString();
                } else {
                    Toast.makeText(context, "数据文件不存在或已被删除", Toast.LENGTH_SHORT).show();
//    				WizardAlertDialog.showErrorDialog(context, "找不到数据文件", R.string.btn_ok);
                }
            } catch (Exception e) {
                Toast.makeText(context, "读取数据文件内容出错", Toast.LENGTH_SHORT).show();
//    			WizardAlertDialog.showErrorDialog(context, "读取文件内容出错", R.string.btn_ok);
                e.printStackTrace();
            }
            return content;
        } else {
            try {
                FileInputStream fis = context.openFileInput(filename);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int len = -1;
                while ((len = fis.read(data)) != -1) {
                    baos.write(data, 0, len);
                }
                content = new String(baos.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }
    }

    /**
     * 以追加的方式在文件的末尾添加内容
     *
     * @param filename 文件名
     * @param content  追加的内容
     */
    public static void append(Context context, String filename, String content) throws IOException {
        FileOutputStream fos = context.openFileOutput(filename, Context.MODE_APPEND);
        fos.write(content.getBytes());
        fos.close();
    }

    /**
     * 删除文件
     *
     * @param filename 文件名
     * @return 是否成功
     */
    public static void delete(Context context, String filename, int uid, String fileType) {
//        return context.deleteFile(filename);

        if (fileType.equals("subject")) {
            String savePath = context.getFilesDir() + "/" + uid + "mysubject/";
            File file = new File(savePath + filename);
            if (file.isFile() && file.exists()) {
//    			return true;
                file.delete();
            }
        } else if (fileType.equals("file")) {
            String savePath = context.getFilesDir() + "/file/";
            File file = new File(savePath + filename);
            if (file.isFile() && file.exists()) {
//    			return true;
                file.delete();
            }
        } else if (fileType.equals("operation")) {
            String savePath = context.getFilesDir() + "/" + uid + "operation/";
            File file = new File(savePath + filename);
            if (file.isFile() && file.exists()) {
//    			return true;
                file.delete();
            }
        } else {
            String filePath = context.getFilesDir() + "/" + filename;
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
//        		return true;
                file.delete();
            }
        }
    }

    public static void delete(String file_path) {
        File file = new File(file_path);
        if (file.isFile() && file.exists()) {
//			return true;
            file.delete();
        }

        if (file.isDirectory()) {
            file.delete();
        }
    }

    /**
     * 获取内部存储路径下的所有文件名
     *
     * @return 文件名数组
     */
    public static String[] queryAllFile(Context context, int uid, boolean isSubject) {
//    	if(fileType.equals("subject")){
//    		String filePath = context.getFilesDir() +"/mysubject/";
//    		File file = new File(filePath);
//    		File[] array = file.listFiles();
//    		String[] str = new String[array.length];
//    		for(int i=0;i<array.length;i++){
//    			str[i] = array[i].getName();
//    		}
//    		return str;
//    	}else if (fileType.equals("file")) {
//    		String filePath = context.getFilesDir() +"/file/";
//    		File file = new File(filePath);
//    		File[] array = file.listFiles();
//    		String[] str = new String[array.length];
//    		for(int i=0;i<array.length;i++){
//    			str[i] = array[i].getName();
//    		}
//    		return str;
//		}else{
//    		return context.fileList();
//    	}
        if (isSubject == true) {
            String filePath = context.getFilesDir() + "/" + uid + "mysubject/";
            File file = new File(filePath);
            File[] array = file.listFiles();
            if (array != null) {
                String[] str = new String[array.length];
                for (int i = 0; i < array.length; i++) {
                    str[i] = array[i].getName();
                }
                return str;
            }
            return null;
        } else {
            return context.fileList();
        }
    }

    /**
     * 判断指定文件名的文件是否存在
     *
     * @return true是  false否
     */
    public static boolean fileIsExists(Context context, String folderName, String filename) {
        String savePath = context.getFilesDir() + "/" + folderName + "/";
        File file = new File(savePath + filename);
        if (file.isFile() && file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath 原文件路径
     * @param newPath 复制后的路径
     * @return
     */
    public static boolean copyFile(String oldPath, String newPath) {
        boolean isok = true;
        try {
            int byteSum = 0;
            int byteRead = 0;
            File oldFile = new File(oldPath);
            File newFile = new File(newPath);
            if (newFile.exists() && newFile.isFile()) {
                return false;
            }
            if (oldFile.exists()) {//文件不存在时
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                int length;
                while ((byteRead = inStream.read(buffer)) != -1) {
                    byteSum += byteRead;
                    fs.write(buffer, 0, byteRead);
                }
                fs.flush();
                fs.close();
                inStream.close();
            } else {
                isok = false;
            }
        } catch (Exception e) {
            isok = false;
        }
        return isok;
    }

    /**
     * 搜索文件夹或者文件的路径
     *
     * @param dir
     */
    public static List<String> listAll(File dir) {
        String path = "";
        List<String> pathStr = new ArrayList<String>();
        if (!dir.exists()) {// 健壮性判断
            throw new RuntimeException("目录不存在！！！");
        }
        File files[] = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                path = file.getAbsolutePath();
                listAll(file);
                pathStr.add(path);
            } else {
                //System.out.println(file.getAbsolutePath());
            }
        }
        return pathStr;
    }


    /**
     * 搜索文件夹或者文件的路径
     *
     * @param dir
     */
    public static List<String> listAllDir(File dir) {
        String path = "";
        List<String> pathStr = new ArrayList<String>();
        if (!dir.exists()) {// 健壮性判断
            throw new RuntimeException("目录不存在！！！");
        }
        File files[] = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                path = file.getAbsolutePath();
                listAll(file);
                pathStr.add(path);
            } else {
                //System.out.println(file.getAbsolutePath());
            }
//				listAll(file, count);
//			else
//				System.out.println(getSpace(count) + file.getAbsolutePath());
        }
        return pathStr;
    }


    //重命名
    public static void reName(Context context, String folderName, String newName) {
        String savePath = context.getFilesDir() + "/" + folderName;
        String newPath = context.getFilesDir() + "/" + newName;
        File file = new File(savePath);
        File newFile = new File(newPath);
        file.renameTo(newFile);
//		 String[] files= file.list();
//		 File f=null;  
//		 String filename="";  
//		 for(String str:files)  {  
//		     f=new File(file,str);// 注意,这里一定要写成File(fl,file)如果写成File(file)是行不通的,一定要全路径  
//		     filename=f.getName();
//		     fi
//		     f.renameTo(new File(file.getAbsolutePath()+"//"+filename.replace(filename,newName)));// 这里可以反复使用replace替换,当然也可以使用正则表达式来替换了  
//		 }
    }


    /**
     * 转化布局文件为Bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//      view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.destroyDrawingCache();
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 保存转化之后的图片
     *
     * @param context
     * @param folderName
     * @param bitmap
     * @param name
     */
    public static void savePNG_After(Context context, String folderName, Bitmap bitmap, String name) {
        String savePath = context.getFilesDir() + "/" + folderName + "/";
        File file = new File(savePath + name);//要输出的文件路径
        if (!file.getParentFile().exists()) {//文件不存在
            file.getParentFile().mkdirs();//创建文件夹    
        }
//		File file = new File(dir, name);
//		if(!file.exists()){
//			file.delete();
//		}
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public static void recursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                recursionDeleteFile(f);
            }
            file.delete();
        }
    }

    public static byte[] readInputStream(InputStream in) throws Exception {
        int len;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        while ((len = in.read(buffer)) != -1) {
            bout.write(buffer, 0, len);
        }
        byte[] data = bout.toByteArray();
        bout.close();
        in.close();
        return data;
    }

    /**
     * 压缩文件
     *
     * @param resFile  需要压缩的文件（夹）
     * @param zipout   压缩的目的文件
     * @param rootpath 压缩的文件路径
     */
    public static void zipFile(File resFile, ZipOutputStream zipout, String rootpath) {

        try {
            rootpath = rootpath + (rootpath.trim().length() == 0 ? "" : File.separator)
                    + resFile.getName();

            rootpath = new String(rootpath.getBytes("8859_1"), "GB2312");

            if (resFile.isDirectory()) {
                File[] fileList = resFile.listFiles();
                for (File file : fileList) {
                    zipFile(file, zipout, rootpath);
                }
            } else {
                byte buffer[] = new byte[BUFF_SIZE];
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(resFile),
                        BUFF_SIZE);
                zipout.putNextEntry(new ZipEntry(rootpath));
                int realLength;
                while ((realLength = in.read(buffer)) != -1) {
                    zipout.write(buffer, 0, realLength);
                }
                in.close();
                zipout.flush();
                zipout.closeEntry();
            }
            Log.d("TAG", "压缩完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
