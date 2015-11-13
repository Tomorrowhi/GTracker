package com.bct.gpstracker.babygroup;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.FileUtils;
import com.bct.gpstracker.util.MediaUtil;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by Admin on 2015/8/22 0022.
 */
public class BabyPublishContentActivity extends BaseActivity implements View.OnClickListener {

    private final static int OPEN_PHOTO = 699;
    private File mfile;
    private HttpUtils mHttp;
    private RequestParams mParams;
    private String randomId;
    private Context mContext = BabyPublishContentActivity.this;
    private PublishAddImageAdapter mAdapter;
    private boolean gridViewState = true;
    private int mCount = 1;
    private String content = "";
    private List<Bitmap> mlist = new ArrayList<>();
    private List<String> mPicPath = new ArrayList<>();
    @ViewInject(R.id.baby_group_publish_content_bt)
    private Button publishContentBt;
    @ViewInject(R.id.baby_group_publish_content_comment)
    private EditText publishContent;
    @ViewInject(R.id.baby_group_publish_iv)
    private ImageView publicAddImageView;
    @ViewInject(R.id.baby_group_publish_gridView)
    private GridView publicAddGridView;
    @ViewInject(R.id.backBtn)
    private ImageButton backBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        ViewUtils.inject(this);
        publishContentBt.setOnClickListener(this);
        publicAddImageView.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        initData();
        initEvent();
    }

    private void initEvent() {
        publicAddGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.removeList(position);
                mAdapter.notifyDataSetChanged();
                //选择图片
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, OPEN_PHOTO);
            }
        });
    }


    private void initData() {
        mAdapter = new PublishAddImageAdapter(BabyPublishContentActivity.this, mlist);
        publicAddGridView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.baby_group_publish_content_bt:
                //发表消息按钮
                //设置信息和图片的唯一标识
                randomId = CommUtil.genSpecificName(Session.getInstance().getImei(), 100) + System.currentTimeMillis();
                //获得用户输入的信息
                content = publishContent.getText().toString();
                if (mPicPath != null && mPicPath.size() > 0) {
                    mCount=1;
                    //用户选择了图片，开始上传图片
                    for (int i = 0; i < mPicPath.size(); i++) {
                        mfile = new File(mContext.getFilesDir() + "/" + mPicPath.get(i));
                        submitPic(mfile);
                    }
                } else {
                    //如果没有选择图片，那么直接发送消息
                    if (TextUtils.isEmpty(content)) {
                        Toast.makeText(mContext, "请编辑内容", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //发表内容
                    submitContent(content);
                }

                break;
            case R.id.baby_group_publish_iv:
                //图片按钮
                if (gridViewState) {
                    publicAddGridView.setVisibility(View.VISIBLE);
                } else {
                    publicAddGridView.setVisibility(View.GONE);
                }
                gridViewState = !gridViewState;
                break;
            case R.id.backBtn:
                //返回上一界面
                BabyPublishContentActivity.this.finish();
                break;

        }
    }


    private class PublishAddImageAdapter extends BaseAdapter {
        private Context context;
        private List<Bitmap> list;
        private ImageView image;

        public PublishAddImageAdapter(Context context, List<Bitmap> list) {
            this.context = context;
            this.list = list;
        }

        public void addList(Bitmap uri) {
            list.add(uri);
        }

        public void removeList(int position) {
            list.remove(position);
        }

        @Override
        public int getCount() {
            return list.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater view = LayoutInflater.from(context);
            convertView = view.inflate(R.layout.publish_gridview_item, null);
            image = (ImageView) convertView.findViewById(R.id.gridview_image_default);
            image.setPadding(5,5,5,5);
            if (position < list.size()) {
                image.setImageBitmap(list.get(position));
            } else if (position == list.size()) {
                image.setImageResource(R.drawable.add_imageview);
            }
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, OPEN_PHOTO);
                }
            });
            return convertView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && CommUtil.isNotBlank(data.getData())) {
            ContentResolver resolver = getContentResolver();
            byte[] bytes = null;
            try {
                InputStream in = resolver.openInputStream(data.getData());
                bytes = FileUtils.readInputStream(in);
            } catch (Exception e) {
                Log.e(Constants.TAG, "读取文件失败！", e);
            }
            if (bytes == null || bytes.length == 0) {
                return;
            }

            Bitmap bitmap = MediaUtil.scaleToSettingSize(bytes);
            if (bitmap == null) {
                Toast.makeText(mContext, "选择图片失败，请重试", Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bout);
            String folderName = CommUtil.genSpecificName(Session.getInstance().getImei(), 100);
            String path = "";
            try {
                path = FileUtils.saveFile(this, folderName, System.currentTimeMillis() + ".jpg", bout.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPicPath.add(path);
            mAdapter.addList(bitmap);
            mAdapter.notifyDataSetChanged();
        }
//        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int count = (int) msg.obj;
            if (count == mPicPath.size()) {
                //当图片发送完成后，开始发送消息
                submitContent(content);
            }
        }
    };


    /**
     * 发送消息数据
     *
     * @param content
     */
    public void submitContent(String content) {

       /*发送消息*/
        mParams = new RequestParams();
        mParams.addHeader("accesskey", Session.getInstance().getAccessKey());
        JSONObject data = new JSONObject();
        HttpEntity entity;
        try {
            data.put("cont", content);
            data.put("randomId", randomId);
            entity = new StringEntity(data.toString(), "utf-8");
            mParams.setBodyEntity(entity);
            mParams.setContentType("applicatin/json");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mHttp = new HttpUtils(60 * 1000);
        mHttp.send(HttpRequest.HttpMethod.POST, Constants.baseUrl + CommonRestPath.baByGroupAddPublish(), mParams, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                return;
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Toast.makeText(mContext, "发表成功", Toast.LENGTH_SHORT).show();
                BabyPublishContentActivity.this.finish();
                Log.e("MyLog", "发送成功");
            }
        });
    }

    /**
     * 发送图片数据
     */
    public void submitPic(File file) {
        mParams = new RequestParams();
        mParams.addHeader("accesskey", Session.getInstance().getAccessKey());
        mParams.addBodyParameter("randomId", randomId);
        mParams.addBodyParameter("file", file);
        mHttp = new HttpUtils(60 * 1000);
        mHttp.send(HttpRequest.HttpMethod.POST, Constants.baseUrl + CommonRestPath.baByGroupAddPic(), mParams, new RequestCallBack<String>() {
            @Override
            public void onStart() {
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                if (isUploading) {
//                          msgTextview.setText("upload: " + current + "/"+ total);
                } else {
//                          msgTextview.setText("reply: " + current + "/"+ total);
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //{"head":{"retcode":1,"msg":""}}
                Message message = Message.obtain();
                message.obj = mCount++;
                mHandler.sendMessage(message);
                Log.e("TGA", "图片上传成功");
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Toast.makeText(mContext, "图片发送失败，请重试", Toast.LENGTH_SHORT).show();
                Log.e("TGA", "图片上传失败");
            }
        });

    }

}
