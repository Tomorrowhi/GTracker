package com.bct.gpstracker.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;

import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.util.Utils;

/**
 * Created by Administrator on 2015/9/6 0006.
 */
public class VibrateDialog {
    private static final String TAG = VibrateDialog.class.getSimpleName();
    private static VibrateDialog mVibrateDialog = null;
    private AlertDialog alertDialog;
    private MediaPlayer mediaPlayer;


    public static synchronized VibrateDialog getInstance() {
        if (mVibrateDialog == null) {
            mVibrateDialog = new VibrateDialog();
        }
        return mVibrateDialog;
    }

    private VibrateDialog() {
    }

    public void showDialog(final Context context, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.vibrate_dialog_bludisconnect)).setPositiveButton(context.getString(R.string.vibrate_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                bleDisconnected(context);
            }
        });
        alertDialog = builder.create();

        alertDialog.setCancelable(cancelable);
        alertDialog.show();

        if (Utils.getPreferences(context).getBoolean(Constants.MSG_VOICE, true)) {
            mediaPlayer = MediaPlayer.create(context,
                    R.raw.alarm);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    public void bleDisconnected(Context context) {
        context.sendBroadcast(new Intent(Constants.VIBRATOR_CLOSE));
        if (null != alertDialog) {
            alertDialog.dismiss();
            alertDialog.cancel();
            alertDialog = null;
        }
        if (null != mediaPlayer)
            mediaPlayer.stop();
    }


}


