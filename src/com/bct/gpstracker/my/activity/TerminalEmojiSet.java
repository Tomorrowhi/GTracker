package com.bct.gpstracker.my.activity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.my.adapter.EmojiAdapter;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.vo.EmojiList;
import com.google.gson.Gson;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Created by Admin on 2015/9/17 0017.
 * 选择表情
 */
public class TerminalEmojiSet extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.backBtn)
    private ImageButton backBtn;
    @ViewInject(R.id.setup_terminal_emoji_gv)
    private GridView setupTerminalEmojiGV;
    @ViewInject(R.id.setup_terminal_emoji_bt)
    private Button setupTerminalEmojiBT;
    @ViewInject(R.id.null_emoji_rl)
    private RelativeLayout nullEmojiRl;
    @ViewInject(R.id.exist_emoji_ll)
    private LinearLayout existEmojiLl;


    private BitmapUtils bitmapUtils;
    private Context mContext = TerminalEmojiSet.this;
    private List<EmojiList> emojiLists = new ArrayList<>();
    private EmojiAdapter emojiAdapter;
    private int pageCount;  //总页数
    private int pageNow = 1;  //当前页

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_terminal_emoji);
        ViewUtils.inject(this);

        initView();
        initData();
        initEvent();
    }

    private void initData() {
        //获得表情列表
        getEmojiList(pageNow);
        emojiAdapter = new EmojiAdapter(mContext, emojiLists, bitmapUtils);
        setupTerminalEmojiGV.setAdapter(emojiAdapter);
    }

    private void initView() {
        /*创建Xutils对象，同时设置缓存路径*/
        bitmapUtils = new BitmapUtils(mContext, mContext.getCacheDir() + "");
        //滑动不加载
        setupTerminalEmojiGV.setOnScrollListener(new PauseOnScrollListener(bitmapUtils, false, true));
    }

    private void initEvent() {
        backBtn.setOnClickListener(this);
        setupTerminalEmojiBT.setOnClickListener(this);

        setupTerminalEmojiGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                emojiLists.get(position).setSelectItem(!emojiLists.get(position).isSelectItem());
                emojiAdapter.notifyDataSetChanged();
            }
        });

        setupTerminalEmojiGV.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        //滑动到了最后一条数据
                        //加载更多
                        if (pageNow < pageCount) {
                            getEmojiList(++pageNow);
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                TerminalEmojiSet.this.finish();
                break;
            case R.id.setup_terminal_emoji_bt:
                //设置表情
                setTerminalEmoji();
                break;

        }
    }

    private void setTerminalEmoji() {
        JSONArray jsonArray = new JSONArray();
        for (EmojiList emojiList : emojiLists) {
            if (emojiList.isSelectItem()) {
                jsonArray.put(emojiList.getEmoteId());
            }
        }
        if (jsonArray.length() == 0) {
            Toast.makeText(mContext, R.string.please_select_emoji, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject json = new JSONObject();
            json.put("imei", ChatActivity.mEntityImei);
            json.put("ids", jsonArray);
            //使用异步请求链接对象
            BctClient.getInstance().POST(mContext, CommonRestPath.SetTerminalEmoji(), json, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ResponseData obj = new ResponseData(response);
                    if (obj.getRetcode() == 1) {
                        Toast.makeText(mContext, R.string.setup_emoji_success, Toast.LENGTH_SHORT).show();
                        TerminalEmojiSet.this.finish();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Toast.makeText(mContext, R.string.setup_emoji_fail, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public void getEmojiList(int page) {

        try {
            JSONObject json = new JSONObject();
            json.put("imei", ChatActivity.mEntityImei);
            json.put("page", page);
            //使用异步请求链接对象
            BctClient.getInstance().POST(mContext, CommonRestPath.GetEmojiList(), json, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ResponseData obj = new ResponseData(response);
                    if (obj.getRetcode() == 1) {
                        try {
                            JSONObject body = obj.getBody();
                            JSONArray jsonArray = body.getJSONArray("data");
                            pageCount = body.getInt("pageCount");
                            pageNow = body.getInt("pageNo");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                EmojiList emojiList = gsonParseData(jsonArray.getString(i));
                                int downloaded = emojiList.getDownloaded();
                                if (downloaded == -1 || downloaded == 2) {
                                    //如果图片是未下载或者下载失败，那么进行显示
                                    emojiLists.add(emojiList);
                                }
                            }
                            if (emojiLists.size() == 0) {
                                Toast.makeText(mContext, R.string.get_emoji_null, Toast.LENGTH_SHORT).show();
                                existEmojiLl.setVisibility(View.GONE);
                                nullEmojiRl.setVisibility(View.VISIBLE);
                            }else {
                                nullEmojiRl.setVisibility(View.GONE);
                                existEmojiLl.setVisibility(View.VISIBLE);
                            }
                            emojiAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**
     * 解析json数据
     *
     * @param JsonString
     * @return 封装的数据对象
     */
    public EmojiList gsonParseData(String JsonString) {
        Gson gson = new Gson();
        EmojiList emojiList = gson.fromJson(JsonString, EmojiList.class);
        return emojiList;
    }
}
