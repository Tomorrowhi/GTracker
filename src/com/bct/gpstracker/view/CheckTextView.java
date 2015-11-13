package com.bct.gpstracker.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author 作者 E-mail:黄飞  353240166@qq.com
 * @version 创建时间：2015年2月10日 下午2:05:10
 * 类说明:
 */
public class CheckTextView extends TextView {
	
	private boolean isChecked;
	
    public CheckTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckTextView(Context context) {
        super(context);
    }

    public CheckTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
    	if(isChecked==true){
    		this.setBackgroundColor(Color.parseColor("#0a9dcd"));
    		this.setTextColor(Color.WHITE);
    	}else {
    		this.setBackgroundColor(Color.parseColor("#f2f2f2"));
    		this.setTextColor(Color.BLACK);
		}
	}
    
    
    
}	
