package com.bct.gpstracker.my.adapter;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.my.activity.DSPSelectorActivity;
import com.bct.gpstracker.my.activity.WifiActivity;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.Music;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.service.PlayerService;
import com.bct.gpstracker.util.*;
import com.lurencun.service.autoupdate.internal.NetworkUtil;

public class DSPSelectorAdapter extends BaseAdapter {
    private DSPSelectorActivity activity;
    private Context mContext;
    private List<Music> list;
    private boolean isPlaying = false;
    private int currPosition = -1;
    private String imei;
    private int positionIdx;
    private SharedPreferences mSharedPreferences;

    public DSPSelectorAdapter(DSPSelectorActivity activity, List<Music> list, String termImei, int position) {
        this.activity = activity;
        mContext = activity;
        this.list = list;
        this.positionIdx = position;
        this.imei = termImei;
        mSharedPreferences = Utils.getPreferences(mContext);
    }

    public void refreshWifi() {
        getWifi();
    }

    static class ContViewHolder {
        TextView musicSerial, musicName, musicSinger;
        Button audition, download2Watch;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Music getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ContViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.activity_dsp_selector_item, null);
            holder = new ContViewHolder();
            convertView.setTag(holder);
            holder.musicSerial = (TextView) convertView.findViewById(R.id.music_serial);
            holder.musicName = (TextView) convertView.findViewById(R.id.music_name);
//            holder.musicSinger = (TextView) convertView.findViewById(R.id.music_singer);
            holder.audition = (Button) convertView.findViewById(R.id.audition);
            holder.download2Watch = (Button) convertView.findViewById(R.id.download2watch);
        } else {
            holder = (ContViewHolder) convertView.getTag();
        }
        final Music music = list.get(position);
        holder.musicSerial.setText(music.getSerial() < 10 ? "0" + music.getSerial() : CommUtil.toStr(music.getSerial()));
        holder.musicName.setText(music.getName());
//        holder.musicSinger.setText(item.getSinger());
        holder.audition.setText(music.isPlaying() ? R.string.stop : R.string.audition);
        holder.audition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int networkType = NetworkUtil.getNetworkType(mContext);
                //判断网络类型
                if (networkType == NetworkUtil.NOCONNECTION) {
                    //无连接
                    Toast.makeText(mContext, R.string.network_err, Toast.LENGTH_SHORT).show();
                } else if (networkType == NetworkUtil.MOBILE) {
                    //移动网络
                    if (!mSharedPreferences.getBoolean(MyConstants.USE_NETWORK, false)) {
                        //提醒用户设置wifi
                        setupPhoneWifi();
                    }
                }
                if (networkType == NetworkUtil.WIFI || mSharedPreferences.getBoolean(MyConstants.USE_NETWORK, false)) {
                    //wifi,直接加载数据
                    final Button btn = (Button) v;
                    final PlayerService player = PlayerService.getPlayer();
                    if (player != null) {
                        if (!isPlaying) {
                            playMusic(v, player, position);
                        } else if (currPosition == position) {
                            isPlaying = false;
                            music.setPlaying(false);
                            if (player != null) {
                                player.stop();
                                btn.setText(R.string.audition);
                            }
                        } else {
                            player.stop();
                            for (Music m : list) {
                                m.setPlaying(false);
                            }
                            music.setPlaying(true);
                            notifyDataSetChanged();
                            playMusic(v, player, position);
//                        Toast.makeText(activity,R.string.playing,Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        holder.download2Watch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSharedPreferences.getBoolean(MyConstants.TERMINAL_NETWORK, false)) {
                    setupHomeWifi(position);
                } else {
                    download2Watch(list.get(position).getId());
                }

            }
        });
        return convertView;
    }


    private void playMusic(View v, final PlayerService player, int position) {
        final Button btn = (Button) v;
        Music music = list.get(position);
        isPlaying = true;
        currPosition = position;
        player.setCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (player != null) {
                    player.stop();
                    btn.setText(R.string.audition);
                }
            }
        });
        player.setPath(music.getUrl());
        int res = player.play();
        if (res == 0) {
            btn.setText(R.string.stop);
            Toast.makeText(activity, R.string.waiting, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, R.string.player_err, Toast.LENGTH_SHORT).show();
            player.stop();
        }
    }

    private void download2Watch(int id) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("audioId", id);
            json.putOpt("termseq", positionIdx + 1);
            json.putOpt("imei", imei);
            Log.d(Constants.TAG, "下载音乐请求信息：id:" + id + " termseq:" + positionIdx + 1 + " imei:" + imei);
            BctClientCallback callback = new BctClientCallback() {
                @Override
                public void onStart() {
                    CommUtil.showProcessing(activity.getMusicListView(), true, true);
                }

                @Override
                public void onFinish() {
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    CommUtil.hideProcessing();
                    if (obj.getRetcode() != 1) {
                        Toast.makeText(activity, R.string.load_audio_list_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(activity, R.string.download_succ, Toast.LENGTH_SHORT).show();
                    activity.finish();
                }

                @Override
                public void onFailure(String message) {
                    CommUtil.hideProcessing();
                    Toast.makeText(activity, R.string.load_audio_list_failed, Toast.LENGTH_SHORT).show();
                }
            };
            BctClient.getInstance().POST(activity, CommonRestPath.sendAudio(), json, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception e) {
            Log.e(Constants.TAG, "下载到手表失败！", e);
        }
    }

    private void getWifi() {
        Device.getWifi(mContext, new BctClientCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) {
                //接收的wifi信息会自动排序，所以只需判断第一个wifi是否存在即可
                if (obj.getRetcode() == 1) {
                    JSONObject json = JSONHelper.getJSONObject(obj.getBodyArray(), 0);
                    if (CommUtil.isBlank(JSONHelper.getString(json, "wifi"))) {
                        //如果为空，则说明没有wifi账号信息
                        mSharedPreferences.edit().putBoolean(MyConstants.HOME_WIFI_MSG, false).apply();
                    } else {
                        //存在账号信息
                        mSharedPreferences.edit().putBoolean(MyConstants.HOME_WIFI_MSG, true).apply();
                    }
                } else {
                    Toast.makeText(mContext, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {

            }
        });
    }

    private void setupPhoneWifi() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.listen_ringgtones).setMessage(R.string.download_remind);
        dialog.setPositiveButton(R.string.setup_wifi, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //设置wifi
                mContext.startActivity(new Intent().setAction("android.net.wifi.PICK_WIFI_NETWORK").putExtra("extra_prefs_show_button_bar", true)
                        .putExtra("extra_prefs_set_back_text", mContext.getString(R.string.cancel))
                        .putExtra("extra_prefs_set_next_text", mContext.getString(R.string.complete))
                        .putExtra("wifi_enable_next_on_connect", true));
            }
        }).setNegativeButton(R.string.use_gprs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSharedPreferences.edit().putBoolean(MyConstants.USE_NETWORK, true).apply();
                //使用流量
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void setupHomeWifi(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.download_music).setMessage(R.string.download_remind_note);
        dialog.setPositiveButton(R.string.setup_home_wifi, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //设置wifi
                mContext.startActivity(new Intent(mContext, WifiActivity.class));
            }
        });
        if (mSharedPreferences.getBoolean(MyConstants.HOME_WIFI_MSG, false)) {
            dialog.setNegativeButton(R.string.start_download, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //直接下载
                    mSharedPreferences.edit().putBoolean(MyConstants.TERMINAL_NETWORK, true).apply();
                    download2Watch(list.get(position).getId());
                    dialog.dismiss();
                }
            });
        }
        AlertDialog alertDialog = dialog.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

}
