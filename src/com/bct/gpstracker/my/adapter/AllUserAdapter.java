package com.bct.gpstracker.my.adapter;

import java.util.List;

import android.content.Context;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseListAdapter;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.Keeper;
import com.bct.gpstracker.pojo.ManaRelation;
import com.bct.gpstracker.pojo.TermFriend;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Administrator on 15-9-8.
 * 联系人管理界面数据适配器
 */
public class AllUserAdapter extends BaseListAdapter <ManaRelation>{

    public AllUserAdapter(Context context,List list){
        this.context =context;
        this.list =list;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final UserHolder holder ;
        if(convertView==null){
            holder =new UserHolder();
            convertView =View.inflate(context, R.layout.layout_user_item,null);
            holder.uHead = (CircleImageView) convertView.findViewById(R.id.cim_mana_head);
            holder.uName = (TextView) convertView.findViewById(R.id.tv_mana_name);
            holder.uAuth = (TextView) convertView.findViewById(R.id.tv_mana_classify);
            holder.monitorInfo = (TextView) convertView.findViewById(R.id.monitor_info);
            holder.more = (ImageView) convertView.findViewById(R.id.im_mana_more);
            convertView.setTag(holder);
        }else{
            holder = (UserHolder) convertView.getTag();
        }

        ManaRelation rel =list.get(position);
        holder.uAuth.setText(rel.getAuthDiscrible());
        holder.uHead.setImageResource(R.drawable.user_no_photo);
        holder.monitorInfo.setVisibility(View.GONE);
        if(rel instanceof Keeper){
            Keeper temp =(Keeper)rel;
            holder.uName.setText(temp.getNickName());
            if (CommUtil.isNotBlank(temp.getPortrait())) {
                ImageLoader.getInstance().displayImage(temp.getPortrait(),holder.uHead);
            }
        }else if(rel instanceof TermFriend){
            TermFriend temp =(TermFriend)rel;
            holder.uName.setText(temp.getName());
//            holder.uHead.setImageResource(R.drawable.user_no_photo);
        }else{
            Device dv =(Device)rel;
            holder.uName.setText(dv.getName());
            if(CommUtil.isNotBlank(dv.getPortrait())){
                if(!dv.getPortrait().toUpperCase().startsWith("HTTP://")){
                    holder.uHead.setImageBitmap(Utils.bytes2Bimap(Base64.decode(dv.getPortrait(),Base64.DEFAULT)));
                }else {
                    ImageLoader.getInstance().displayImage(dv.getPortrait(),holder.uHead);
                }
            }else{
                holder.uHead.setImageResource(R.drawable.user_no_photo);
            }
            if (!dv.isBinded()) {
                holder.monitorInfo.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

    class UserHolder{
        public CircleImageView uHead;//头像
        public TextView uName;//姓名
        public TextView uAuth;//关系
        public TextView monitorInfo;//备注
        public ImageView more;//更多功能
    }
}
