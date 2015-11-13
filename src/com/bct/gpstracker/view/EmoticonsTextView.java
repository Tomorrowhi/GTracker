package com.bct.gpstracker.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bct.gpstracker.MainActivity;

/**
 * Created by Admin on 2015/9/7 0007.
 */
public class EmoticonsTextView extends TextView {
    public EmoticonsTextView(Context context) {
        super(context);
    }

    public EmoticonsTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EmoticonsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!TextUtils.isEmpty(text)) {
            super.setText(replace(text), type);
        } else {
            super.setText(text, type);
        }
    }

    private Pattern buildPattern() {
        StringBuilder patternString = new StringBuilder(
                MainActivity.mEmoticons.size() * 3);
        patternString.append('(');
        for (int i = 0; i < MainActivity.mEmoticons.size(); i++) {
            String s = MainActivity.mEmoticons.get(i);
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        patternString.replace(patternString.length() - 1,
                patternString.length(), ")");
        return Pattern.compile(patternString.toString());
    }

    private CharSequence replace(CharSequence text) {
        try {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            Pattern pattern = buildPattern();
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                if (MainActivity.mEmoticonsId.containsKey(matcher.group())) {
                    int id = MainActivity.mEmoticonsId.get(matcher.group());
                    Bitmap bitmap = BitmapFactory.decodeResource(
                            getResources(), id);
                    if (bitmap != null) {
                        ImageSpan span = new ImageSpan(getContext(), bitmap);
                        builder.setSpan(span, matcher.start(), matcher.end(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            return builder;
        } catch (Exception e) {
            return text;
        }
    }
}