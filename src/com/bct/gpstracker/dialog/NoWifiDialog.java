package com.bct.gpstracker.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.lurencun.service.autoupdate.Version;
import com.lurencun.service.autoupdate.internal.VersionDialogListener;

public class NoWifiDialog {

	private Context context;
	private Version version;
	private VersionDialogListener listener;

	public NoWifiDialog(Context context, Version version, VersionDialogListener listener){
		this.context = context;
		this.version = version;
		this.listener = listener;
	}
	
	public void show(){
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_found_version);
//        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView feature = (TextView) dialog.findViewById(R.id.feature);
		title.setText("检测到正在使用手机流量");
		feature.setText("更新会消耗手机流量，是否继续");
        feature.setMovementMethod(ScrollingMovementMethod.getInstance());
        
        View ignore = dialog.findViewById(R.id.ignore);
        Button update = (Button) dialog.findViewById(R.id.update);
		update.setText("继续");
        CheckBox laterOnWifi = (CheckBox) dialog.findViewById(R.id.only_wifi);
		laterOnWifi.setVisibility(View.GONE);
        

        ignore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				listener.doIgnore();
			}
		});
        
        update.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				listener.doUpdate(false);
			}
		});
		dialog.setCancelable(false);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();
        
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();                
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);  
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.heightPixels;
        int height = metrics.widthPixels;
	    if (height > width) {  
	        lp.width = (int) (width * 0.9);          
	    } else {  
	        lp.width = (int) (width * 0.5);                  
	    }  
	    dialog.getWindow().setAttributes(lp);
	}
}
