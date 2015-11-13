/**
 * @Project: Lens
 * @Title: ImageTouchScaleListener.java
 * @Package: com.hh.lens.listener
 * @Description: 
 * @Author: hh
 * @Date: 2013-2-6 下午3:48:47
 * @Copyright: 2013 HH Inc. All right reserved
 * @Version:
 */
package com.bct.gpstracker.listener;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

/**
 * @Classname: ImageTouchScaleListener
 * @Description:
 * @Author: hh
 * @Date: 2013-2-6 下午3:48:47
 * @Version:
 */
public class ImageTouchScaleListener implements OnTouchListener
{
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	DisplayMetrics dm;
	ImageView imgView;
	int imageWidth;
	int imageHeight;
	float currWidth;
	float currHeight;
	float savedWidth;
	float savedHeight;
	float minScaleR;// 最小缩放比例
	static final float MAX_SCALE = 4f;// 最大缩放比例
	static final int NONE = 0;// 初始状态
	static final int DRAG = 1;// 拖动
	static final int ZOOM = 2;// 缩放
	static final int NORMAL = 3;// 100%大小切换
	int mode = NONE;
	PointF prev = new PointF();
	PointF mid = new PointF();
	float dist = 1f;
	long lastTime = 0l;// 上一次点击时间

	/**
	 * ImageTouchScaleListener 构造函数
	 * 
	 * @Title:
	 * @Description:
	 * @param view
	 *            存放图片的ImageView对象
	 * @param dms
	 *            屏幕分辨率对象
	 * @param width
	 *            bitmap的宽度
	 * @param height
	 *            bitmap的高度
	 */
	public ImageTouchScaleListener(ImageView view, DisplayMetrics dms, int width, int height)
	{
		imgView = view;
		dm = dms;
		imageWidth = width;
		imageHeight = height;
		currWidth = width;
		currHeight = height;

		// 先初始化一次
		minZoom();
		center();
		imgView.setImageMatrix(matrix);
	}

	/**
	 * 触屏监听
	 * 
	 * @Title: onTouch
	 * @Description:
	 * @param v
	 * @param event
	 * @return
	 * 
	 * @see OnTouchListener#onTouch(View,
	 *      MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		switch (event.getAction() & MotionEvent.ACTION_MASK)
		{
		// 主点按下
			case MotionEvent.ACTION_DOWN:
				// 计算是不是双击图片
				long currTime = System.currentTimeMillis();
				if (lastTime == 0)
				{
					lastTime = currTime;
				}
				else if (currTime - lastTime < 300)
				{
					System.out.println("========" + currWidth + "  " + dm.widthPixels);
					if (Float.valueOf(currWidth).intValue() != dm.widthPixels)
					{
						// 图片为放大状态,则缩放到最合适
						float scale = dm.widthPixels / currWidth;
						matrix.postScale(scale, scale);
						currWidth = dm.widthPixels;
						currHeight = dm.heightPixels;
					}
					else
					{
						// 图片为最合适，则放大到100%
						float scale = imageWidth / (float) imgView.getWidth();
						currWidth = currWidth * scale;
						currHeight = currHeight * scale;
						System.out.println("currWidth:" + currWidth + " currHeight:" + currHeight);
						matrix.postScale(scale, scale, event.getX(), event.getY());
					}
					lastTime = 0;
					mode = NORMAL;
					center();
				}
				else
				{
					lastTime = 0;
				}

				savedMatrix.set(matrix);
				prev.set(event.getX(), event.getY());
				mode = DRAG;
				break;
			// 副点按下
			case MotionEvent.ACTION_POINTER_DOWN:
				dist = spacing(event);
				// 如果连续两点距离大于10，则判定为多点模式
				if (spacing(event) > 10f)
				{
					savedMatrix.set(matrix);
					savedWidth = currWidth;
					savedHeight = currHeight;
					midPoint(mid, event);
					mode = ZOOM;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG)
				{
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - prev.x, event.getY() - prev.y);
				}
				else if (mode == ZOOM)
				{
					float newDist = spacing(event);
					if (newDist > 10f)
					{
						matrix.set(savedMatrix);

						float tScale = newDist / dist;
						matrix.postScale(tScale, tScale, mid.x, mid.y);
						currWidth = savedWidth * tScale;
						currHeight = savedHeight * tScale;
						System.out.println("currWidth:" + currWidth + " currHeight:" + currHeight + " scale:" + tScale);
					}
				}
				break;
		}
		imgView.setImageMatrix(matrix);
		CheckView();
		return true;
	}

	/**
	 * 限制最大最小缩放比例，自动居中
	 */
	private void CheckView()
	{
		float p[] = new float[9];
		matrix.getValues(p);
		if (mode == ZOOM)
		{
			if (p[0] < minScaleR)
			{
				matrix.setScale(minScaleR, minScaleR);
				currWidth *= minScaleR;
				currHeight *= minScaleR;
			}
			if (p[0] > MAX_SCALE)
			{
				matrix.set(savedMatrix);
			}
		}
		center();
	}

	/**
	 * 最小缩放比例，最大为100%
	 */
	private void minZoom()
	{
		minScaleR = Math
				.min((float) dm.widthPixels / (float) imageWidth, (float) dm.heightPixels / (float) imageHeight);
		if (minScaleR < 1.0)
		{
			matrix.postScale(minScaleR, minScaleR);
			currWidth *= minScaleR;
			currHeight *= minScaleR;
		}
	}

	private void center()
	{
		center(true, true);
	}

	/**
	 * 横向、纵向居中
	 */
	protected void center(boolean horizontal, boolean vertical)
	{
		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0, 0, imageWidth, imageHeight);
		m.mapRect(rect);
		float height = rect.height();
		float width = rect.width();
		float deltaX = 0, deltaY = 0;
		if (vertical)
		{
			// 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下放留空则往下移
			int screenHeight = dm.heightPixels;
			if (height < screenHeight)
			{
				deltaY = (screenHeight - height) / 2 - rect.top;
			}
			else if (rect.top > 0)
			{
				deltaY = -rect.top;
			}
			else if (rect.bottom < screenHeight)
			{
				deltaY = imgView.getHeight() - rect.bottom;
			}
		}
		if (horizontal)
		{
			int screenWidth = dm.widthPixels;
			if (width < screenWidth)
			{
				deltaX = (screenWidth - width) / 2 - rect.left;
			}
			else if (rect.left > 0)
			{
				deltaX = -rect.left;
			}
			else if (rect.right < screenWidth)
			{
				deltaX = screenWidth - rect.right;
			}
		}
		matrix.postTranslate(deltaX, deltaY);
	}

	/**
	 * 两点的距离
	 */
	private float spacing(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return Double.valueOf(Math.sqrt(x * x + y * y)).floatValue();
	}

	/**
	 * 两点的中点
	 */
	private void midPoint(PointF point, MotionEvent event)
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
}
