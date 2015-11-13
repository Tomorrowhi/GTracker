package com.bct.gpstracker.babygroup;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.view.MyGridView;
import com.bct.gpstracker.vo.*;
import com.google.gson.Gson;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Created by Admin on 2015/8/11 0011.
 */
public class CommentFileActivity extends BaseActivity {

    private Context context = CommentFileActivity.this;
    private BabyFriend mFriendData;
    private ListView mListView;
    private Button mSendbtn;
    private ImageButton backBtn;
    private ImageButton deleteCommentBtn;
    private EditText mEditCommentMessage;
    private View mHeaderView;
    private TextView mHeaderUserName;
    private TextView mHeaderTime;
    private TextView mHeaderContent;
    private TextView mHeaderMessageCount;
    private TextView mHeaderLoading;
    private ImageView mHeaderLoadingIV;
    private MyGridView mHeaderImageView;
    private RelativeLayout mLayoutLoading;
    private List<CommentBean> mComments = new ArrayList<>();
    private List<FirstLevelComment> entityFirstList;
    private List<String> mImagePath=new ArrayList<>();
    private FriendImageViewAdapter friendImageViewAdapter;
    private FirstLevelComment mFirstCommentData;
    private SecondLevelComment mSecondCommentData;
    private FriendCommentAdapter mAdapter;
    private BitmapUtils bitmapUtils;
    private boolean stateReply = false; //回复消息的状态
    private int mPosition;   //当前消息所在Item的编号
    private String mContId = "";   //消息ID
    private String mcmtId = "";   //子评论ID
    private String cmtId = "";   //父评论ID


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_file);
        initView();
        initData();
        init();
        initEvent();
    }

    private void initEvent() {
        /*退出按钮*/
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentFileActivity.this.finish();
            }
        });
        /*删除按钮*/
        deleteCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mAbuilder = new AlertDialog.Builder(context);
                mAbuilder.setMessage("确认删除？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContId = mFriendData.getPublishId();
                        deleteContent(mContId, "", Constants.NULL_VALUE);
                        //TODO 根据所获得的position,返回当前的编号给BabyGroupFragment,然后由其修改数据，单条刷新数据
                        //关闭当前Activity
                        CommentFileActivity.this.finish();
                    }
                }).setNegativeButton("取消", null).show();

            }
        });

        /*点击评论按钮*/
        mSendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditCommentMessage.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(context, "请输入评论", Toast.LENGTH_SHORT).show();
                    return;
                }
                //限制最大输入字符数为400
                mEditCommentMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(400)});
                if (mEditCommentMessage.getText().length() > 400) {
                    Toast.makeText(context, "评论内容过长，请修改后再发表", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!stateReply) {
                    mContId = mFriendData.getPublishId();
                    cmtId = "";
                    mcmtId = "";
                }
                stateReply = false;
                submitContentFirst(message);

            }
        });

        /*评论的点击事件*/
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (id != -1) {
                    final String[] codes = new String[]{"回复", "删除"};
                    AlertDialog.Builder mDialog = new AlertDialog.Builder(context);
                    mDialog.setItems(codes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                //回复
                                stateReply = true;
                                //打开输入法
                                InputMethodManager m = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                                //输入框获得焦点
                                mEditCommentMessage.setEnabled(true);
                                mContId = "";
                                //判断点击的是几级评论
                                if (mComments.get(position - 1).getId() == 1) {
                                    //回复一级评论
                                    cmtId = mComments.get(position - 1).getCommentId();
                                    mcmtId = "";
                                } else {
                                    //回复二级评论
                                    mcmtId = mComments.get(position - 1).getFatherCommentID();
                                    cmtId = mComments.get(position - 1).getCommentId();
                                }
                                mEditCommentMessage.setHint("回复" + mComments.get(position - 1).getUserName());
                                mEditCommentMessage.setHintTextColor(Color.GRAY);

                            } else {
                                //删除
                                mContId = "";
                                deleteContent(mContId, mComments.get(position - 1).getCommentId(), position - 1);
                            }
                        }
                    });
                    mDialog.show();
                }
            }
        });
    }

    private void init() {
        //初始化编辑框
        mEditCommentMessage.setText("");
        mEditCommentMessage.setHint("");

          /*设置评论数量*/
        mHeaderMessageCount.setText(mComments.size() + "");

        mAdapter = new FriendCommentAdapter(getApplication(),
                CommentFileActivity.this, mComments);
        mListView.setAdapter(mAdapter);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        /*将评论数据进行转化*/
        commentConvert();
    }

    /**
     * 提取评论数据
     */
    private void commentConvert() {
        mComments.clear();
        if (entityFirstList != null && entityFirstList.size() > 0) {
            for (FirstLevelComment anEntityFirstList : entityFirstList) {
                mFirstCommentData = anEntityFirstList;
                //获取一级评论内容
                CommentBean commentI = new CommentBean();
                commentI.setId(1);  //一级评论标记
                commentI.setFatherCommentID("");
                commentI.setCommentContent(mFirstCommentData.getCommentContent());
                commentI.setCommentId(mFirstCommentData.getCommentId());
                commentI.setCommTime(mFirstCommentData.getCommTime());
                commentI.setPortrait(mFirstCommentData.getPortrait());
                commentI.setReplyMsg(mFirstCommentData.getReplyMsg());
                commentI.setUserName(mFirstCommentData.getUserName());
                mComments.add(commentI);
                if (mFirstCommentData.getSecondLevelComment() != null && mFirstCommentData.getSecondLevelComment().size() > 0) {
                    for (int j = 0; j < mFirstCommentData.getSecondLevelComment().size(); j++) {
                        //获得二级评论内容
                        mSecondCommentData = mFirstCommentData.getSecondLevelComment().get(j);
                        CommentBean commentJ = new CommentBean();
                        commentJ.setId(2);  //二级评论标记
                        commentJ.setFatherCommentID(mFirstCommentData.getCommentId());
                        commentJ.setCommentContent(mSecondCommentData.getCommentContent());
                        commentJ.setCommentId(mSecondCommentData.getCommentId());
                        commentJ.setCommTime(mSecondCommentData.getCommTime());
                        commentJ.setPortrait(mSecondCommentData.getPortrait());
                        commentJ.setReplyMsg(mSecondCommentData.getReplyMsg());
                        commentJ.setUserName(mSecondCommentData.getUserName());
                        mComments.add(commentJ);
                    }
                }
            }
        }
    }


    /**
     * 初始化界面
     */
    private void initView() {
        mListView = (ListView) findViewById(R.id.baby_group_comment_lv_list);
        mSendbtn = (Button) findViewById(R.id.baby_group_comment_btn_send);
        backBtn = (ImageButton) findViewById(R.id.backBtn);
        deleteCommentBtn = (ImageButton) findViewById(R.id.comment_file_delete);
        mEditCommentMessage = (EditText) findViewById(R.id.baby_group_comment_eet_editer);
        mHeaderView = LayoutInflater.from(context).inflate(
                R.layout.header_feed, null);
        mHeaderUserName = (TextView) mHeaderView.findViewById(R.id.baby_group_header_userName);
        mHeaderTime = (TextView) mHeaderView.findViewById(R.id.baby_group_header_tv_time);
        mHeaderContent = (TextView) mHeaderView.findViewById(R.id.baby_group_header_etv_content);
        mHeaderImageView = (MyGridView) mHeaderView.findViewById(R.id.baby_group_header_gridview_content);
        mHeaderMessageCount = (TextView) mHeaderView.findViewById(R.id.baby_group_header_htv_commentcount);
        mLayoutLoading = (RelativeLayout) mHeaderView.findViewById(R.id.baby_group_header_feed_layout_loading);
        mHeaderLoading = (TextView) mHeaderView.findViewById(R.id.baby_group_header_feed_tv_loading);
        mHeaderLoadingIV = (ImageView) mHeaderView.findViewById(R.id.baby_group_header_feed_iv_loading);


        String key = "BabyGroupMessage";
        Parcelable parcelableExtra = this.getIntent().getParcelableExtra(key);
        mPosition = this.getIntent().getIntExtra("position", Constants.NULL_VALUE);
        mFriendData = (BabyFriend) parcelableExtra;
        entityFirstList = mFriendData.getFirstLevelComment();
         /*判断是否为当前用户评论*/
        String userName = Session.getInstance().getUser().getPhone();
        if (userName.equals(mFriendData.getUserName())) {
            //显示删除按钮
            deleteCommentBtn.setVisibility(View.VISIBLE);
        } else {
            deleteCommentBtn.setVisibility(View.GONE);
        }
        /*设置头数据*/
        mHeaderUserName.setText(mFriendData.getUserName());
        mHeaderTime.setText(Constants.COMM_DATE_FMT.format(Long.parseLong(mFriendData.getPublishTime())));
        if("".equals(mFriendData.getPublishContent()))
        {
            mHeaderContent.setVisibility(View.GONE);
        }
        mHeaderContent.setText(mFriendData.getPublishContent());
        if (mFriendData.getPublishPath() != null && !("".equals(mFriendData.getPublishPath()))) {
            mHeaderImageView.setVisibility(View.VISIBLE);
            String[] picPath = mFriendData.getPublishPath().split(",");
            if (picPath != null) {
                for (int i = 0; i < picPath.length; i++) {
                    mImagePath.add(picPath[i]);
                }
            }
            /*创建Xutils对象，同时设置缓存路径*/
            bitmapUtils = new BitmapUtils(context, context.getCacheDir() + "/pic/");
            mHeaderImageView.setOnScrollListener(new PauseOnScrollListener(bitmapUtils,false,true));
            friendImageViewAdapter = new FriendImageViewAdapter(context, mImagePath,bitmapUtils);
            mHeaderImageView.setAdapter(friendImageViewAdapter);
            friendImageViewAdapter.notifyDataSetChanged();
        }else{
            mHeaderImageView.setVisibility(View.GONE);
        }
        mListView.addHeaderView(mHeaderView);
    }


    /**
     * 发送评论数据
     *
     * @param
     */
    private void submitContentFirst(String content) {
        try {
            JSONObject data = new JSONObject();

            data.put("contId", mContId);
            data.put("cont", content);
            data.put("cmtId", cmtId);
            data.put("mcmtId", mcmtId);
            BctClient.getInstance().POST(context, CommonRestPath.baByGroupAdd(), data, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Toast.makeText(context, "评论成功", Toast.LENGTH_SHORT).show();
                    ResponseData responseArray = new ResponseData(response);
                    try {
                        String bodyArray = responseArray.getBodyArray().get(0).toString();

                        FirstLevelComment pullItemData = (FirstLevelComment) gsonParseData(bodyArray, FirstLevelComment.class);
                        //添加评论
                        //判断返回的信息是几级评论
                        CommentBean secondLevelComment = new CommentBean();
                        if (pullItemData.getSecondLevelComment() == null) {
                            //二级评论的返回信息
                            secondLevelComment.setId(2);
                        } else {
                            //一级评论的返回信息
                            secondLevelComment.setId(1);
                        }
                        secondLevelComment.setCommentContent(pullItemData.getCommentContent());
                        secondLevelComment.setCommTime(pullItemData.getCommTime());
                        secondLevelComment.setCommentId(pullItemData.getCommentId());
                        secondLevelComment.setPortrait(pullItemData.getPortrait());
                        secondLevelComment.setReplyMsg(pullItemData.getReplyMsg());
                        secondLevelComment.setUserName(pullItemData.getUserName());
                        mComments.add(secondLevelComment);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //关闭输入法
                    InputMethodManager m = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    //刷新评论数据
                    init();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(context, "评论失败，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }


    /**
     * '
     * 删除指定的评论
     */
    private void deleteContent(String contId, String commentId, final int position) {

        try {
            JSONObject data = new JSONObject();
            data.put("contId", contId);
            data.put("cmtId", commentId);
            BctClient.getInstance().POST(context, CommonRestPath.baByGroupDelete(), data, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                    //移除本地数据，并进行刷新
                    if (position < mComments.size()) {
                        mComments.remove(position);
                    }
                      /*设置评论数量*/
                    mHeaderMessageCount.setText(mComments.size() + "");
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(context, "删除失败，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /**
     * 解析json数据
     *
     * @param JsonString
     * @return 封装的数据对象
     */
    public Object gsonParseData(String JsonString, Class<?> obj) {
        Gson gson = new Gson();
        Object baByBean = gson.fromJson(JsonString, obj);
        return baByBean;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
