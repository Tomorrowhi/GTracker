package com.bct.gpstracker.my.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.my.adapter.FenceAdapter;
import com.bct.gpstracker.pojo.FenceEntity;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.util.JSONHelper;
import com.bct.gpstracker.vo.Session;

public class FenceListActivity extends BaseActivity {
    private ImageButton backButton, addButton;
    private ListView listView;

    private List<FenceEntity> entityList = new ArrayList<FenceEntity>();
    private FenceAdapter mAdapter;
    private Context mContext = FenceListActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        String flag = getIntent().getStringExtra("flag");
//        //在此是判断是否从FenceSetUp启动的Activity,如果是，则执行一次
//        if ("setup".equals(flag)) {
//            FenceListActivity.this.finish();
//        }
        setContentView(R.layout.activity_fence_list);
        TextView titleTV = (TextView) findViewById(R.id.titleNameTV);
        titleTV.setText(getString(R.string.fence_title) + "-" + Session.getInstance().getSetupDevice().getName());
        listView = (ListView) findViewById(R.id.listView1);
        backButton = (ImageButton) findViewById(R.id.backBtn);
        addButton = (ImageButton) findViewById(R.id.addBtn);

        backButton.setOnClickListener(clickListener);
        addButton.setOnClickListener(clickListener);

        mAdapter = new FenceAdapter(FenceListActivity.this, entityList);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long id) {

                final String[] codes = new String[]{getString(R.string.edit), getString(R.string.delete)};
                AlertDialog.Builder mDialog = new AlertDialog.Builder(mContext);
                mDialog.setTitle(R.string.fence_click_title);
                mDialog.setItems(codes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            //编辑
                            Intent intent = new Intent(FenceListActivity.this, FenceActivity.class);
                            //intent.putExtra("fence", entityList.get(position));
                            Session.getInstance().setSetupfence(entityList.get(position));
                            FenceListActivity.this.startActivity(intent);
                        } else {
                            //删除
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                            alertDialog.setTitle(R.string.notice).setIcon(R.drawable.collect_icon_delete).setMessage(R.string.notice_delete_area)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteFence(entityList.get(position).getId(), position);
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, null).show();
                        }
                    }
                });
                mDialog.show();
            }
        });
    }

    /**
     * 删除电子栅栏
     *
     * @param id
     */
    private void deleteFence(int id, final int position) {
        Session.getInstance().getSetupDevice().fenceDelete(this, id, new BctClientCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    Toast.makeText(FenceListActivity.this, R.string.fence_delete_success, Toast.LENGTH_SHORT).show();
                    entityList.remove(position);
                    mAdapter.notifyDataSetChanged();
                    enterFenceActivity();

                }else{
                    Toast.makeText(FenceListActivity.this, R.string.fence_delete_exception, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(FenceListActivity.this, R.string.fence_delete_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 当没有栅栏数据时，直接进入设置电子围栏界面
     */
    public void enterFenceActivity() {
        if (entityList.size() == 0) {
            //没有数据，直接进入增加电子围栏界面
            Toast.makeText(FenceListActivity.this, R.string.fence_null_data, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FenceListActivity.this, FenceActivity.class);
            Session.getInstance().setSetupfence(null);
            intent.putExtra("fencelist", true);
            FenceListActivity.this.startActivity(intent);
            FenceListActivity.this.finish();
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
                    FenceListActivity.this.finish();
                    break;
                case R.id.addBtn:
                    Intent intent = new Intent(FenceListActivity.this, FenceActivity.class);
                    Session.getInstance().setSetupfence(null);
                    FenceListActivity.this.startActivity(intent);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    /**
     * 获取所有的设备
     */
    private void getData() {
        Session.getInstance().getSetupDevice().getFenceList(this, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(R.string.get_data, FenceListActivity.this);
            }

            @Override
            public void onFinish() {
                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                /*清楚旧的数据*/
                entityList.clear();
                if (obj.getRetcode() == 1) {
                    for (int i = 0; i < obj.getBodyArray().length(); i++) {
                        FenceEntity mEntity = new FenceEntity(JSONHelper.getJSONObject(obj.getBodyArray(), i));
                        entityList.add(mEntity);
                    }
                    enterFenceActivity();
                    mAdapter.notifyDataSetChanged();
                    //initView();
                    //Toast.makeText(FenceListActivity.this, entityList.size() + "个电子围栏", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FenceListActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(FenceListActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
