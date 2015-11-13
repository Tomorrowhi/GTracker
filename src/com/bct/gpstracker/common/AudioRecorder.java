package com.bct.gpstracker.common;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaRecorder;

public class AudioRecorder
{
	private static int SAMPLE_RATE_IN_HZ = 8000; 

	final MediaRecorder recorder = new MediaRecorder();
	final String path;
	final String folder;
	Context context;

	public AudioRecorder(Context context,String folderName,String path)
	{
		this.context = context;
		this.folder = folderName;
		this.path = initializePath(context, folderName, path);
	}

	private String initializePath(Context context, String folderName, String path)
	{
		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}
		if (!path.contains("."))
		{
			path += ".amr";
		}
//		return Environment.getExternalStorageDirectory().getAbsolutePath()+ "/gpstracker" + path;
		return context.getFilesDir()+ "/"+folderName+"/" + path;
	}

	public void start() throws IOException
	{
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) { throw new IOException(
				"SD Card is not mounted,It is  " + state + "."); }
		File directory = new File(path).getParentFile();
		if (!directory.exists() && !directory.mkdirs()) { throw new IOException(
				"Path to file could not be created"); }
        File file=new File(path);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        recorder.reset();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//		recorder.setAudioChannels(AudioFormat.CHANNEL_CONFIGURATION_MONO);
		recorder.setAudioSamplingRate(SAMPLE_RATE_IN_HZ);
		recorder.setOutputFile(path);
		recorder.prepare();
		recorder.start();
	}

	public void stop() throws IOException
	{
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getAmplitude() {
        if (recorder != null) {
            return (recorder.getMaxAmplitude());
        } else
            return 0;
    }
}