package com.bct.gpstracker.util;

import android.content.Context;
import android.os.Bundle;

import com.bct.gpstracker.AppContext;
import com.iflytek.cloud.speech.*;

/**
 * 语音播报组件
 *
 */
public class TTSController implements SynthesizerListener {

	public static TTSController ttsManager;
	private Context mContext;
	// 合成对象.
	private SpeechSynthesizer mSpeechSynthesizer;

	TTSController(Context context) {
		mContext = context;
	}

	public static TTSController getInstance(Context context) {
		if (ttsManager == null) {
			ttsManager = new TTSController(context);
		}
		return ttsManager;
	}

	public void init() {
		SpeechUser.getUser().login(mContext, null, null, "appid="+ AppContext.getMetaData(mContext, "xfyun_appid"), listener);
		// 初始化合成对象.
		mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext);
		initSpeechSynthesizer();
	}

	/**
	 * 使用SpeechSynthesizer合成语音，不弹出合成Dialog.
	 * 
	 * @param
	 */
	public void playText(String playText) {
		if (!isfinish) {
			return;
		}
		if (null == mSpeechSynthesizer) {
			// 创建合成对象.
			mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext);
			initSpeechSynthesizer();
		}
		// 进行语音合成.
		mSpeechSynthesizer.startSpeaking(playText, this);

	}

	public void stopSpeaking() {
		if (mSpeechSynthesizer != null)
			mSpeechSynthesizer.stopSpeaking();
	}
	public void startSpeaking() {
		 isfinish=true;
	}

	private void initSpeechSynthesizer() {
		// 设置发音人
		mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
		// 设置语速
//		mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, mContext.getString(R.string.preference_key_tts_speed));
//		// 设置音量
//		mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, mContext.getString(R.string.preference_key_tts_volume));
//		// 设置语调
//		mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, mContext.getString(R.string.preference_key_tts_pitch));

	}

	/**
	 * 用户登录回调监听器.
	 */
	private SpeechListener listener = new SpeechListener() {

		@Override
		public void onData(byte[] arg0) {
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error != null) {

			}
		}

		@Override
		public void onEvent(int arg0, Bundle arg1) {
		}
	};

	@Override
	public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {

	}

	boolean isfinish = true;

	@Override
	public void onCompleted(SpeechError arg0) {
		
		isfinish = true;
	}

	@Override
	public void onSpeakBegin() {
		
		isfinish = false;

	}

	@Override
	public void onSpeakPaused() {
		

	}

	@Override
	public void onSpeakProgress(int arg0, int arg1, int arg2) {
		

	}

	@Override
	public void onSpeakResumed() {
		

	}

	public void destroy() {
		if (mSpeechSynthesizer != null) {
			mSpeechSynthesizer.stopSpeaking();
		}
	}
}
