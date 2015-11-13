package com.bct.gpstracker.my.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.my.adapter.DSPSelectorAdapter;
import com.bct.gpstracker.pojo.Music;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.service.PlayerService;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.JsonHttpResponseHelper;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/**
 *
 */
public class DSPSelectorActivity extends BaseActivity {
    private ListView musicListView;

    DSPSelectorAdapter dspSelectorAdapter;
    private String termImei;
    private List<Music> musicList = new ArrayList<>();
    private int currPage = 1;
    private int lastPage = 1;
    private Context mContext;

    @ViewInject(R.id.prev_page)
    Button prevPageButton;

    @ViewInject(R.id.next_page)
    Button nextPageButton;

    @Override
    protected void onDestroy() {
        PlayerService player = PlayerService.getPlayer();
        if (player != null) {
            player.stop();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        PlayerService player = PlayerService.getPlayer();
        if (player != null) {
            player.stop();
        } else {
            Intent intent = new Intent(DSPSelectorActivity.this, PlayerService.class);
            startService(intent);
        }
        dspSelectorAdapter.refreshWifi();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsp_selector);
        ViewUtils.inject(this);
        mContext = DSPSelectorActivity.this;

        termImei = getIntent().getStringExtra("IMEI");
        int position = getIntent().getIntExtra("POSITION", 0);

        dspSelectorAdapter = new DSPSelectorAdapter(this, musicList, termImei, position);
        musicListView = (ListView) findViewById(R.id.music_selector_listview);
        musicListView.setAdapter(dspSelectorAdapter);

        //加载第一页
        loadPage(1);
    }

    public ListView getMusicListView() {
        return musicListView;
    }

    @OnClick(R.id.prev_page)
    private void prevOnClick(View v) {
        int to = --currPage;
        if (to < 1) {
            currPage = 1;
            Toast.makeText(DSPSelectorActivity.this, "已经到达第一页了！", Toast.LENGTH_SHORT).show();
        } else {
            loadPage(to);
        }
    }

    @OnClick(R.id.next_page)
    private void nextOnClick(View v) {
        if (lastPage < 1) {
            lastPage = 1;
        }
        int to = ++currPage;
        if (to > lastPage) {
            currPage = lastPage;
            Toast.makeText(DSPSelectorActivity.this, "已经到达最后一页了！", Toast.LENGTH_SHORT).show();
        } else {
            loadPage(to);
        }
    }

    private void loadPage(final int to) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("page", to);
            json.putOpt("imei", termImei);
            BctClientCallback callback = new BctClientCallback() {
                @Override
                public void onStart() {
                    CommUtil.showProcessing(musicListView, true, false);
                }

                @Override
                public void onFinish() {
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    CommUtil.hideProcessing();
                    if (obj.getRetcode() != 1) {
                        Toast.makeText(DSPSelectorActivity.this, R.string.load_audio_list_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONObject body = obj.getBody();
                        lastPage = body.optInt("pageCount");
                        JSONArray array = body.getJSONArray("data");
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
                                music.setSerial((to - 1) * array.length() + i + 1);
                                music.setStatus(jsonObject.optInt("recordState"));
                                musicList.add(music);

                            }
                            dspSelectorAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "获取列表失败！", e);
                    }
                }

                @Override
                public void onFailure(String message) {
                    CommUtil.hideProcessing();
                    Toast.makeText(DSPSelectorActivity.this, R.string.load_audio_list_failed, Toast.LENGTH_SHORT).show();
                }
            };
            BctClient.getInstance().POST(DSPSelectorActivity.this, CommonRestPath.audioList(), json, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception e) {
            Log.e(Constants.TAG, "获取分页失败！", e);
        }
    }


}
