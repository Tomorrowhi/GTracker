package com.bct.gpstracker.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.bct.gpstracker.R;
import com.bct.gpstracker.my.activity.AddDeviceActivity;

/**
 * Created by lc on 2015/9/10 0010.
 */
public class AddDeviceDialog {
    private static final String TAG = AddDeviceDialog.class.getSimpleName();
    private static AddDeviceDialog mAddDeviceDialog = null;
    private AlertDialog alertDialog;
    protected OnClickNegativeButton onClickNegativeButton;


    public interface OnClickNegativeButton {
        void clickNegativeButton();
    }

    public void setOnclickNegativeButton(OnClickNegativeButton onClickNegativeButton) {
        this.onClickNegativeButton = onClickNegativeButton;
    }


    public static synchronized AddDeviceDialog getInstance() {
        if (mAddDeviceDialog == null) {
            mAddDeviceDialog = new AddDeviceDialog();
        }
        return mAddDeviceDialog;
    }

    private AddDeviceDialog() {
    }

    public void showDialog(final Context context, boolean cancelable) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.add_device_notice_title).setMessage(R.string.add_device_jump).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.startActivity(new Intent(context, AddDeviceActivity.class));
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onClickNegativeButton.clickNegativeButton();
            }
        });
        alertDialog = builder.create();

        alertDialog.setCancelable(cancelable);
        alertDialog.show();



    }
}
