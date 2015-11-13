package com.bct.gpstracker.my.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bct.gpstracker.R;
import com.bct.gpstracker.vo.EmojiList;
import com.lidroid.xutils.BitmapUtils;

/**
 * Created by Admin on 2015/9/17 0017.
 */
public class EmojiAdapter extends BaseAdapter {

    private Context mContext;
    private List<EmojiList> mEmojiLists;
    private BitmapUtils mBitmapUtils;

    public EmojiAdapter() {
    }

    public EmojiAdapter(Context context, List<EmojiList> emojiLists, BitmapUtils bitmapUtils) {
        super();
        mContext = context;
        mEmojiLists = emojiLists;
        mBitmapUtils = bitmapUtils;
        initData();
    }

    private void initData() {
        //设置默认加载图片
        mBitmapUtils.configDefaultLoadFailedImage(R.drawable.chat_icon_msg_failed);
        mBitmapUtils.configDefaultLoadingImage(R.drawable.chat_icon);
    }

    private class ViewHolder {
        RelativeLayout itemRL;
        ImageView gifIV;
        CheckBox itemCB;
    }

    @Override
    public int getCount() {
        return mEmojiLists.size();
    }

    @Override
    public Object getItem(int position) {
        return mEmojiLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final EmojiList emoji = mEmojiLists.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.set_terminal_emoji_item, null);
            holder.itemRL = (RelativeLayout) convertView.findViewById(R.id.setup_emoji_item_rl);
            holder.gifIV = (ImageView) convertView.findViewById(R.id.setup_emoji_item_gifiv);
            holder.itemCB = (CheckBox) convertView.findViewById(R.id.setup_emoji_item_cb);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (emoji.isSelectItem()) {
            convertView.setBackgroundColor(0x802F9BFF);
            holder.itemCB.setChecked(true);
        } else {
            convertView.setBackgroundColor(0x002F9BFF);
            holder.itemCB.setChecked(false);
        }
        if (!TextUtils.isEmpty(emoji.getEmoteUrl())) {
            mBitmapUtils.display(holder.gifIV, emoji.getEmoteUrl());
        }
        holder.itemCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.itemCB.setChecked(emoji.isSelectItem());
            }
        });
        return convertView;
    }
}
