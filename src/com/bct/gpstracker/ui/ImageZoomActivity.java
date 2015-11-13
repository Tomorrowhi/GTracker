/**
 * @Project: Lens
 * @Title: PicZoomActivity.java
 * @Package: com.hh.lens.ui
 * @Description: 
 * @Author: hh
 * @Date: 2013-2-6 上午1:11:51
 * @Copyright: 2013 HH Inc. All right reserved
 * @Version:
 */
package com.bct.gpstracker.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.listener.ImageTouchScaleListener;
import com.bct.gpstracker.util.CommUtil;

/**
 * @Classname: PicZoomActivity
 * @Description:
 * @Author: hh
 * @Date: 2013-2-6 上午1:11:51
 * @Version:
 */
public class ImageZoomActivity extends BaseActivity
{
	private ImageView imageView;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @Title: onCreate
	 * 
	 * @Description:
	 * 
	 * @param savedInstanceState
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zoom_imageview);
		init();
	}

	/**
	 * 
	 * @Methodname: init
	 * @Discription:
	 * @Author HH
	 * @Time 2013-2-6 上午1:20:58
	 */
	private void init()
	{
		String imgPath = getIntent().getStringExtra(Constants.PIC_PATH);
		if (CommUtil.isNotBlank(imgPath))
		{
			imageView = (ImageView) findViewById(R.id.imageZoom);
            BitmapFactory.Options options=new BitmapFactory.Options();
			Bitmap img = BitmapFactory.decodeFile(imgPath,options);
			if (img == null)
			{
				new AlertDialog.Builder(this).setTitle(R.string.msg_notify).setMessage(R.string.cannot_found_pic)
						.setPositiveButton(R.string.confirm, new OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								finish();
							}
						}).show();
			}
			else
			{
				imageView.setImageBitmap(img);
				DisplayMetrics dm = new DisplayMetrics();
		        getWindowManager().getDefaultDisplay().getMetrics(dm);// 获取屏幕分辨率
		        ImageTouchScaleListener listener=new ImageTouchScaleListener(imageView, dm, options.outWidth,options.outHeight);
		        imageView.setOnTouchListener(listener);
			}
		}
	}
}
