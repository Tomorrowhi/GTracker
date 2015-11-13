package com.bct.gpstracker.my.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.pojo.Music;
import com.bct.gpstracker.util.CommUtil;

public class DSPAdapter extends BaseAdapter {
    private Context context;
    private List<Music> list;
    private String termImei;

    public DSPAdapter(Context context, List<Music> list, String termImei) {
        this.context = context;
        this.list = list;
        this.termImei = termImei;
    }

    static class ContViewHolder {
        TextView musicSerial, musicName, musicSinger, musicDownloadStatus;
        LinearLayout playing;
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
        final ContViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_dsp_manager_item, null);
            holder = new ContViewHolder();
            convertView.setTag(holder);
            holder.musicSerial = (TextView) convertView.findViewById(R.id.music_serial);
            holder.musicName = (TextView) convertView.findViewById(R.id.music_name);
            holder.musicSinger = (TextView) convertView.findViewById(R.id.music_singer);
            holder.musicDownloadStatus = (TextView) convertView.findViewById(R.id.music_downloading);
            holder.playing=(LinearLayout)convertView.findViewById(R.id.playing);
        } else {
            holder = (ContViewHolder) convertView.getTag();
        }
        final Music music = list.get(position);
        holder.musicSerial.setText(music.getSerial() < 10 ? "0" + music.getSerial() : CommUtil.toStr(music.getSerial()));
        holder.musicName.setText(music.getName());
        holder.musicSinger.setText(music.getSinger());
        holder.playing.setVisibility(music.isPlaying()?View.VISIBLE:View.GONE);
        if(music.getStatus()==0){
            holder.musicDownloadStatus.setText(R.string.music_downloading);
            holder.musicDownloadStatus.setTextColor(context.getResources().getColor(R.color.darkgreen));
            holder.musicDownloadStatus.setVisibility(View.VISIBLE);
        }else if(music.getStatus()==2){
            holder.musicDownloadStatus.setText(R.string.music_download_failed);
            holder.musicDownloadStatus.setTextColor(context.getResources().getColor(R.color.red));
            holder.musicDownloadStatus.setVisibility(View.VISIBLE);
        }else{
            holder.musicDownloadStatus.setVisibility(View.GONE);
        }
        return convertView;
    }

}
