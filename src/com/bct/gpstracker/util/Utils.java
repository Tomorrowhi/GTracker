package com.bct.gpstracker.util;

import java.io.*;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.pojo.StationInfo;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.WifiAdmin;

@SuppressLint("SimpleDateFormat")
public class Utils {
    // 获取ApiKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {

        }
        return apiKey;
    }

    /**
     * 取得周几
     *
     * @param date
     * @return
     */
    public static String getWeek(Date date) {
        String Week = "周";
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            Week += "日";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 2) {
            Week += "一";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 3) {
            Week += "二";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 4) {
            Week += "三";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 5) {
            Week += "四";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 6) {
            Week += "五";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 7) {
            Week += "六";
        }
        return Week;
    }

    /**
     * 获取当前时间点
     *
     * @param dateformat
     * @return
     */
    public static String getNowTime(String dateformat) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);// 可以方便地修改日期格式
        String hehe = dateFormat.format(now);
        return hehe;
    }

    /**
     * 格式化日期 yyyy-MM-dd kk:mm
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        if (date == null) return "None";
        return android.text.format.DateFormat.format("yyyy-MM-dd kk:mm", date).toString();
    }

    /**
     * 格式化日期 yyyy-MM-dd
     *
     * @param date
     * @return
     */
    public static String formatDateShort(Date date) {
        if (date == null) return "None";
        return android.text.format.DateFormat.format("yyyy-MM-dd", date).toString();
    }

    /**
     * 格式化日期 MM-dd
     *
     * @param date
     * @return
     */
    public static String getMonthDate(Date date) {
        if (date == null) return "None";
        return android.text.format.DateFormat.format("MM-dd", date).toString();
    }

    /**
     * 判断系统是不是24小时制
     *
     * @return boolean  True是24小时
     */
    public static boolean is24(Context ctx) {
        ContentResolver cv = ctx.getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
        if (strTimeFormat != null && strTimeFormat.equals("24")) {// strTimeFormat某些rom12小时制时会返回null
            return true;
        } else {
            return false;
        }
    }

    /**
     * 比较两个时间的前后关系
     *
     * @param time1 老时间
     * @param time2 新时间
     * @return 0等于，1老时间小于新时间(所需)
     */
    public static int compareTime(String time1, String time2) {
//		String s1="2008-01-25 09:12:09";     
//		String s2="2008-01-29 09:12:11";     
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Calendar c1 = java.util.Calendar.getInstance();
        java.util.Calendar c2 = java.util.Calendar.getInstance();
        try {
            c1.setTime(df.parse(time1));
            c2.setTime(df.parse(time2));
        } catch (java.text.ParseException e) {
            Log.e(Constants.TAG, "格式不正确", e);
        }
        int result = c1.compareTo(c2);
        if (result == 0)
            return 0;
//		System.out.println("c1相等c2");     
        else if (result < 0)
            return 1;
//		System.out.println("c1小于c2");     
        else
            return 2;
//		System.out.println("c1大于c2");
    }

    /**
     * 将2014-10-12 12:12:12的格式转换为1012 121212格式
     *
     * @param time
     * @return
     */
    public static String getDayTime(String time) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = df.parse(time);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");// 可以方便地修改日期格式
            String hehe = dateFormat.format(date);
            return hehe;
        } catch (Exception e) {
            Log.e(Constants.TAG, "格式不正确", e);
            return "";
        }
    }

    /**
     * 对网络连接进行判断
     *
     * @return true, 网络已连接； false，未连接网络
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 对WIFI网络连接进行判断
     *
     * @return true, WIFI已连接； false，WIFI未连接
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 对MOBILE网络连接进行判断
     *
     * @return true, MOBILE已连接； false，MOBILE未连接
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获取网络连接类型
     *
     * @return
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    /**
     * 创建数据适配器时需要转换的工具
     *
     * @param @return MAP对象
     */
    public static Map<String, Object> createMap(String key, String label, String value) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", key);
        map.put("label", label);
        map.put("value", value);
        return map;
    }

    /**
     * 创建键值对工具
     */
    public static Map<String, Object> createMap(String key, String value) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", key);
        map.put("value", value);
        return map;
    }

    public static String dataPath = getSDCardPath() + "/gpstracker_data/";

    /**
     * 截取手机屏幕
     */
    private static String SavePath;

    @SuppressWarnings("deprecation")
    public static String GetandSaveCurrentImage(WindowManager windowManager,
                                                View decorview) {
        // 1.构建Bitmap
        Display display = windowManager.getDefaultDisplay();
        int w = display.getWidth();
        int h = display.getHeight();
        Bitmap Bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        // 2.获取屏幕
        decorview.setDrawingCacheEnabled(true);
        Bmp = decorview.getDrawingCache();
        SavePath = getSDCardPath() + "/ShareWX/ScreenImage";
        // 3.保存Bitmap
        try {
            File path = new File(SavePath);
            // 文件
            String filepath = SavePath + "/Scinan_Screen.png";
            File file = new File(filepath);
            if (!path.exists()) {
                path.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            if (null != fos) {
                Bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
        }
        return SavePath;
    }

    /**
     * 获取SD卡相关信息
     *
     * @return
     */
    public static String getSDCardPath() {
        File sdcardDir = null;
        // 判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        if (sdcardDir == null) {
            return "";
        } else {
            return sdcardDir.toString();
        }
    }

    /**
     * 获取可用存储路径，优先使用SD卡，结尾带斜杠
     *
     * @return
     */
    public static String getAvailableStoragePath() {
        String sdCardPath = getSDCardPath();
        StringBuilder sb = new StringBuilder();
        if (CommUtil.isNotBlank(sdCardPath)) {
            sb.append(sdCardPath).append("/data/").append(AppContext.getContext().getPackageName()).append('/');
        } else {
            sb.append(AppContext.getContext().getFilesDir()).append("/data/");
        }
        return sb.toString();
    }

    /**
     * 生成MD5字符串
     *
     * @param plainText 需要生成MD5的字符串
     * @return MD5字符串
     */
    public static String Md5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString().toUpperCase();
//			System.out.println("result: " + buf.toString());// 32位的加密
//			System.out.println("result: " + buf.toString().substring(8, 24));// 16位的加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * String类型的集合转换为String 每个数据之间使用“,”间隔
     *
     * @param stringList
     * @return
     */
    public static String listToString(List<String> stringList) {
        if (stringList == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean flag = false;
        for (String string : stringList) {
            if (flag) {
                result.append(",");
            } else {
                flag = true;
            }
            result.append(string);
        }
        return result.toString();
    }

    /**
     * 获取WebView使用的HTML代码  没有其他任何修饰的样式
     *
     * @param content 内容
     * @return
     */
    public static String getHtml(String content) {
        StringBuffer html = new StringBuffer();
        html.append("<!DOCTYPE HTML><html><body >");
        html.append(content);
        html.append("</body></html>");
        return html.toString();
    }

    /**
     * 修复图片出线内存溢出的情况
     *
     * @param url  图片的URL
     * @param view 展示的控件
     */
    public static void fixImage(String url, ImageView view) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap b = BitmapFactory.decodeFile(url, options);
        view.setImageBitmap(b);
    }

    /**
     * 动态添加控件
     *
     * @param context
     * @param time
     * @return
     */
    public static TextView getTimeView(Context context, String time) {
        TextView textView = new TextView(context);
        textView.setText(time);
        textView.setTextSize(12);
        textView.setTextColor(0x7f05001e);
        LinearLayout.LayoutParams layoutParams_txt = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams_txt.setMargins(0, 5, 0, 0);
        textView.setLayoutParams(layoutParams_txt);
        return textView;
    }

    /**
     * 验证是否是一个正确的邮箱地址
     *
     * @param email
     * @return
     */
    public static boolean validateEmail(String email) {
        //Pattern pattern = Pattern.compile("[0-9a-zA-Z]*.[0-9a-zA-Z]*@[a-zA-Z]*.[a-zA-Z]*", Pattern.LITERAL);  
        if (email == null) {
            return false;
        }
        //验证开始  
        //不能有连续的.  
        if (email.indexOf("..") != -1) {
            return false;
        }
        //必须带有@  
        int atCharacter = email.indexOf("@");
        if (atCharacter == -1) {
            return false;
        }
        //最后一个.必须在@之后,且不能连续出现  
        if (atCharacter > email.lastIndexOf('.') || atCharacter + 1 == email.lastIndexOf('.')) {
            return false;
        }
        //不能以.,@结束和开始  
        if (email.endsWith(".") || email.endsWith("@") || email.startsWith(".") || email.startsWith("@")) {
            return false;
        }
        return true;
    }

    /**
     * 实现文本复制功能
     *
     * @param content 内容
     */
    public static void copy(String content, Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
        Toast.makeText(context, "Copy Successed", Toast.LENGTH_SHORT).show();
    }

    /**
     * 实现粘贴功能
     *
     * @param context
     * @return
     */
    public static String paste(Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        return cmb.getText().toString().trim();
    }


    /**
     * 将文件转成base64 字符串
     *
     * @param path 文件路径
     * @return *
     * @throws Exception
     */

    public static String encodeBase64File(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return "";
            }
            FileInputStream inputFile = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            inputFile.read(buffer);
            inputFile.close();
            return new String(Base64.encode(buffer, Base64.DEFAULT));
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * @param context
     * @return
     */
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("app_info", Context.MODE_PRIVATE);
    }

    /**
     * 获取软键盘管理
     *
     * @param context
     * @return
     */
    public static InputMethodManager getManager(Context context) {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return manager;
    }


    //存储进SD卡
    public static void saveImageFile(Bitmap bm, String fileName) throws Exception {
        File file = new File(fileName);
        //检测图片是否存在
        if (file.exists()) {
            file.delete();  //删除原图片
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true));
        //100表示不进行压缩，70表示压缩率为30%
        bm.compress(Bitmap.CompressFormat.JPEG, 60, bos);
        bos.flush();
        bos.close();
    }


    /**
     * 根据提供的经度和纬度、以及半径，取得此半径内的最大最小经纬度
     */
    public static double[] getAround(double lat, double lon, int raidus) {

        Double latitude = lat;
        Double longitude = lon;

        Double degree = (24901 * 1609) / 360.0;
        double raidusMile = raidus;

        Double dpmLat = 1 / degree;
        Double radiusLat = dpmLat * raidusMile;
        Double minLat = latitude - radiusLat;
        Double maxLat = latitude + radiusLat;

        Double mpdLng = degree * Math.cos(latitude * (Math.PI / 180));
        Double dpmLng = 1 / mpdLng;
        Double radiusLng = dpmLng * raidusMile;
        Double minLng = longitude - radiusLng;
        Double maxLng = longitude + radiusLng;
        //System.out.println("["+minLat+","+minLng+","+maxLat+","+maxLng+"]");  
        return new double[]{new BigDecimal(minLat).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue(),
                new BigDecimal(minLng).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue(),
                new BigDecimal(maxLat).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue(),
                new BigDecimal(maxLng).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue()};
    }

    /**
     * 获取WIFI相关的信息
     *
     * @param context
     * @return
     */
    public static String getWifiInfo(Context context) {
        WifiAdmin mWifiAdmin = new WifiAdmin(context);
        if (mWifiAdmin.checkState() == 1) {
            return "w,0,";
        } else {
            StringBuffer sb = new StringBuffer();
            // 每次点击扫描之前清空上一次的扫描结果
//    	  	if(sb!=null){
//    	  		sb=new StringBuffer();
//    	  	}
            //开始扫描网络
            mWifiAdmin.startScan();
            List<ScanResult> list = mWifiAdmin.getWifiList();
            if (list != null && list.size() != 0) {
                if (list.size() > 25) {
                    sb.append("w,25,");
                    for (int i = 0; i < 25; i++) {
                        //得到扫描结果
                        ScanResult mScanResult = list.get(i);
                        sb.append((mScanResult.BSSID).replace(":", "") + ",");
                        sb.append((mScanResult.level) + ",");
//        	  			sb=sb.append(+"  ").append(mScanResult.SSID+"   ").append(mScanResult.capabilities+"   ").append(mScanResult.frequency+"   ").append(mScanResult.level+"\n\n");
//        	  			sb=sb.append(mScanResult.BSSID+"  ").append(mScanResult.SSID+"   ").append(mScanResult.capabilities+"   ").append(mScanResult.frequency+"   ").append(mScanResult.level+"\n\n");
                    }
//        	  		allNetWork.setText("扫描到的wifi网络：\n"+sb.toString());
//        	  		System.out.println("扫描到的wifi网络：\n"+sb.toString());
                    return sb.toString();
                } else {
                    sb.append("w," + list.size() + ",");
                    for (int i = 0; i < list.size(); i++) {
                        //得到扫描结果
                        ScanResult mScanResult = list.get(i);
                        sb.append((mScanResult.BSSID).replace(":", "") + ",");
                        sb.append((mScanResult.level) + ",");
//        	  			sb=sb.append(mScanResult.BSSID+"  ").append(mScanResult.SSID+"   ").append(mScanResult.capabilities+"   ").append(mScanResult.frequency+"   ").append(mScanResult.level+"\n\n");
//        	  			sb=sb.append(mScanResult.BSSID+"  ").append(mScanResult.SSID+"   ").append(mScanResult.capabilities+"   ").append(mScanResult.frequency+"   ").append(mScanResult.level+"\n\n");
                    }
//        	  		allNetWork.setText("扫描到的wifi网络：\n"+sb.toString());
//        	  		System.out.println("扫描到的wifi网络：\n"+sb.toString());
                    return sb.toString();
                }
            } else {
                return "w,0,";
            }
        }
    }


    private static Handler handler;

    /**
     * 获取手机基站信息
     *
     * @throws JSONException
     */
    public static String getGSMCellLocationInfo(final Context context) {
        try {
            final List<StationInfo> infoList = new ArrayList<StationInfo>();
            final StationInfo stationInfo = new StationInfo();
//   			int lastSignal=-80;
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            manager.listen(new PhoneStateListener() {

                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    super.onSignalStrengthsChanged(signalStrength);
                    int lastSignal = -113 + 2 * (signalStrength.getGsmSignalStrength()); //信号强度
//					Message message = new Message();
//					message.arg1 = lastSignal;
//					message.what = 0;
//					handler.sendMessage(message);
                    stationInfo.setSignalStrength(lastSignal + "");
//					infoList.add(stationInfo);
//					handler.sendEmptyMessage(0);

//					Toast.makeText(context, "Go to Firstdroid!!! GSM Cinr = " + String.valueOf(lastSignal), Toast.LENGTH_SHORT).show();
                }
            }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            handler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    if (msg.what == 0) {
//   						infoList.add(stationInfo);
//   						int signal = msg.arg1;
//   						stationInfo.setSignalStrength(signal+"");
//   						System.out.println("设置信号强度:"+signal);
                    }
                }

                ;
            };


            String operator = manager.getNetworkOperator();
            if (CommUtil.isBlank(operator)) {
                return Constants.DEFAULT_BLANK;
            }
            /**通过operator获取 MCC 和MNC */
            int mcc = Integer.parseInt(operator.substring(0, 3));
            int mnc = Integer.parseInt(operator.substring(3));
            //	System.out.println("获取基站信息得到的国家代码和信号类型:"+operator+"---mcc:"+mcc+"---mnc:"+mnc);
            if (mnc == 0 || mnc == 1) {
                // 中国移动和中国联通获取LAC、CID的方式
                GsmCellLocation location = (GsmCellLocation) manager.getCellLocation();
                int lac = location.getLac();
                int cellId = (location.getCid()) & 0xffff;
                stationInfo.setCid(cellId + "");
                stationInfo.setLac(lac + "");
                if (stationInfo.getSignalStrength() == null) {
                    stationInfo.setSignalStrength("-" + (int) (Math.random() * 100));
                }
                infoList.add(stationInfo);
                //      System.out.println("获取基站信息得到的基站编号:"+cellId+"----位置区域码:"+lac+"----信号强度:");
            } else if (mnc == 2) {
                /**通过CdmaCellLocation获取中国电信 LAC 和cellID */
                CdmaCellLocation location1 = (CdmaCellLocation) manager.getCellLocation();
                int lac = location1.getNetworkId();
                int cellId = location1.getBaseStationId() / 16;
                stationInfo.setCid(cellId + "");
                stationInfo.setLac(lac + "");
                if (stationInfo.getSignalStrength() == null) {
                    stationInfo.setSignalStrength("-" + (int) (Math.random() * 100));
                }
                infoList.add(stationInfo);
                //    System.out.println("获取基站信息得到的基站编号:"+cellId+"----位置区域码:"+lac+"----信号强度:");
            }


//   			GsmCellLocation location = (GsmCellLocation) manager.getCellLocation();
            /**通过GsmCellLocation获取中国移动和联通 LAC 和cellID */
//   			int lac = location.getLac();
//   			int cellid = location.getCid();
//   			System.out.println("基站ID："+cellid);
            /**通过CdmaCellLocation获取中国电信 LAC 和cellID */
//   	       CdmaCellLocation location1 = (CdmaCellLocation) mTelephonyManager.getCellLocation(); 
//   	       lac = location1.getNetworkId(); 
//   	      cellId = location1.getBaseStationId(); 
//   	      cellId /= 16;
//   			int strength = 0;
//   			/**通过getNeighboringCellInfo获取BSSS */
//   			List<NeighboringCellInfo> infoLists = manager.getNeighboringCellInfo();
//   			System.out.println("infoLists:"+infoLists+"     size:"+infoLists.size());
//   			for (NeighboringCellInfo info : infoLists) {
//   				strength+=(-133+2*info.getRssi());// 获取邻区基站信号强度 
//   				info.getLac();// 取出当前邻区的LAC 
//   				info.getCid();// 取出当前邻区的CID 
//   				System.out.println("信号强度rssi:"+info.getRssi()+"   strength:"+strength+"-=-=-=基站ID:"+info.getCid());
//   			}
            // 获取邻区基站信息
            List<NeighboringCellInfo> infos = manager.getNeighboringCellInfo();
            StringBuffer sb = new StringBuffer("总数 : " + infos.size() + "\n");
            if (infos.size() >= 6) {
                for (int i = 0; i < 6; i++) {
                    StationInfo stationInfo1 = new StationInfo();
                    stationInfo1.setSignalStrength((-113 + 2 * infos.get(i).getRssi()) + "");
                    stationInfo1.setCid(infos.get(i).getCid() + "");
                    stationInfo1.setLac(infos.get(i).getLac() + "");
                    infoList.add(stationInfo1);
                }
            } else {
                for (NeighboringCellInfo info1 : infos) { // 根据邻区总数进行循环
                    sb.append(" 邻区的LAC : " + info1.getLac()); // 取出当前邻区的LAC
                    sb.append(" 邻区的CID : " + info1.getCid()); // 取出当前邻区的CID
                    sb.append(" 邻区基站信号强度BSSS : " + (-113 + 2 * info1.getRssi()) + "\n"); // 获取邻区基站信号强度
                    StationInfo stationInfo1 = new StationInfo();
                    stationInfo1.setSignalStrength((-113 + 2 * info1.getRssi()) + "");
                    stationInfo1.setCid(info1.getCid() + "");
                    stationInfo1.setLac(info1.getLac() + "");
                    infoList.add(stationInfo1);
                }
            }
            // System.out.println("获取邻区基站信息:" + sb.toString());
            StringBuffer sbTemp = new StringBuffer();
            sbTemp.append("l," + infoList.size() + "," + mcc + "," + mnc + ",");
            for (int i = 0; i < infoList.size(); i++) {
                sbTemp.append(infoList.get(i).getCid() + "," + infoList.get(i).getLac() + "," + infoList.get(i).getSignalStrength() + ",");
            }
            return sbTemp.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 把一个View的对象转换成bitmap
     */
    public static Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        //能画缓存就返回false 
        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e("TAG", "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        // Restore the view 
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);
        return bitmap;
    }

    /**
     * 判断电话号码格式
     * 一键呼叫，拨打911，110，国内外手机号，座机
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^\\d{1,20}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    // 判断email格式是否正确
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);

        return m.matches();
    }

    public static String join(String join, List<String> strAry) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strAry.size(); i++) {
            if (i == (strAry.size() - 1)) {
                sb.append(strAry.get(i));
            } else {
                sb.append(strAry.get(i)).append(join);
            }
        }

        return new String(sb);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 手机震动
     *
     * @param activity
     * @param milliseconds
     */
    public static void Vibrate(final Activity activity, long milliseconds) {
        Vibrator vib = (Vibrator) activity
                .getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    /**
     * final Activity activity  ：调用该方法的Activity实例
     * long milliseconds ：震动的时长，单位是毫秒
     * long[] pattern  ：自定义震动模式 。数组中数字的含义依次是[静止时长，震动时长，静止时长，震动时长。。。]时长的单位是毫秒
     * boolean isRepeat ： 是否反复震动，如果是true，反复震动，如果是false，只震动一次
     * boolean flag:为true时震动，为false时不震动
     */
    public static void Vibrate(final Activity activity, long[] pattern, boolean isRepeat, boolean flag) {
        if (flag) {
            Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
            vib.vibrate(pattern, isRepeat ? 1 : -1);
        }
    }

    /**
     * 震动关闭
     *
     * @param activity
     */
    public static void closeVibrate(Activity activity) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.cancel();
    }

    /**
     * 图片转灰度
     *
     * @param bmpOriginal
     * @return
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * 将ImageView的图片转为灰色，ImageView必须是有图片的情况下才能用
     *
     * @param view
     */
    public static void setGrayImageView(ImageView view) {
        view.buildDrawingCache();
        BitmapDrawable drawable = (BitmapDrawable) view.getDrawable();
        if (null != drawable) {
            Bitmap image = drawable.getBitmap();
            if (null != image) {
                Bitmap bitmap = toGrayscale(image);
                view.setImageBitmap(bitmap);
            }
        }
    }

    public static Bitmap getBitmapFromView(ImageView view) {
        view.buildDrawingCache();
        BitmapDrawable drawable = (BitmapDrawable) view.getDrawable();
        if (null != drawable) {
            Bitmap image = drawable.getBitmap();
            return image;
        }
        return null;
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    /**
     * 强制帮用户打开GPS
     *
     * @param context
     */
    public static void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public static void openGPSManually(Context context) {
        LocationManager alm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "GPS模块正常", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(context, "请开启GPS！", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    public static String getPrettyDescribe(Context context, ContType type, String content, int maxChar) {
        String cont = Constants.DEFAULT_BLANK;
        if ((ContType.isTxtType(type) && CommUtil.isBlank(content)) || maxChar < 1) {
            return cont;
        }
        if (ContType.TXT == type) {
            cont = content.length() > (maxChar + 3) ? content.substring(0, maxChar - 3) + "..." : content;
            if (cont.startsWith("[zgif")) {
                cont = context.getString(R.string.msg_face);
            }
        } else if (ContType.PIC == type) {
            cont = context.getString(R.string.msg_pic);
        } else if (ContType.AUDIO == type) {
            cont = context.getString(R.string.msg_audio);
        } else if (ContType.VIDEO == type) {
            cont = context.getString(R.string.msg_video);
        } else if (ContType.FILE == type) {
            cont = context.getString(R.string.msg_file);
        } else if (ContType.isTxtType(type)) {
            cont = content.length() > (maxChar + 3) ? content.substring(0, maxChar - 3) + "..." : content;
        } else {
            cont = context.getString(R.string.msg_unkonow);
        }
        return cont;
    }
}
