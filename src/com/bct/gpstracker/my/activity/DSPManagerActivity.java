package com.bct.gpstracker.my.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.simple.eventbus.Subscriber;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.fix.swipemenu.*;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.my.adapter.DSPAdapter;
import com.bct.gpstracker.pojo.Music;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.service.PlayerService;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.JsonHttpResponseHelper;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.vo.Msg;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnItemClick;
import com.lurencun.service.autoupdate.internal.NetworkUtil;

/**
 *
 */
public class DSPManagerActivity extends BaseActivity {

    @ViewInject(R.id.dsp_manager_ok_get_data)
    private LinearLayout getData;
    @ViewInject(R.id.dsp_manager_no_get_data)
    private RelativeLayout noGetData;

    private SwipeMenuListView musicListView;
    private DSPAdapter dspAdapter;
    private List<Music> musicList = new ArrayList<>();
    private boolean isPlaying = false;
    private int playingIdx = -1;
    private String termImei;
    public static final String RE_DOWNLOAD_TAG = "RE_DOWNLOAD_TAG";
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(DSPManagerActivity.this, PlayerService.class);
        stopService(intent);
        AppContext.getEventBus().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsp_manager);
        ViewUtils.inject(this);
        AppContext.getEventBus().register(this);
        mContext = DSPManagerActivity.this;

        termImei = getIntent().getStringExtra("IMEI");
        mSharedPreferences = Utils.getPreferences(mContext);
        //设置默认网络标识
        mSharedPreferences.edit().putBoolean(MyConstants.USE_NETWORK, false).apply();
        mSharedPreferences.edit().putBoolean(MyConstants.TERMINAL_NETWORK, false).apply();
        mSharedPreferences.edit().putBoolean(MyConstants.HOME_WIFI_MSG, false).apply();

        dspAdapter = new DSPAdapter(this, musicList, termImei);
        musicListView = (SwipeMenuListView) findViewById(R.id.music_listview);
        musicListView.setAdapter(dspAdapter);
        musicListView.setMenuCreator(creator);
        musicListView.setOnMenuItemClickListener(onMenuItemClickListener);
        musicListView.setViewProcessor(new SwipeMenuListView.ViewProcessor() {
            @Override
            public void execute(SwipeMenuLayout layout, int position) {
                Music music = musicList.get(position);
                LinearLayout linearLayout = (LinearLayout) layout.findViewWithTag(DSPManagerActivity.RE_DOWNLOAD_TAG);
                if (music.getStatus() == 2 && linearLayout != null) {
                    linearLayout.setVisibility(View.VISIBLE);
                } else if (linearLayout != null) {
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(DSPManagerActivity.this).inflate(R.layout.remind_watch_song, null);
        dialog.setView(view).show();

    }

    private void setupWifi(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.listen_ringgtones).setMessage(R.string.download_remind);
        dialog.setPositiveButton(R.string.setup_wifi, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //设置wifi
                startActivity(new Intent().setAction("android.net.wifi.PICK_WIFI_NETWORK").putExtra("extra_prefs_show_button_bar", true)
                        .putExtra("extra_prefs_set_back_text", getString(R.string.cancel))
                        .putExtra("extra_prefs_set_next_text", getString(R.string.complete))
                        .putExtra("wifi_enable_next_on_connect", true));
            }
        }).setNegativeButton(R.string.use_gprs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //添加网络标识
                mSharedPreferences.edit().putBoolean(MyConstants.USE_NETWORK, true).apply();
                //使用流量
                playMusic(position);
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isPlaying = false;
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }




    @Override
    protected void onResume() {
        PlayerService player = PlayerService.getPlayer();
        if (player != null) {
            player.stop();
        } else {
            Intent intent = new Intent(DSPManagerActivity.this, PlayerService.class);
            startService(intent);
        }
        isPlaying = false;

        loadPage();
        super.onResume();
    }

private SwipeMenuCreator creator = new SwipeMenuCreator() {
    @Override
    public void create(SwipeMenu menu) {
        SwipeMenuItem auditionItem = new SwipeMenuItem(AppContext.getContext());
        auditionItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
        auditionItem.setWidth(Utils.dp2px(AppContext.getContext(), 60f));
        auditionItem.setTitle(R.string.audition);
        auditionItem.setTitleSize(18);
        auditionItem.setTitleColor(Color.WHITE);
        menu.addMenuItem(auditionItem);

        SwipeMenuItem updateItem = new SwipeMenuItem(AppContext.getContext());
        updateItem.setBackground(new ColorDrawable(Color.parseColor("#FF924E")));
        updateItem.setWidth(Utils.dp2px(AppContext.getContext(), 60f));
        updateItem.setTitle(R.string.update);
        updateItem.setTitleSize(18);
        updateItem.setTitleColor(Color.WHITE);
        menu.addMenuItem(updateItem);

        SwipeMenuItem downItem = new SwipeMenuItem(AppContext.getContext());
        downItem.setBackground(new ColorDrawable(Color.parseColor("#9F3131")));
        downItem.setWidth(Utils.dp2px(AppContext.getContext(), 60f));
        downItem.setTitle(R.string.re_download);
        downItem.setTitleSize(18);
        downItem.setTitleColor(Color.WHITE);
        downItem.setTag(RE_DOWNLOAD_TAG);
        menu.addMenuItem(downItem);
    }
};

private SwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener = new SwipeMenuListView.OnMenuItemClickListener() {
    @Override
    public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
        switch (index) {
            case 0:
                if (!isPlaying) {
                    newWorkJudge(position);
                } else if (position == playingIdx) {
                    stopMusic(position);
                } else {
                    Toast.makeText(DSPManagerActivity.this, R.string.playing, Toast.LENGTH_SHORT).show();
                    for (Music m : musicList) {
                        m.setPlaying(false);
                    }
                    dspAdapter.notifyDataSetChanged();
                }
                break;
            case 1:
                Intent intent = new Intent(DSPManagerActivity.this, DSPSelectorActivity.class);
                intent.putExtra("POSITION", position);
                intent.putExtra("IMEI", termImei);
                startActivity(intent);
                break;
            case 2: //重新下载的点击事件
                //提示

                final Music music = musicList.get(position);
                if (music.getStatus() == 2) { //表示下载失败，要重新下载
                    setHomeWifi(position, music);
                } else if (music.getStatus() == 0) {
                    Toast.makeText(DSPManagerActivity.this, R.string.no_again_download, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DSPManagerActivity.this, R.string.success_download, Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
        return false;
    }
};

    private void setHomeWifi(final int position, final Music music) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.download_music).setMessage(R.string.download_error_note);
        dialog.setPositiveButton(R.string.setup_home_wifi, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //设置wifi
                mContext.startActivity(new Intent(mContext, WifiActivity.class));
            }
        });
        dialog.setNegativeButton(R.string.start_download, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //直接下载
                mSharedPreferences.edit().putBoolean(MyConstants.TERMINAL_NETWORK, true).apply();
                download2Watch(music.getId(),position);
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void newWorkJudge(int position) {
        int networkType = NetworkUtil.getNetworkType(mContext);
        //判断网络类型
        if (networkType == NetworkUtil.NOCONNECTION) {
            //无连接
            Toast.makeText(DSPManagerActivity.this, R.string.network_err, Toast.LENGTH_SHORT).show();
        } else if (networkType == NetworkUtil.MOBILE) {
            //移动网络
            if (mSharedPreferences.getBoolean(MyConstants.USE_NETWORK, false)) {
                //使用移动网络，加载数据
                playMusic(position);
            } else {
                //提醒用户设置wifi
                setupWifi(position);
            }
        } else {
            //wifi,直接加载数据
            playMusic(position);
        }
    }

    private void playMusic(final int position) {

        final Music music = musicList.get(position);
        if (CommUtil.isBlank(music.getUrl()) || !music.getUrl().toUpperCase().startsWith("HTTP://")) {
            Log.w(Constants.TAG, "音频播放地址无效:" + music.getUrl());
            return;
        }

        isPlaying = true;
        View itemView = musicListView.getChildAt(position);
        View playView = itemView.findViewById(R.id.playing);
        TextView swipeTextView = (TextView) itemView.findViewWithTag(SwipeMenuView.SWIPE_MEUN_VIEW_TEXT_TAG);
        swipeTextView.setText(R.string.stop);
        playView.setVisibility(View.VISIBLE);
        playingIdx = position;

        playView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMusic(position);
            }
        });

        PlayerService player = PlayerService.getPlayer();
        if (player != null) {
            player.setCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopMusic(position);
                }
            });
            player.setPath(music.getUrl());
            int res = player.play();
            if (res == 0) {
                Toast.makeText(DSPManagerActivity.this, R.string.waiting, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DSPManagerActivity.this, R.string.player_err, Toast.LENGTH_SHORT).show();
                player.stop();
            }
        }
    }

    private void stopMusic(int position) {
        isPlaying = false;
        View itemView = musicListView.getChildAt(position);
        View playView = itemView.findViewById(R.id.playing);
        TextView swipeTextView = (TextView) itemView.findViewWithTag(SwipeMenuView.SWIPE_MEUN_VIEW_TEXT_TAG);
        swipeTextView.setText(R.string.audition);
        playView.setVisibility(View.GONE);

        PlayerService player = PlayerService.getPlayer();
        if (player != null) {
            player.stop();
        }
    }

    private void loadPage() {
        try {
            if (musicListView.isShown()) {
                CommUtil.showProcessing(musicListView, true, false);
            } else {
                CommUtil.showProcessing(musicListView, true, true);
            }
            JSONObject json = new JSONObject();
            json.put("imei", termImei);
            BctClientCallback callback = new BctClientCallback() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFinish() {
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    CommUtil.hideProcessing();
                    if (obj.getRetcode() != 1) {
                        Toast.makeText(DSPManagerActivity.this, R.string.load_audio_list_failed, Toast.LENGTH_SHORT).show();
                        getData.setVisibility(View.GONE);
                        noGetData.setVisibility(View.VISIBLE);
                        return;
                    }
                    try {
                        JSONArray array = obj.getBodyArray();
                        if (array != null && array.length() > 0) {
                            musicList.clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                Music music = new Music();
                                music.setId(jsonObject.getInt("audioId"));
                                music.setUrl(jsonObject.optString("audioUrl"));
                                String name = jsonObject.optString("audioName");
                                int idx;
                                if (CommUtil.isNotBlank(name) && (idx = name.indexOf('_')) != -1) {
                                    name = name.substring(idx + 1, name.length());
                                }
                                music.setName(name);
//                                music.setFileSize(jsonObject.optInt("audioSize"));
                                music.setSerial(i + 1);
                                music.setStatus(jsonObject.optInt("recordState"));
                                musicList.add(music);

                            }
                            if (musicList.size() == 0) {
                                getData.setVisibility(View.GONE);
                                noGetData.setVisibility(View.VISIBLE);
                            } else {
                                getData.setVisibility(View.VISIBLE);
                                noGetData.setVisibility(View.GONE);
                            }
                            dspAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "获取列表失败！", e);
                    }
                }

                @Override
                public void onFailure(String message) {
                    CommUtil.hideProcessing();
                    Toast.makeText(DSPManagerActivity.this, R.string.load_audio_list_failed, Toast.LENGTH_SHORT).show();
                    getData.setVisibility(View.GONE);
                    noGetData.setVisibility(View.VISIBLE);
                }
            };
            BctClient.getInstance().POST(DSPManagerActivity.this, CommonRestPath.audioSelectedList(), json, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception e) {
            Log.e(Constants.TAG, "获取分页失败！", e);
        }
    }

    @OnItemClick(R.id.music_listview)
    private void listItemOnClick(AdapterView<?> parent, View view, int position, long id) {
        musicListView.smoothOpenMenu(position);
    }

    private void download2Watch(int id, int positionIdx) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("audioId", id);
            json.putOpt("termseq", positionIdx + 1);
            json.putOpt("imei", termImei);
            json.putOpt("redownload", 1); //这里是重新下载，要传个1

            Log.d(Constants.TAG, "下载音乐请求信息：id:" + id + " termseq:" + positionIdx + 1 + " imei:" + termImei + "redownload" + 1);
            BctClientCallback callback = new BctClientCallback() {
                @Override
                public void onStart() {
                    CommUtil.showProcessing(musicListView, true, true);
                }

                @Override
                public void onFinish() {
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    CommUtil.hideProcessing();
                    if (obj.getRetcode() != 1) {
                        Toast.makeText(DSPManagerActivity.this, R.string.load_audio_list_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(DSPManagerActivity.this, R.string.download_succ, Toast.LENGTH_SHORT).show();
                    loadPage();
                }

                @Override
                public void onFailure(String message) {
                    CommUtil.hideProcessing();
                    Toast.makeText(DSPManagerActivity.this, R.string.load_audio_list_failed, Toast.LENGTH_SHORT).show();
                }
            };
            BctClient.getInstance().POST(DSPManagerActivity.this, CommonRestPath.sendAudio(), json, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception e) {
            Log.e(Constants.TAG, "下载到手表失败！", e);
        }
    }

    @Subscriber(tag = Constants.EVENT_TAG_AUDIO_STATUS)
    private void updateAudioStatus(Msg msg) {
        if (msg == null || msg.getData() == null) {
            return;
        }
        String data = new String(msg.getData()).trim();
        String[] items = data.split("\\|");
        if (items.length >= 2) {
            int id = CommUtil.toInteger(items[0]);
            int status = CommUtil.toInteger(items[1]);
            for (Music music : musicList) {
                if (music.getId() == id) {
                    music.setStatus(status);
                    break;
                }
            }
            dspAdapter.notifyDataSetChanged();
        }
    }
}
