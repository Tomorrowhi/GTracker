package com.bct.gpstracker.baby.adapter;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bct.gpstracker.adapter.BaseArrayListAdapter;
import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.msg.MsgMainFragment;

public class EmoteAdapter extends BaseArrayListAdapter {

    public EmoteAdapter(Context context, List<String> datas) {
        super(context, datas);
    }

    public EmoteAdapter(Context context, List<String> datas, boolean oldOrNew) {
        super(context, datas, oldOrNew);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_emote, null);
            holder = new ViewHolder();
            holder.mIvImage = (ImageView) convertView
                    .findViewById(R.id.emote_item_iv_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String name = (String) getItem(position);
        if (mOldOrNewGif) {
            Uri uri = MsgMainFragment.mEmoticonsUri.get(name);
            holder.mIvImage.setImageURI(uri);
        } else {
            int id = MainActivity.mEmoticonsId.get(name);
            holder.mIvImage.setImageResource(id);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView mIvImage;
    }
}
