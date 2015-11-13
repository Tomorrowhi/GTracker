package com.bct.gpstracker.my.activity;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.PaintDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.CommHandler;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.pojo.PushMsg;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.view.DropDownListView;
import com.bct.gpstracker.vo.MsgType;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

public class MsgCenterActivity extends BaseActivity {
	
	private ImageButton backButton;
    private LinkedList<PushMsg> listItems = null;
    private DropDownListView     listView  = null;
    private MsgListAdapter adapter;
    private int page=0;
    private int pageSize=5;
    private String last_modified;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_messages);
		
		backButton = (ImageButton) findViewById(R.id.backBtn);
		backButton.setOnClickListener(clickListener);

        listView = (DropDownListView)findViewById(R.id.msg_list_view);
        // set drop down listener
        listView.setOnDropDownListener(new DropDownListView.OnDropDownListener() {
            @Override
            public void onDropDown() {
                new GetDataTask(true).execute();
            }
        });

        // set on bottom listener
        listView.setOnBottomListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new GetDataTask(false).execute();
            }
        });

        ImageView msgMore=(ImageView)findViewById(R.id.msgMore);
        msgMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuWindow(v);
            }
        });

        try {
            List<PushMsg> list= AppContext.db.findAll(Selector.from(PushMsg.class).where("type", "<>", MsgType.WARN_MSG.getValue()).orderBy("create_time", true).limit(pageSize));
            listItems =new LinkedList<>(list);
        } catch (DbException e) {
            Log.e(Constants.TAG,"取数据出错！",e);
        }
        adapter = new MsgListAdapter(this,listItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!isSelecting){
                    return;
                }
                ViewHolder holder=(ViewHolder)view.getTag();
                holder.checkBox.toggle();
                adapter.selection.put(id, holder.checkBox.isChecked());
                Log.d(Constants.TAG,"当前选择："+adapter.selection);
            }
        });
	}

    private class MsgListAdapter extends BaseAdapter{
        private Context context;
        private List<PushMsg> msgList;
        public Map<Long,Boolean> selection=new ConcurrentHashMap<>();

        public MsgListAdapter(Context context, List<PushMsg> msgList) {
            this.context = context;
            this.msgList = msgList;
        }

        @Override
        public int getCount() {
            return msgList.size();
        }

        @Override
        public Object getItem(int position) {
            return msgList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return msgList.get(position).getId();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if(view==null){
                LayoutInflater inflater= LayoutInflater.from(context);
                view=inflater.inflate(R.layout.msg_item, null);
                holder=new ViewHolder();
                holder.checkBox=(CheckBox)view.findViewById(R.id.check_msg);
                holder.msg=(TextView)view.findViewById(R.id.comm_msg);
                holder.ctime=(TextView)view.findViewById(R.id.msg_ctime);
                view.setTag(holder);
            }else{
                holder=(ViewHolder)view.getTag();
            }

            holder.msg.setText(msgList.get(position).getMsg());

//            Long wtime=msgList.get(position).getUploadTime();
//            String wdt=wtime==null?"":CommUtil.getDateTime(new Date(wtime));

            Long time=msgList.get(position).getCreateTime();
            String dt=time==null?"":CommUtil.getDateTime(new Date(time));
            holder.ctime.setText(dt);

            if(isSelecting){
                holder.checkBox.setVisibility(View.VISIBLE);
            }else{
                holder.checkBox.setVisibility(View.GONE);
            }
            return view;
        }
    }

    static class ViewHolder{
        CheckBox checkBox;
        TextView msg;
        TextView ctime;
    }

    private class GetDataTask extends AsyncTask<Void, Void, List<PushMsg>> {

        private boolean isDropDown;

        public GetDataTask(boolean isDropDown){
            this.isDropDown = isDropDown;
        }

        @Override
        protected List<PushMsg> doInBackground(Void... params) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<PushMsg> result) {

            try {
                if (isDropDown) {
                    Long time=CommUtil.isEmpty(listItems)?0:listItems.get(0).getCreateTime();
                    List<PushMsg> list= AppContext.db.findAll(Selector.from(PushMsg.class).where(WhereBuilder.b("create_time", ">", time).and("type", "<>", MsgType.WARN_MSG.getValue())).orderBy
                            ("create_time", true).limit(pageSize));
                    if(list!=null){
                        for(int i=list.size()-1;i>=0;i--){
                            listItems.addFirst(list.get(i));
                        }
                    }
                    adapter.notifyDataSetChanged();
                    listView.onDropDownComplete(last_modified==null?"":(getString(R.string.last_modified) + last_modified));
                } else {
                    List<PushMsg> list= AppContext.db.findAll(Selector.from(PushMsg.class).where("type", "<>", MsgType.WARN_MSG.getValue()).orderBy("create_time", true).offset((++page) * pageSize)
                            .limit(pageSize));
                   if(list!=null&&list.size()>0){
                        for(int i=0;i<list.size();i++){
                            listItems.add(list.get(i));
                        }
                    }else{
                        listView.setHasMore(false);
                    }
                    adapter.notifyDataSetChanged();
                    // should call onBottomComplete function of DropDownListView at end of on bottom complete.
                    listView.onBottomComplete();
                }
            } catch (DbException e) {
                Log.e(Constants.TAG,"更新出错",e);
            }
            last_modified=CommUtil.getDateTime(new Date());
            super.onPostExecute(result);
        }
    }
	
	/**
	 * 点击事件
	 */
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.backBtn:
				MsgCenterActivity.this.finish();
				break;
			}
		}
	};

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean refresh=intent.getBooleanExtra(Constants.REFRESH_FLAG,false);
        if(refresh){
            listView.onDropDown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//        menu.add(0, 1, 1, R.string.delete_before_last_week);
//        menu.add(0, 2, 2, R.string.delete_before_last_month);
//        menu.add(0, 3, 3, R.string.delete_all_warn_msg);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        new AlertDialog.Builder(MsgCenterActivity.this)
                .setTitle(R.string.notice)
                .setMessage(String.format(getString(R.string.confirm_msg), item.getTitle()))
                .setPositiveButton(R.string.yes, menuDialogListener.with(item.getItemId()))
                .setNegativeButton(R.string.no, null)
                .show();
        return true;
    }

    MenuDialogListener menuDialogListener=new MenuDialogListener();
    private class MenuDialogListener implements DialogInterface.OnClickListener{
        private int menuId;

        public MenuDialogListener with(int menuId) {
            this.menuId = menuId;
            return this;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                switch (menuId){
                    case 1:
                        long beforeLastWeekTime=System.currentTimeMillis()-604800000L;
                        AppContext.db.delete(PushMsg.class, WhereBuilder.b("create_time","<",beforeLastWeekTime).and("type", "<>", MsgType.WARN_MSG.getValue()));
                        break;
                    case 2:
                        long beforeLastMonthTime=System.currentTimeMillis()-2592000000L;
                        AppContext.db.delete(PushMsg.class, WhereBuilder.b("create_time","<",beforeLastMonthTime).and("type", "<>", MsgType.WARN_MSG.getValue()));
                        break;
                    case 3:
                        AppContext.db.delete(PushMsg.class, WhereBuilder.b("type", "<>", MsgType.WARN_MSG.getValue()));
                        break;
                    case 4:
                        if(!adapter.selection.isEmpty()){
                            for(Map.Entry<Long,Boolean> entry:adapter.selection.entrySet()){
                                if(entry.getValue()){
                                    AppContext.db.deleteById(PushMsg.class, entry.getKey());
                                }
                            }
                            adapter.selection.clear();
                        }
                        break;
                    default:
                        break;
                }

                deletingFinish();

                List<PushMsg> list= AppContext.db.findAll(Selector.from(PushMsg.class).where("type", "<>", MsgType.WARN_MSG.getValue()).orderBy("create_time", true).limit(pageSize));
                listItems.clear();
                listItems.addAll(list);
                adapter.notifyDataSetChanged();
                CommUtil.sendMsg(CommHandler.TOAST_SHORT, getString(R.string.delete_success));

            } catch (DbException e) {
                Log.e(Constants.TAG,"删除信息失败！",e);
            }
            dialog.dismiss();
        }
    }

    /**
     * 右上角菜单选择框
     */
    private PopupWindow menuWindow;
    private View menuView;
    private LinearLayout delMsg;
    private boolean isSelecting=false;

    private void showMenuWindow(View parent) {
//        WindowManager windowManager = (WindowManager) AppContext.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (menuWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) AppContext.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            menuView = layoutInflater.inflate(R.layout.menu_msg_list, null);
            delMsg = (LinearLayout) menuView.findViewById(R.id.del_msg);
            // 创建一个PopuWidow对象
            DisplayMetrics dm =getResources().getDisplayMetrics();
            menuWindow = new PopupWindow(menuView, (dm.widthPixels) / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 使其聚集
        menuWindow.setFocusable(true);
        // 设置允许在外点击消失
        menuWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        menuWindow.setBackgroundDrawable(new PaintDrawable());
//        menuWindow.setBackgroundDrawable(new BitmapDrawable());
        menuWindow.showAsDropDown(parent, 0, 0);
        delMsg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelecting = true;
                if (CommUtil.isNotEmpty(listItems)) {
                    for (int i = 0; i < listView.getChildCount(); i++) {
                        View view = listView.getChildAt(i);
                        if (view == null) {
                            continue;
                        }
                        CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_msg);
                        if (checkBox == null) {
                            continue;
                        }
                        checkBox.setVisibility(View.VISIBLE);
                    }
                    showBottomMenuWindow(v);
                }
                if (menuWindow != null) {
                    menuWindow.dismiss();
                }
            }
        });
    }

    /**
     * 底部菜单选择框
     */
    private PopupWindow menuBottomWindow;
    private View menuBottomView;
    private LinearLayout warnMsgSelectAll,warnMsgSelectNone;
    private Button delButton,cancelButton;

    private void showBottomMenuWindow(View parent) {
//        WindowManager windowManager = (WindowManager) AppContext.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (menuBottomWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) AppContext.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            menuBottomView = layoutInflater.inflate(R.layout.menu_warn_bottom, null);
            warnMsgSelectAll = (LinearLayout) menuBottomView.findViewById(R.id.warn_msg_select_all);
            warnMsgSelectNone = (LinearLayout) menuBottomView.findViewById(R.id.warn_msg_select_none);
            delButton=(Button)menuBottomView.findViewById(R.id.warn_msg_ok);
            cancelButton=(Button)menuBottomView.findViewById(R.id.warn_msg_cancel);
            // 创建一个PopuWidow对象
            DisplayMetrics dm =getResources().getDisplayMetrics();
            menuBottomWindow = new PopupWindow(menuBottomView, Double.valueOf(dm.widthPixels * 0.8).intValue(), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 使其聚集
//        menuBottomWindow.setFocusable(true);
        // 设置允许在外点击消失
//        menuBottomWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
//        menuBottomWindow.setBackgroundDrawable(new PaintDrawable());
//        menuWindow.setBackgroundDrawable(new BitmapDrawable());
        menuBottomWindow.showAtLocation(listView, Gravity.BOTTOM, 0, 0);
        warnMsgSelectAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.selection.clear();
                if (CommUtil.isNotEmpty(listItems)) {
                    for (int i = 0; i < listView.getChildCount(); i++) {
                        View view = listView.getChildAt(i);
                        if (view == null) {
                            continue;
                        }
                        CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_msg);
                        if (checkBox == null) {
                            continue;
                        }
                        checkBox.setChecked(true);
                        adapter.selection.put(listView.getItemIdAtPosition(i), true);
                    }
                }
            }
        });
        warnMsgSelectNone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.selection.clear();
                if (CommUtil.isNotEmpty(listItems)) {
                    for(int i=0;i<listView.getChildCount();i++){
                        View view=listView.getChildAt(i);
                        if(view==null){
                            continue;
                        }
                        CheckBox checkBox=(CheckBox)view.findViewById(R.id.check_msg);
                        if(checkBox==null){
                            continue;
                        }
                        checkBox.setChecked(false);
                    }
                }
            }
        });
        delButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MsgCenterActivity.this)
                        .setTitle(R.string.notice)
                        .setMessage(String.format(getString(R.string.confirm_msg), getString(R.string.menu_delete_msg)))
                        .setPositiveButton(R.string.yes, menuDialogListener.with(4))
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        });
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deletingFinish();
            }
        });
    }

    private void deletingFinish(){
        if(menuBottomWindow!=null){
            menuBottomWindow.dismiss();
        }
        isSelecting=false;
        adapter.selection.clear();
        if (CommUtil.isNotEmpty(listItems)) {
            for (int i = 0; i < listView.getChildCount(); i++) {
                View view = listView.getChildAt(i);
                if (view == null) {
                    continue;
                }
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_msg);
                if (checkBox == null) {
                    continue;
                }
                checkBox.setVisibility(View.GONE);
                checkBox.setChecked(false);
            }
        }
    }
}
