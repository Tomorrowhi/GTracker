package com.bct.gpstracker.common;


import java.text.SimpleDateFormat;

public final class Constants {

    private Constants() {
        // hide me
    }

    public static final String TAG = "BCT";

    public static final String DEFAULT_BLANK = "";

    public static final SimpleDateFormat COMM_DATETIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat COMM_DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat COMM_TIME_FMT = new SimpleDateFormat("HH:mm:ss");

//    public static final String[] STS=new String[]{"MQNAW","HXNDX","WSZDANXD","WXNGZQM","MQIMYSM"};

    /**
     * 经纬度坐标组分隔符
     */
    public static final String POSI_GROUP_SEP = ";";
    /**
     * 经纬度坐标分隔符
     */
    public static final String POSI_ITEM_SEP = ",";

    /**
     * 地球半径（单位：米），来自NASA网站：
     * http://nssdc.gsfc.nasa.gov/planetary/factsheet/earthfact.html
     */
    public static final Double EARTH_RADIUS = 6371000D;

    public static final String REFRESH_FLAG = "REFRESH_FLAG";

    public static final int NOTIFIACTION_ID = 19831022;

    public static final int HEART_BEAT_INTERVAL = 60000;

    public static final int RECEIVE_INTERVAL = 1000;

    public static final int PACKAGE_BODY_LENGTH = 3072;

    public static boolean hasNewVersion = false;

    public static final String UPDATE_SESSION_LOCATION="UPD_SESSION_LOC";

    public static final float MAX_PIC_WIDTH_HEIGHT = 1200f;

    public final static int CHAT_MSG = 9990;
    public final static int CHAT_LOGIN = 9991;

    public static boolean serviceRunning = false;

    public static final String ACTION_NEW_MSG = "com.xx.MSG_RECEIVED";

    public static final String ACTION_COMM_SERVICE = "com.xx.communicationService";

    public static final String PACKAGER = "com.bct.gpstracker";

    public static final String REGX_IP_PORT = "^(\\d{1,3}\\.){3}\\d{1,3}(\\:|\\ )\\d{3,5}$";

    public static final String REGX_EMOJI = "\\[zgif\\d+?\\.gif\\]";

    public final static String PROTOCOL_KEY = "USER_PROTOCOL";

    public static String baseUrl;

    public static final long MSG_TIMEOUT = 30000;//30秒

    public static final int MAX_RECEIVE_LENGTH = 1048576;//1MB

    public static final String SPELLING_SEPARATOR = " ";

    //EVENT_TAG
    public static final String EVENT_TAG_CHAT_PROGRESS = "EVENT_TAG_CHAT_PROGRESS";
    public static final String EVENT_TAG_CHAT_SEND = "EVENT_TAG_CHAT_SEND";
    public static final String EVENT_TAG_CHAT_DISPLAYMSG = "EVENT_TAG_CHAT_DISPLAYMSG";
    public static final String EVENT_TAG_UNREAD_DATA = "EVENT_TAG_UNREAD_DATA";
    public static final String EVENT_TAG_UNREAD_DATA_AFTER = "EVENT_TAG_UNREAD_DATA_AFTER";
    public static final String EVENT_TAG_POSI_NOTIFY = "EVENT_TAG_POSI_NOTIFY";
    public static final String EVENT_TAG_UPDATE_ACCOUNT = "EVENT_TAG_UPDATE_ACCOUNT";
    public static final String EVENT_TAG_UPDATE_TEL_ACCOUNT = "EVENT_TAG_UPDATE_TEL_ACCOUNT";
    public static final String EVENT_TAG_DELETE_TEL_ACCOUNT = "EVENT_TAG_DELETE_TEL_ACCOUNT";
    public static final String EVENT_TAG_TERM_STATUS = "EVENT_TAG_TERM_STATUS";
    public static final String EVENT_TAG_TERM_BIND = "EVENT_TAG_TERM_BIND";
    public static final String EVENT_TAG_OFFLINE_NOTIFY = "EVENT_TAG_OFFLINE_NOTIFY";
    public static final String EVENT_TAG_AUDIO_STATUS = "EVENT_TAG_AUDIO_STATUS";

    public static final String PIC_PATH = "PIC_PATH";

    public static final int NULL_VALUE = 9999;    //无意义参数
    public static final int COMMENT_FILE = 399;    //启动消息界面的标识
    public static final int PUBLISH_COMMENT = 399;    //启动消息界面的标识

    public static final String MSG_VOICE = "MSG_VOICE";
    public static final String MSG_VIBRATE = "MSG_VIBRATE";

    public static final String VOLUME = "volume"; //音量调整的SP key

    public static final String BLE_CONNECTED_FAIL = "ble_connected_fail";//蓝牙连接失败
    public static final String BLE_ACTION_DISCONNECTED = "ble_action_disconnected"; //蓝牙设备已断开
    public static final String VIBRATOR_CLOSE = "vibrator_close"; //震动关闭的广播
    public static final String CONNECT_BLE = "connectble"; //BtActivity用于连接蓝牙的action
    public static boolean isBlutActivityOpen = false;
    public static final String BLE_ADDRESS = "ble_address"; //用于intent传蓝牙mac的key
    public static boolean isBleConnected = false; //蓝牙是否处理连接状态
    public static boolean isActivityConnected = false;  //蓝牙连接是否是主动断开
    public static boolean isBleDisconnected = false; //蓝牙是否断开

    public static final int REQUEST_INTERVAL = 5000;

    public static final int CAMERA_REQUEST_CODE = 11001; //调用像机的请求码
    public static final int RESULT_REQUEST_CODE = 11002; //调用图片裁剪的请求码

    /**
     * 作为title显示到手表的最大ASCII字符长度
     * 一个非ASCII字符算两个ASCII字符
     */
    public static final int MAX_WATCH_TITLE_DISPLAY_LEN = 6;

    public static final int ALARM_MAX_TITLE_LEN = 10; //闹钟标题的最大ASCII字符长度
    public static final int ALARM_MAX_CONTENT_LEN = 30; //闹钟内容的最大ASCII字符长度


    public static final int VALIDCODE_TOTAL_TIME = 120000; //获取验证码的倒计时时长，单位毫秒
    public static final int VALIDCODE_APART_TIME = 1000; //计时的时间间隔，1000毫秒

    public static final String SETTING_SERVER_IP="server_ip";
    public static final String SETTING_SERVER_URL="server_url";
    public static final String SETTING_NAVI_MAP="navi_map";

    public static final String MANAGER_USER_NUM="1";

    public static final int SORT_MANAGER=1;
    public static final int SORT_KEEPER=5;
    public static final int SORT_FRIEND=10;
    public static final int SORT_MONITOR_OBJECT=15;

    public static final int MAP_LEVEL=16;

    public static final String FILE_SCHEME="file://";
    public static int VOICE_MAX_TIME = 15; // 最长录制时间，单位秒，0为无时间限制
}