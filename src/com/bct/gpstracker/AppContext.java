package com.bct.gpstracker;

import java.io.*;
import java.util.LinkedList;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.simple.eventbus.EventBus;

import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.CrashHandler;
import com.bct.gpstracker.listener.XXDbUpgradeListener;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.lidroid.xutils.DbUtils;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

/**
 * Created by HH
 * Date: 2015/6/24 0024
 * Time: 下午 8:26
 */
public class AppContext extends Application {
    private static AppContext context;
    public static DbUtils db = null;
    public static boolean isEntered = false;
    private static EventBus eventBus;
    public static boolean forceLogout = false;
    public static LinkedList<Long> newChatMsgIds = new LinkedList<>();
    public static boolean managerInfoChecked=false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        //全局异常捕获
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        eventBus = EventBus.getDefault();
        String dbName = getMetaData(this, "DB_NAME");
        int dbVersion = getMetaData(this, "DB_VERSION");
        copyAttachedDatabase(this, dbName);
        db = DbUtils.create(this, dbName, dbVersion, new XXDbUpgradeListener());
        if (CommUtil.isBlank(Constants.baseUrl)) {
            Constants.baseUrl = getMetaData(this, "base_url");
        }
        ImageLoader.getInstance().init(getImageLoaderConfiguration());
    }

    public static Context getContext() {
        return context;
    }

    public static EventBus getEventBus() {
        return eventBus;
    }


    public static <T> T getMetaData(Context context, String name) {
        try {
            final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                return (T) ai.metaData.get(name);
            }
        } catch (Exception e) {
            Log.w(Constants.TAG, "Couldn't find meta-data: " + name);
        }

        return null;
    }

    public void copyAttachedDatabase(Context context, String dbName) {
        final File dbPath = context.getDatabasePath(dbName);

        // If the database already exists, return
        if (dbPath.exists()) {
//            dbPath.delete();
            return;
        }

        // Make sure we have a path to the file
        dbPath.getParentFile().mkdirs();

        // Try to copy database file
        try {
            final InputStream inputStream = context.getAssets().open(dbName);
            final OutputStream output = new FileOutputStream(dbPath);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Failed to open file", e);
        }
    }

    public ImageLoaderConfiguration getImageLoaderConfiguration() {
        return new ImageLoaderConfiguration.Builder(context)
                .memoryCacheExtraOptions(300, 300) // default = device screen dimensions
//                .diskCacheExtraOptions(500, 500, null)
                .threadPoolSize(5) // default
                .threadPriority(Thread.NORM_PRIORITY - 1) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .diskCache(new UnlimitedDiskCache(new File(Utils.getAvailableStoragePath() + "pic"))) // default
                .diskCacheSize(100 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .imageDownloader(new BaseImageDownloader(context)) // default
                .imageDecoder(new BaseImageDecoder(true)) // default
                .defaultDisplayImageOptions(new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build())
//                .writeDebugLogs()
                .build();
    }
}
