package com.bct.gpstracker.util;

import java.io.File;
import java.text.DecimalFormat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;

import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lurencun.service.autoupdate.Version;

/**
 * Created by lc on 2015/10/9 0009.
 * 下载app的控制类
 */
public class AppUpdataHandle {
    /**
     * 定义通知管理者对象
     */
    private NotificationManager manager;

    /**
     * 定义通知对象
     */
    private Notification notification;

    /**
     * 通知的id
     */
    private int notificationId = 1001;
    private boolean isDownloading = false;
    private final int NET_STATE_NORMAL = 20001;
    private final int NET_STATE_WIFI = 20002;
    private final int NET_STATE_3G = 20003;
    private int networkState = NET_STATE_NORMAL;
    private boolean isRegistReceive = false; //广播是否注册


    /**
     * 通知栏默认显示的View
     */
    private RemoteViews contentView;
    private static final String STATUS_BAR_COVER_CLICK_ACTION = "STATUS_BAR_COVER_CLICK_ACTION";
    private static final String PROGRESS_STOP = "progress_stop";

    private String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "Download" + File.separator + "GpsTracker.apk";

    private Context context;
    private Version version;
    private HttpHandler<File> httpHandler;
    private HttpUtils httpUtils;

    private AppUpdataHandle(Context context) {
        this.context = context;
        httpUtils = new HttpUtils();
    }

    private static AppUpdataHandle instance = null;

    public static synchronized AppUpdataHandle getInstance(Context context) {
        if (instance == null) {
            instance = new AppUpdataHandle(context);
        }
        return instance;
    }

    public void downloadAndInstall(Version version) {
        this.version = version;
        startDownloading();
    }

    /**
     * 从头开始下载
     */
    private void startDownloading() {
        networkState = NET_STATE_NORMAL; //每次下载的时候都让其恢复成默认
        createNotification();
        httpDownload(false);
        isRegistReceive = false;
    }

    /**
     * @param flat    是否接着之前的下载
     */
    private void httpDownload(boolean flat) {
        Log.d(Constants.TAG, "apkPath:" + apkPath);
        httpHandler = httpUtils.download(version.targetUrl, apkPath, flat, false, new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                Log.d(Constants.TAG, "下载结束,开始安装");


                File file = new File(apkPath);
                Uri uri = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                notification.setLatestEventInfo(context, "当当熊", "下载成功，点击安装", pendingIntent);
                context.unregisterReceiver(onClickReceiver);
                manager.notify(notificationId, notification);

                if (myNetReceiver != null) {
                    if (isRegistReceive == true) {
                        context.unregisterReceiver(myNetReceiver);
                        isRegistReceive = false;
                    }
                }


                File apkFile = new File(apkPath);
                Intent installIntent = new Intent();
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                installIntent.setAction(android.content.Intent.ACTION_VIEW);
                installIntent.addCategory("android.intent.category.DEFAULT");
                installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                context.startActivity(installIntent);
            }

            @Override
            public void onFailure(HttpException e, String s) {


                if (myNetReceiver != null) {
                    if (isRegistReceive == true) {
                        context.unregisterReceiver(myNetReceiver);
                        isRegistReceive = false;
                    }
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                Log.d(Constants.TAG, current + "/" + total);
                Log.d(Constants.TAG, "下载的百分比：" + (int) (((double) current / (double) total) * 100));
                contentView.setTextViewText(R.id.total, "/" + doubleFormat((double) total / (1024 * 1024)) + "M");
                contentView.setTextViewText(R.id.percent, (int) (((double) current / (double) total) * 100) + "%");

                contentView.setProgressBar(R.id.progress_num, 100, (int) ((double) current / (double) total * 100),
                        false);
                contentView.setTextViewText(R.id.download, doubleFormat((double) current / (1024 * 1024)) + "M");
                // show_view
                manager.notify(notificationId, notification);
            }

            @Override
            public void onStart() {
                Log.d(Constants.TAG, "http开始下载");
                isDownloading = true;

                //开始下载后
//                networkState = NET_STATE_WIFI;


                /////////动态注册广播
                IntentFilter mFilter = new IntentFilter();
                mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                context.registerReceiver(myNetReceiver, mFilter);
                isRegistReceive = true;

            }

            @Override
            public void onCancelled() {
                Log.d(Constants.TAG, "下载的http删除了");

                if (myNetReceiver != null) {
                    if (isRegistReceive == true) {
                        context.unregisterReceiver(myNetReceiver);
                        isRegistReceive = false;
                    }
                }
            }


        });
    }

    /**
     * 创建通知的方法
     */
    private void createNotification() {
        if (null != httpHandler) {
            httpHandler.cancel();
            httpHandler = null;
        }
        Log.d(Constants.TAG, "创建通知");
        // 实例化通知管理者
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
        // 实例化通知对象
        notification = new Notification();
        // 这个图标必须要设置，不然下面那个RemoteViews不起作用.
        notification.icon = R.drawable.ic_launcher;
        // 这个参数是通知提示闪出来的值.
        notification.tickerText = "开始下载";

        //加载自定义布局
        contentView = new RemoteViews(context.getPackageName(), R.layout.update);
//        contentView.setTextViewText(R.id.download_states, "正在下载");
        contentView.setTextViewText(R.id.percent, "0.0%");
        contentView.setProgressBar(R.id.progress_num, 100, 0, false);
        contentView.setImageViewBitmap(R.id.progress_btn, BitmapFactory.decodeResource(context.getResources(), R.drawable.updata_pause));


        //广播只注册一次
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(STATUS_BAR_COVER_CLICK_ACTION);
        intentFilter.addAction(PROGRESS_STOP);
        context.registerReceiver(onClickReceiver, intentFilter);

        Intent buttonIntent = new Intent(STATUS_BAR_COVER_CLICK_ACTION);
        PendingIntent pendButtonIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, 0);
        contentView.setOnClickPendingIntent(R.id.progress_btn, pendButtonIntent);

        buttonIntent = new Intent(PROGRESS_STOP);
        pendButtonIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, 0);
        contentView.setOnClickPendingIntent(R.id.progress_stop, pendButtonIntent);

        //R.id.trackname为你要监听按钮的id
//            contentView.setOnClickPendingIntent(R.id.trackname, pendButtonIntent);


        notification.contentView = contentView;
        notification.flags = Notification.FLAG_AUTO_CANCEL;


        manager.notify(notificationId, notification);
    }

    /**
     * 将字节流转换成M的转换工具
     *
     * @param d
     * @return
     */
    public String doubleFormat(double d) {
        DecimalFormat df = new DecimalFormat("0.##");
        return df.format(d);
    }

    BroadcastReceiver onClickReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(STATUS_BAR_COVER_CLICK_ACTION)) {
                //在这里处理点击事件
                Log.d(Constants.TAG, "按钮点击了");
                if (isDownloading) {
                    pauseUpdata(context);
                } else {
                    resumeUpdata(context);
                }
            } else if (intent.getAction().equals(PROGRESS_STOP)) {
                Log.d(Constants.TAG, "停止按钮点击了");
                manager.cancel(notificationId);
                if (null != httpHandler) {
                    httpHandler.cancel();
                    httpHandler = null;
                }
                if (myNetReceiver != null) {
                    if (isRegistReceive == true) {
                        context.unregisterReceiver(myNetReceiver);
                        isRegistReceive = false;
                    }
                }

            }
        }
    };

    /**
     * 继续下载
     *
     * @param context
     */
    private void resumeUpdata(Context context) {
        httpDownload(true);
        isDownloading = true;
        contentView.setImageViewBitmap(R.id.progress_btn, BitmapFactory.decodeResource(context.getResources(), R.drawable.updata_pause));
        manager.notify(notificationId, notification);
    }

    /**
     * 暂停下载
     *
     * @param context
     */
    private void pauseUpdata(Context context) {
        if (null != httpHandler) {
            httpHandler.cancel();
            httpHandler = null;
        }
        isDownloading = false;
        contentView.setImageViewBitmap(R.id.progress_btn, BitmapFactory.decodeResource(context.getResources(), R.drawable.updata_play));
        manager.notify(notificationId, notification);
    }


    /////////////监听网络状态变化的广播接收器
    private ConnectivityManager mConnectivityManager;

    private NetworkInfo netInfo;
    private BroadcastReceiver myNetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                netInfo = mConnectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isAvailable()) {
                    Log.d(Constants.TAG, "有网络连接");
                    /////////////网络连接
                    String name = netInfo.getTypeName();

                    if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        Log.d(Constants.TAG, "wifi网络连接");
                        if (networkState == NET_STATE_3G) {
                            //之前的网络为3g，又换回了wifi，则继续下载
                            resumeUpdata(context);
//                            networkState = NET_STATE_WIFI;
                        }
                        networkState = NET_STATE_WIFI;

                    } else if (netInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                        /////有线网络

                    } else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        Log.d(Constants.TAG, "3g网络连接");
                        if (networkState == NET_STATE_WIFI) {
                            //之前的网络为wifi,现在改为3g网络，则必须停止下载
                            pauseUpdata(context);
                        }
                        networkState = NET_STATE_3G;
                    }

                    Log.d(Constants.TAG, "当前网络连接状态:" + networkState);
                } else {
                    ////////网络断开

                }
            }

        }
    };




}
