package com.bct.gpstracker.found;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.vo.FoundBean;
import static java.lang.Float.valueOf;

public class FoundDetailActivity extends BaseActivity {

    private TextView foundName;
    private TextView foundDistance;
    private TextView foundArea;
    private TextView foundContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initDara();


    }

    /**
     * 初始化界面
     */
    private void initView() {
        setContentView(R.layout.activity_found_detail);
        ImageButton backButton = (ImageButton) findViewById(R.id.backBtn);
        foundName = (TextView) findViewById(R.id.nameTV);
        foundDistance = (TextView) findViewById(R.id.distanceTV);
        foundArea = (TextView) findViewById(R.id.found_detail_area);
        foundContent = (TextView) findViewById(R.id.found_detail_content);

        backButton.setOnClickListener(clickListener);
    }

    /**
     * 初始化数据
     */
    private void initDara() {
        /*获取传递的数据对象*/
        String key = "FoundBeanMessage";
        Parcelable parcelableExtra = this.getIntent().getParcelableExtra(key);
        //转换数据对象
        FoundBean foundUserMsg = (FoundBean) parcelableExtra;
        /*转换距离单位*/
        String distance = roundDistance(foundUserMsg.getDistance());
        /*计算所在城市信息*/
        // String city = reverseGeocoding(foundUserMsg.getLat(), foundUserMsg.getLng());
        foundArea.setText("深圳");
        /*设置界面数据*/
        foundName.setText(foundUserMsg.getUserName());
        foundDistance.setText(distance);

        foundContent.setText(foundUserMsg.getPublishContent());
    }

    /**
     * 四舍五入
     *
     * @param data
     * @return
     */
    private String roundDistance(String data) {
        String distance;
        //四舍五入，保留三位数
        DecimalFormat decimal = new DecimalFormat("#.#");
        String str = decimal.format(valueOf(data));
        Float aFloat = valueOf(str);

        if (aFloat < 1 && (aFloat * 1000) > 50) {
            //如果距离小于1公里大于50米
            distance=(int)(aFloat * 1000) + "米";
        } else if((aFloat*1000)<50) {
            distance="50米以内";
        }else{
            distance=aFloat + "公里";
        }
        return distance;
    }

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn:
                    FoundDetailActivity.this.finish();
                    break;
            }
        }
    };


}
