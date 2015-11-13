package com.bct.gpstracker.babygroup;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.JsonHttpResponseHelper;
import com.bct.gpstracker.view.RefreshListView;
import com.bct.gpstracker.vo.BabyFriend;
import com.google.gson.Gson;


/**
 * 宝宝圈界面
 */
public class BabyGroupFragment extends Fragment {

    private final String key = "BabyGroupMessage";
    private Long pageNumber = 0L; //请求的页码信息
    private List<BabyFriend> entityList = new ArrayList<>();
    public FriendAdapter mFriendAdapter;
    /*获得当前登录的用户名*/
    public boolean pullState = false;  //刷新状态
    private boolean publishState = false;   //是否点击发表消息的按钮
    private View view;
    private RefreshListView listView;
    private RelativeLayout loading;
    private Button publishBt;
    private LinearLayout publicView;
    private EditText publishContent;
    private Button publishContentBt;
    private ImageView imgaeViewBt;
    private GridView mGridView;


    public static BabyGroupFragment newInstance() {
        BabyGroupFragment newFragment = new BabyGroupFragment();
        return newFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_baby_group, container, false);
        listView = (RefreshListView) view.findViewById(R.id.baby_listview);
        loading = (RelativeLayout) view.findViewById(R.id.baby_loading);
        publishBt = (Button) view.findViewById(R.id.baby_group_publish_bt);
        mFriendAdapter = new FriendAdapter(getActivity(), entityList);
        mFriendAdapter.setListView(listView);
        listView.setAdapter(mFriendAdapter);

        getData();
        //initView();
        initEvent();
        return view;
    }

    /**
     * 初始化事件
     */
    private void initEvent() {

        refreshPullDownUse();
        /*发表按钮的点击事件*/
        publishBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BabyPublishContentActivity.class);
                startActivityForResult(intent, Constants.PUBLISH_COMMENT);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(key, entityList.get(position-1));
                Intent intent = new Intent(getActivity(), CommentFileActivity.class);
                intent.putExtras(bundle);
                //传递当前item的编号
                intent.putExtra("position",position-1);
                startActivityForResult(intent, Constants.COMMENT_FILE);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
                    //设置请求编码
                    pageNumber = 0L;
                    //设置刷新状态
                    pullState = true;
                    getData();



    }

    /**
     * 滑动刷新功能
     */
    private void refreshPullDownUse() {
        //1. 设置可以下拉刷新
        listView.setIsRefreshHead(true);
        //  设置可以加载更多数据
        listView.setIsRefreshFoot(true);
        //2. 设置下拉刷新数据的监听器:OnRefreshDataListener
        listView.setOnRefreshDataListener(new RefreshListView.OnRefreshDataListener() {

            @Override
            public void refresdData() {
                //添加刷新数据的代码
                listView.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                //设置请求编码
                pageNumber = 0L;
                //设置刷新状态
                pullState = true;
                getData();
                Toast.makeText(getActivity(), "刷新成功", Toast.LENGTH_SHORT).show();
                mFriendAdapter.notifyDataSetChanged();
                listView.refreshStateFinish();//调用listvew的这个方法处理刷新结果状态改变，显示视图


            }

            @Override
            public void loadingMore() {
                //只需覆盖此方法，添加刷新数据的代码

                /*判断是否存在数据*/
                if (entityList.size() > 0) {
                    //设置请求码
                    pageNumber = Long.parseLong(entityList.get(entityList.size() - 1).getPublishId());
                }
                if (pageNumber == 1) {
                    Toast.makeText(getActivity(), "没有更多数据", Toast.LENGTH_SHORT).show();
                    listView.refreshStateFinish();
                    return;
                }
                getData();
                mFriendAdapter.notifyDataSetChanged();
                listView.refreshStateFinish();//调用listvew的这个方法处理刷新结果状态改变，显示视图

            }
        });
    }

    /**
     * 初始化数据
     */
    public void getData() {

        if (pullState) {
            entityList.clear();
            pullState = !pullState;
        }
        //向服务器端发送请求，获得数据
        getRequest(pageNumber, new BctClientCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) {
                //将回调的数据转换为Json数组
                JSONArray bodyArray = obj.getBodyArray();
                try {
                    for (int i = 0; i < bodyArray.length(); i++) {
                         Log.e("TGA", "jsonObject:" + bodyArray.toString());
                        BabyFriend bfBean = gsonParseData(bodyArray.getString(i));
                        for (int j = 0; j < entityList.size(); j++) {
                            if (entityList.get(j).getPublishId().equals(bfBean.getPublishId())) {
                                entityList.remove(j);
                            }
                        }
                        entityList.add(bfBean);
                    }
                    mFriendAdapter.notifyDataSetChanged();
                    loading.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String message) {
                //连接失败重试方法
            }

        });
    }


    /**
     * 从服务器端请求数据
     *
     * @param pageNumber 请求的页码
     * @param callback   回调信息
     */
    private void getRequest(Long pageNumber, final BctClientCallback callback) {
        try {
            JSONObject data = new JSONObject();
            data.put("endId", pageNumber);
            //使用异步请求链接对象
            BctClient.getInstance().POST(getActivity(), CommonRestPath.baByGroupQuery(), data, new JsonHttpResponseHelper(callback).getHandler());
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化页面
     */
    private ImageView babyGroupView, friendGroupView, interestView;

    /**
     * 初始化界面
     */
    private void initView() {
        babyGroupView = (ImageView) view.findViewById(R.id.imageView1);
        friendGroupView = (ImageView) view.findViewById(R.id.imageView2);
        interestView = (ImageView) view.findViewById(R.id.imageView3);

        babyGroupView.setOnClickListener(clickListener);
        friendGroupView.setOnClickListener(clickListener);
        interestView.setOnClickListener(clickListener);
    }

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imageView1:
                    Toast.makeText(getActivity(), "功能开发中", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.imageView2:
                    Toast.makeText(getActivity(), "功能开发中", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.imageView3:
                    Toast.makeText(getActivity(), "功能开发中", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * 解析json数据
     *
     * @param JsonString
     * @return 封装的数据对象
     */
    public BabyFriend gsonParseData(String JsonString) {
        Gson gson = new Gson();
        BabyFriend baByBean = gson.fromJson(JsonString, BabyFriend.class);
        return baByBean;
    }

   }
