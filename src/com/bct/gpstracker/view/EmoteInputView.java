package com.bct.gpstracker.view;

import android.content.Context;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.adapter.EmoteAdapter;
import com.bct.gpstracker.msg.MsgMainFragment;
import com.bct.gpstracker.util.Utils;


public class EmoteInputView extends LinearLayout implements OnClickListener,
        OnCheckedChangeListener, OnItemClickListener {

    private GridView mGvDisplay;
    private RadioGroup mRgInner;
    private ImageView mIvDelete;

    private EmoteAdapter mLocalGifAdapter;
    private EmoteAdapter mNewGifAdapter;
    private EmoteAdapter mEmojiAdapter;

    private EmoticonsEditText mEEtView;

    private boolean mIsSelectedDefault;

    public EmoteInputView(Context context) {
        super(context);
        init();
    }

    public EmoteInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public EmoteInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        inflate(getContext(), R.layout.chat_emotionbar, this);
        mGvDisplay = (GridView) findViewById(R.id.emotionbar_gv_display);
        mRgInner = (RadioGroup) findViewById(R.id.emotionbar_rg_inner);
        mIvDelete = (ImageView) findViewById(R.id.emotionbar_iv_delete);

        mGvDisplay.setOnItemClickListener(this);
        mRgInner.setOnCheckedChangeListener(this);
        //优先加载新表情
        if(MsgMainFragment.mEmoticonsNewGif.size()!=0)
        {
            mLocalGifAdapter = new EmoteAdapter(getContext(),
                    MsgMainFragment.mEmoticonsNewGif,true);
        }else{
            mLocalGifAdapter = new EmoteAdapter(getContext(),
                    MainActivity.mEmoticons_Zgif,false);
        }
        mEmojiAdapter = new EmoteAdapter(getContext(),
                MainActivity.mEmoticons_Zemoji);
        mGvDisplay.setAdapter(mLocalGifAdapter);
        mGvDisplay.setColumnWidth((Utils.dp2px(getContext(), 80)));
        mIsSelectedDefault = true;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.emotionbar_rb_default:
                mGvDisplay.setColumnWidth((Utils.dp2px(getContext(), 80)));
                mGvDisplay.setAdapter(mLocalGifAdapter);
                mIsSelectedDefault = true;
                break;

            case R.id.emotionbar_rb_emoji:
                mGvDisplay.setAdapter(mEmojiAdapter);
                mGvDisplay.setColumnWidth((Utils.dp2px(getContext(), 40)));
                mIsSelectedDefault = false;
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emotionbar_iv_delete:
                if (mEEtView != null) {
                    int start = mEEtView.getSelectionStart();
                    String content = mEEtView.getText().toString().trim();
                    if (TextUtils.isEmpty(content)) {
                        return;
                    }
                    String startContent = content.substring(0, start);
                    String endContent = content.substring(start, content.length());
                    String lastContent = content.substring(start - 1, start);
                    int last = startContent.lastIndexOf("[zem");
                    int lastChar = startContent.substring(0,
                            startContent.length() - 1).lastIndexOf("]");

                    if ("]".equals(lastContent) && last > lastChar) {
                        if (last != -1) {
                            mEEtView.setText(startContent.substring(0, last)
                                    + endContent);
                            // 定位光标位置
                            CharSequence info = mEEtView.getText();
                            if (info instanceof Spannable) {
                                Spannable spanText = (Spannable) info;
                                Selection.setSelection(spanText, last);
                            }
                            return;
                        }
                    }
                    mEEtView.setText(startContent.substring(0, start - 1)
                            + endContent);
                    // 定位光标位置
                    CharSequence info = mEEtView.getText();
                    if (info instanceof Spannable) {
                        Spannable spanText = (Spannable) info;
                        Selection.setSelection(spanText, start - 1);
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        String text = null;
        if (mIsSelectedDefault) {
            text = MainActivity.mEmoticons_Zgif.get(arg2);
            mEEtView.setText(text);
        } else {
            text = MainActivity.mEmoticons_Zemoji.get(arg2);
            if (mEEtView != null && !TextUtils.isEmpty(text)) {
                int start = mEEtView.getSelectionStart();
                //如果start的值为-1,则说明EditText控件中没有光标
                if (start == -1) {
                    start = 0;
                }
                CharSequence content = mEEtView.getText().insert(start, text);
                mEEtView.setText(content);
                // 定位光标位置
                CharSequence info = mEEtView.getText();
                if (info instanceof Spannable) {
                    Spannable spanText = (Spannable) info;
                    Selection.setSelection(spanText, start + text.length());
                }

            }
        }

    }

    public void setEditText(EmoticonsEditText editText) {
        mEEtView = editText;
    }
}
