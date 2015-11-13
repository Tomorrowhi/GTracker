package com.bct.gpstracker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import com.bct.gpstracker.R;

public class ClearEditText extends EditText implements OnFocusChangeListener, TextWatcher {
	/**
	 * 删除按钮的引用
	 */
    private Drawable mCancelDrawable;
    Paint paint;
    Drawable mDrawable;
    float searchSize=42;
    boolean focus=false;

    public ClearEditText(Context context) {
    	this(context, null); 
    } 
 
    public ClearEditText(Context context, AttributeSet attrs) { 
    	//这里构造方法也很重要，不加这个很多属性不能再XML里面定义
    	this(context, attrs, android.R.attr.editTextStyle);
    }
    
    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs);
    }
    
    
    private void init(Context context, AttributeSet attrs) {
    	//获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
    	mCancelDrawable = getCompoundDrawables()[2];
        if (mCancelDrawable == null) {
        	mCancelDrawable = getResources().getDrawable(R.drawable.cancel);
        } 
        mCancelDrawable.setBounds(0, 0, mCancelDrawable.getIntrinsicWidth(), mCancelDrawable.getIntrinsicHeight());
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.search_edit_color));
        paint.setTextSize(R.dimen.text_zise_normal);

        setClearIconVisible(false);
        setOnFocusChangeListener(this); 
        addTextChangedListener(this);
    }
 
 
    /**
     * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件
     * 当我们按下的位置 在  EditText的宽度 - 图标到控件右边的间距 - 图标的宽度  和
     * EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向没有考虑
     */
    @Override 
    public boolean onTouchEvent(MotionEvent event) { 
        if (getCompoundDrawables()[2] != null) { 
            if (event.getAction() == MotionEvent.ACTION_UP) {
                focus = event.getX() > (getWidth() - getPaddingRight() - mCancelDrawable.getIntrinsicWidth())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                if (focus) {
                    this.setText(""); 
                } 
            } 
        } 
 
        return super.onTouchEvent(event); 
    } 
 
    /**
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     */
    @Override 
    public void onFocusChange(View v, boolean hasFocus) { 
        if (hasFocus) { 
            setClearIconVisible(getText().length() > 0); 
        } else { 
            setClearIconVisible(false); 
        } 
    } 
 
 
    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     * @param visible
     */
    protected void setClearIconVisible(boolean visible) { 
        Drawable right = visible ? mCancelDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    } 
     
    
    /**
     * 当输入框里面内容发生变化的时候回调的方法
     */
    @Override 
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        setClearIconVisible(s.length() > 0); 
    } 
 
    @Override 
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         
    } 
 
    @Override 
    public void afterTextChanged(Editable s) { 
         
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        drawHint(canvas);
//    }

    private void drawHint(Canvas canvas) {
        if (this.getText().toString().length() == 0) {
            float textWidth = paint.measureText(getContext().getString(R.string.search));
            float textHeight = getFontLeading(paint);

            float dx = (getWidth() - searchSize - textWidth - 8) / 2;
            float dy = (getHeight() - searchSize) / 2;

            canvas.save();
            canvas.translate(getScrollX() + dx, getScrollY() + dy);
            if (mDrawable != null) {
                mDrawable.draw(canvas);
            }
            canvas.drawText(getContext().getString(R.string.search), getScrollX() + searchSize + 8, getScrollY() + (getHeight() - (getHeight() - textHeight) / 2) - paint.getFontMetrics().bottom - dy, paint);
            canvas.restore();
        }
    }

//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        if (mDrawable == null) {
//            try {
//                mDrawable = getContext().getResources().getDrawable(R.drawable.btn_search);
//                mDrawable.setBounds(0, 0, (int) searchSize, (int) searchSize);
//            } catch (Exception e) {
//
//            }
//        }
//    }

//    @Override
//    protected void onDetachedFromWindow() {
//        if (mDrawable != null) {
//            mDrawable.setCallback(null);
//            mDrawable = null;
//        }
//        super.onDetachedFromWindow();
//    }

    public float getFontLeading(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.bottom - fm.top;
    }

    /**
     * 设置晃动动画
     */
    public void setShakeAnimation(){
    	this.setAnimation(shakeAnimation(5));
    }
    
    
    /**
     * 晃动动画
     * @param counts 1秒钟晃动多少下
     * @return
     */
    public static Animation shakeAnimation(int counts){
    	Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
    	translateAnimation.setInterpolator(new CycleInterpolator(counts));
    	translateAnimation.setDuration(1000);
    	return translateAnimation;
    }
 
 
}
