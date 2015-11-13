package com.bct.gpstracker.found;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.JsonHttpResponseHelper;
import com.bct.gpstracker.view.RefreshListView;
import com.bct.gpstracker.vo.FoundBean;
import com.google.gson.Gson;

public class FoundFragment extends Fragment {

    private final String key="FoundBeanMessage";
    private List<FoundBean> entityFoundList = new ArrayList<>();
    private FoundAdapter mAdapter;
    private RefreshListView listView;
    private RelativeLayout loading;
    private View view;
    public static FoundFragment newInstance() {
        return new FoundFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_found, container, false);
        listView = (RefreshListView) view.findViewById(R.id.listView1);
        loading = (RelativeLayout) view.findViewById(R.id.found_loading);
        mAdapter = new FoundAdapter(getActivity(), entityFoundList);
        listView.setAdapter(mAdapter);
        getData();
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Bundle bundle = new Bundle();
               // Log.e("TGA",position+"");
                bundle.putParcelable(key,entityFoundList.get(position-1));
                Intent intent = new Intent(getActivity(), FoundDetailActivity.class);
                intent.putExtras(bundle);
                getActivity().startActivity(intent);
            }
        });
//		initView();
        initEvent();
        return view;
    }

    private void initEvent() {
        refreshPullDownUse();
    }


    /**
     * 初始化页面
     */
    private ImageView interestView;

    private void initView() {
        interestView = (ImageView) view.findViewById(R.id.imageView1);

        interestView.setOnClickListener(clickListener);
    }

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imageView1:
                    Toast.makeText(getActivity(), "功能开发中", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * 滑动刷新功能
     */
    private void refreshPullDownUse() {
        //1. 设置可以下拉刷新
        listView.setIsRefreshHead(true);
        //2. 设置下拉刷新数据的监听器:OnRefreshDataListener
        listView.setOnRefreshDataListener(new RefreshListView.OnRefreshDataListener() {

            @Override
            public void refresdData() {
                //添加刷新数据的代码
                //设置刷新状态
                loading.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                getData();
                Toast.makeText(getActivity(), "数据已刷新", Toast.LENGTH_SHORT).show();
                mAdapter.notifyDataSetChanged();
                listView.refreshStateFinish();//调用listvew的这个方法处理刷新结果状态改变，显示视图
            }

            @Override
            public void loadingMore() {
                //下拉加载更多，预留
            }
        });
    }


    private void getData() {
        entityFoundList.clear();
        //向服务器端发送请求，获得数据
        getRequest(new BctClientCallback() {
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
                       // Log.e("TGA", "jsonObject:" + bodyArray.toString());
                        FoundBean bfBean = gsonParseData(bodyArray.getString(i));
                        entityFoundList.add(bfBean);
                    }
                    listView.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
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
     * @param callback   回调信息
     */
    private void getRequest(final BctClientCallback callback) {
        try {
            JSONObject data = new JSONObject();
            //使用异步请求链接对象
            BctClient.getInstance().POST(getActivity(), CommonRestPath.FoundQuery(), data, new JsonHttpResponseHelper(callback).getHandler());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /**
     * 解析json数据
     *
     * @param JsonString
     * @return 封装的数据对象
     */
    public FoundBean gsonParseData(String JsonString) {
        Gson gson = new Gson();
        FoundBean baByBean = gson.fromJson(JsonString, FoundBean.class);
        return baByBean;
    }
}
