package com.bct.gpstracker.baby.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.adapter.EmoteAdapter;
import com.bct.gpstracker.util.Utils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by Admin on 2015/10/8 0008.
 * 聊天界面底部动画显示
 */
public class ChatTerMainEmojiFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String EMOJI_PAGE_NUM = "now_page";
    private static final String EMOJI_PAGE_TOTAL_NUM = "total_page";

    @ViewInject(R.id.emotionbar_gv_display)
    private GridView girdViewEmoji;


    private View view;
    private Context mContext;
    private ChatTerMainEmojiFragmentCallBack mChatTerMainEmojiFragmentCallBack;
    private String emojiFlag;
    private EmoteAdapter mLocalGifAdapter;
    private List<String> tempEmojiName = new ArrayList<>();
    private int mPageNum, mTotalNum;

    //创建Fragment对象
    public static ChatTerMainEmojiFragment newInstance(int pageNum, int totalNum) {
        ChatTerMainEmojiFragment newFragment = new ChatTerMainEmojiFragment();
        Bundle args = new Bundle();
        args.putInt(EMOJI_PAGE_NUM, pageNum);
        args.putInt(EMOJI_PAGE_TOTAL_NUM, totalNum);
        newFragment.setArguments(args);
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chat_emotionbar, container, false);
        ViewUtils.inject(this, view);
        mPageNum = getArguments().getInt(EMOJI_PAGE_NUM);
        mTotalNum = getArguments().getInt(EMOJI_PAGE_TOTAL_NUM);
        initData();
        initEvent();
        return view;
    }

    private void initData() {
        tempEmojiName.clear();
        int showRangeStart = 0, showRangeEnd = 0;
        switch (mPageNum) {
            case 1:
                showRangeStart = 0;
                showRangeEnd = 8;
                break;
            case 2:
                showRangeStart = 8;
                showRangeEnd = 16;
                break;
            case 3:
                showRangeStart = 16;
                showRangeEnd = 24;
                break;
        }
        //优先加载新表情
        if (ChatActivity.mEmoticonsNewGifCopy.size() != 0) {
            if (mPageNum < mTotalNum) {
                //不是最后一页
                for (int j = showRangeStart; j < showRangeEnd; j++) {
                    tempEmojiName.add(ChatActivity.mEmoticonsNewGifCopy.get(j));
                }
            } else if (mPageNum == mTotalNum) {
                //最后一页
                for (int j = showRangeStart; j < ChatActivity.mEmoticonsNewGifCopy.size(); j++) {
                    tempEmojiName.add(ChatActivity.mEmoticonsNewGifCopy.get(j));
                }
            }
            mLocalGifAdapter = new EmoteAdapter(mContext,
                    tempEmojiName, true);
        } else {
            if (mPageNum < mTotalNum) {
                //不是最后一页
                for (int j = showRangeStart; j < showRangeEnd; j++) {
                    tempEmojiName.add(ChatActivity.mEmoticons_ZgifCopy.get(j));
                }
            } else if (mPageNum == mTotalNum) {
                //最后一页
                for (int j = showRangeStart; j < ChatActivity.mEmoticons_ZgifCopy.size(); j++) {
                    tempEmojiName.add(ChatActivity.mEmoticons_ZgifCopy.get(j));
                }
            }
            mLocalGifAdapter = new EmoteAdapter(mContext,
                    tempEmojiName, false);
        }

        girdViewEmoji.setColumnWidth((Utils.dp2px(mContext, 80)));
        girdViewEmoji.setAdapter(mLocalGifAdapter);

    }

    private void initEvent() {
        girdViewEmoji.setOnItemClickListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof ChatTerMainEmojiFragmentCallBack)) {
            throw new IllegalStateException("ChatTerMainEmojiFragment所在的Activity必须实现ChatAddOneFragmentCallBack接口");
        }
        mChatTerMainEmojiFragmentCallBack = (ChatTerMainEmojiFragmentCallBack) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mChatTerMainEmojiFragmentCallBack = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        emojiFlag = tempEmojiName.get(position);
        Log.d("TAG", emojiFlag);
        mChatTerMainEmojiFragmentCallBack.emojiData(emojiFlag);
    }

    /**
     * Avtivity回调接口
     */
    public interface ChatTerMainEmojiFragmentCallBack {
        void emojiData(String emojiFlag);
    }
}
