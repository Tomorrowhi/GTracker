package com.bct.gpstracker.dialog;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;

import com.bct.gpstracker.R;

public class WizardAlertDialog {
	private ProgressDialog pdialog;
    private static WizardAlertDialog dialogs;
    
    private WizardAlertDialog() {

    }
    
    public static synchronized WizardAlertDialog getInstance() {
        if (dialogs == null) {
            dialogs = new WizardAlertDialog();
        }
        return dialogs;
    }
    
    /**
    * Dialog to show error with custom String on button.
    *
    * @param activityContext
    * the parent Activity context.
    * the message to display.
    * the String resource id to display text on button.
    */
    public static void showErrorDialog(Context activityContext, int resID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(R.string.title_error)
                .setMessage(activityContext.getString(resID))
                .setPositiveButton(R.string.btn_ok, null);
        AlertDialog alert = builder.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alert.show();
    }
    
    public static void showErrorDialog(Context activityContext, String message, int btnNameResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(R.string.title_error).setMessage(message).setPositiveButton(btnNameResId, null);
        AlertDialog alert = builder.create();
        alert.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alert.show();
    }
    
    /**
    * Stops running progress-bar.
    */
    public void closeProgressDialog() {
        if (pdialog != null) {
            pdialog.dismiss();
            pdialog = null;
        }
    }
    
    /**
    * Shows progress-bar.
    *
    * @param resID
    * the String resource id to display message on progress bar.
    * @param ctx
    * the parent Activity context.
    */
    public void showProgressDialog(int resID, Context ctx) {
        String message = ctx.getString(resID);
        pdialog = ProgressDialog.show(ctx, null, message, true, true);
        pdialog.setCancelable(true);
    }

    public void showProgressDialog(int resID, Context ctx,int flag) {
        String message = ctx.getString(resID);
        pdialog = ProgressDialog.show(ctx, null, message, true, true);
        pdialog.setCancelable(true);
        pdialog.setCanceledOnTouchOutside(false);
    }

    public void showProgressDialog(String message, Context ctx) {
        pdialog = ProgressDialog.show(ctx, null, message, true, true);
        pdialog.setCancelable(true);
    }
    
    public void showProgressDialog(int resID, Context ctx,boolean cancelable) {
        String message = ctx.getString(resID);
        pdialog = ProgressDialog.show(ctx, null, message, true, true);
        pdialog.setCancelable(cancelable);
    }
    
    /**
    * Dialog to show authentication error.
    *
    * @param activityContext
    * the parent Activity context.
    * @param message
    * the message to display on dialog.
    * @param btnNameResId
    * the String resource id to display message on Button.
    */
    public static void showAuthenticationErrorDialog(final Activity activityContext, String message, int btnNameResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(R.string.title_error).setMessage(message)
                .setPositiveButton(btnNameResId, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activityContext.finish();

                    }
                });
        AlertDialog alert = builder.create();
        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alert.show();
    }

    /**
    * Dialog to show result.
    *
    * @param activityContext
    * the parent Activity context.
    * @param message
    * the message to display on dialog.
    * @param btnNameResId
    * the String resource id to display message on Button.
    * @param txtResult
    * the String resource id to display title.
    */
    public static void showResultDialog(final Activity activityContext,
            String message, int btnNameResId, int txtResult) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(txtResult).setMessage(message)
                .setPositiveButton(btnNameResId, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activityContext.finish();

                    }
                });
        AlertDialog alert = builder.create();
        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alert.show();
    }
    
    /**
    * Dialog to show message.
    *
    * @param nMessage
    * the message to display on dialog.
    * @param resId
    * the String resource id to display title.
    * @param context
    * the parent Activity context.
    */
    public static void showMessageDialog(String nMessage, int resId,
            Context context) {
        AlertDialog.Builder hueAlerts = new AlertDialog.Builder(context);
        hueAlerts.setTitle(resId);
        hueAlerts.setMessage(nMessage);
        hueAlerts.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        hueAlerts.setCancelable(false);
        hueAlerts.show();
    }
    
    /**
     * 显示日期选择对话框
     * @param ctx
     * @param year 年
     * @param month 月
     * @param day 日
     * @param showView 展示的控件
     */
    public static void showDateDialog(Context ctx,int year,int month,int day,final EditText showView){
    	DatePickerDialog dialog = null;
    	if(year==0||month==0||day==0){
    		dialog = new DatePickerDialog(ctx,new OnDateSetListener() {
    			@Override
    			public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
    				WizardAlertDialog.updateDate(year,monthOfYear+1,dayOfMonth,showView);
    			}
    		}, Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    	}else{
    		dialog = new DatePickerDialog(ctx,new OnDateSetListener() {
    			@Override
    			public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
    				WizardAlertDialog.updateDate(year,monthOfYear+1,dayOfMonth,showView);
    			}
    		}, year,month-1, day);
		}
		dialog.show();
    }
    
	/**
	 * 日期文本框显示
	 */
	public static void updateDate(int year,int month,int day,EditText view){
		String yearStr = String.valueOf(year);
		String monthStr = "";
		String dayStr = "";
		if(month>0&&month<10){
			monthStr = "0"+month;
		}else {
			monthStr = String.valueOf(month);
		}
		if(day>0&&day<10){
			dayStr = "0"+day;
		}else {
			dayStr = String.valueOf(day);
		}
		view.setText(yearStr + "-" + monthStr + "-" + dayStr);
	}



}
