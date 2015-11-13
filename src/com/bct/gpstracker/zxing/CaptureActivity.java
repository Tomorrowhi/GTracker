package com.bct.gpstracker.zxing;

import java.io.IOException;
import java.util.Vector;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.zxing.camera.CameraManager;
import com.bct.gpstracker.zxing.decoding.CaptureActivityHandler;
import com.bct.gpstracker.zxing.decoding.InactivityTimer;
import com.bct.gpstracker.zxing.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

public class CaptureActivity extends BaseActivity implements Callback
{

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private Button cancelButton;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zxing);
		
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		cancelButton = (Button) findViewById(R.id.btn_cancel_scan);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CaptureActivity.this.finish();
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface)
		{
			initCamera(surfaceHolder);
		}
		else
		{
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
		{
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy()
	{
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder)
	{
		try
		{
			CameraManager.get().openDriver(surfaceHolder);
		}
		catch (IOException ioe)
		{
			return;
		}
		catch (RuntimeException e)
		{
			return;
		}
		if (handler == null)
		{
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (!hasSurface)
		{
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView()
	{
		return viewfinderView;
	}

	public Handler getHandler()
	{
		return handler;
	}

	public void drawViewfinder()
	{
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(final Result obj, Bitmap barcode)
	{
	       inactivityTimer.onActivity();	//此处的代码是扫描成功直接进行返回上一个页面
	        playBeepSoundAndVibrate();
	        String resultString = obj.getText();
	        //FIXME
	        if (resultString.equals("")) {
	            Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
	        }else {
//	            System.out.println("Result:"+resultString);
	            Intent resultIntent = new Intent();
	            Bundle bundle = new Bundle();
	            bundle.putString("result", resultString);
	            resultIntent.putExtras(bundle);
	            this.setResult(RESULT_OK, resultIntent);
	        }
	        CaptureActivity.this.finish();
//		inactivityTimer.onActivity();	//这里是弹出对话框进行IMEI号的显示
//		playBeepSoundAndVibrate();
//		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//		if (barcode == null)
//		{
//			dialog.setIcon(null);
//		}
//		else
//		{
//
//			Drawable drawable = new BitmapDrawable(barcode);
//			dialog.setIcon(drawable);
//		}
//		dialog.setTitle(R.string.zxing_dialog_title);
//		dialog.setMessage(obj.getText());
//		dialog.setNegativeButton(R.string.zxing_dialog__sure, new DialogInterface.OnClickListener()
//		{
//			@Override
//			public void onClick(DialogInterface dialog, int which)
//			{
////				Intent intent = new Intent();
////				intent.setAction("android.intent.action.VIEW");
////				Uri content_url = Uri.parse(obj.getText());
////				intent.setData(content_url);
////				startActivity(intent);
//				
//				String objString=obj.getText();
//				String[] objStr=objString.split(";");
//				addDevice(objStr[0],objStr[1]);
////				finish();
//			}
//		});
//		dialog.setPositiveButton(R.string.zxing_dialog_cancel, new DialogInterface.OnClickListener()
//		{
//			@Override
//			public void onClick(DialogInterface dialog, int which)
//			{
//				finish();
//			}
//		});
//		dialog.create().show();
	}

	private void initBeepSound()
	{
		if (playBeep && mediaPlayer == null)
		{
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try
			{
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			}
			catch (IOException e)
			{
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate()
	{
		if (playBeep && mediaPlayer != null)
		{
			mediaPlayer.start();
		}
		if (vibrate)
		{
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener()
	{
		public void onCompletion(MediaPlayer mediaPlayer)
		{
			mediaPlayer.seekTo(0);
		}
	};
	
	/**
	 * 添加设备
	 * @param
	 */
	private void addDevice(String name,String imei){
//		try {
//			JSONObject object = new JSONObject();
//			object.put("id", "0");
//			object.put("name", name);
////			object.put("phone", phone);
//			object.put("imei", imei);
//			HttpEntity entity = new StringEntity(object.toString(),"UTF-8");
//			AsyncHttpClient client = new AsyncHttpClient();
//			client.addHeader("accesskey", Session.getInstance().getAccessKey());
//			client.post(CaptureActivity.this,
//					Utils.getMetaValue(CaptureActivity.this, "base_url")+CommonRestPath.deviceAdd(),
//					entity, "", new AsyncHttpResponseHandler(){
//				@Override
//				public void onSuccess(int statusCode, String content) {
//				//	System.out.println(content);
//					String retCode = "";
//					try {
//						JSONObject object = new JSONObject(content);
//						if(object.has("head")){
//							retCode = object.getJSONObject("head").getString("retcode");
//						}
//						if(!retCode.equals("")){
//							if(retCode.equals("1")){
//								CaptureActivity.this.finish();
//							}else{
//								Toast.makeText(CaptureActivity.this, R.string.add_device_err, Toast.LENGTH_SHORT).show();
//								CaptureActivity.this.finish();
//							}
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//				@Override
//				public void onFailure(Throwable error, String content) {
//				//	System.err.println(content);
//				}
//				@Override
//				public void onFinish() {
////					progressBar.setVisibility(View.GONE);
//					WizardAlertDialog.getInstance().closeProgressDialog();
//				}
//				@Override
//				public void onStart() {
////					System.out.println("接口URL："+Utils.getMetaValue(RegistActivity.this, "base_url")+CommonRestPath.checkPhone());
//					WizardAlertDialog.getInstance().showProgressDialog(R.string.add_device_post_data, CaptureActivity.this);
////					progressBar.setVisibility(View.VISIBLE);
//				}
//			});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}