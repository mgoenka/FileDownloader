package com.mgoenka.filedownloader;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

//usually, subclasses of AsyncTask are declared inside the activity class.
//that way, you can easily modify the UI thread from here
class DownloadTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    long startTime = 0;
    long fileLength;
    
    String fileName = "/sdcard/Network";

    public DownloadTask(Context context) {
        this.context = context;

        startTime = System.currentTimeMillis();
        
        fileName += 1 + (int)(Math.random() * 1000)  + ".txt";
		appendLog("Download Clicked");
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        
        try {
        	URL url = new URL(sUrl[0]);
            URLConnection ucon = url.openConnection();
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            fileLength = ucon.getContentLength();

            ByteArrayBuffer baf = new ByteArrayBuffer(4096);
            int current = 0;
            long total = 0;
            
            appendLog("Download started in " + (System.currentTimeMillis() - startTime) + " miliseconds");
            
            while ((current = bis.read()) != -1) {
               baf.append((byte) current);
               total += current;
                try {
                    Thread.sleep(5000);
                    appendLog("Download progress - " + (total * 100 / fileLength) + "%, Throughput: " +
                    		current/5 + " bytes/second");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /* Convert the Bytes read to a String. */
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(baf.toByteArray());
            fos.close();

	    } catch (Exception e) {
	        return e.toString();
	    } finally {
	       try {
	           if (output != null)
	               output.close();
	           if (input != null)
	               input.close();
	       } catch (IOException ignored) {
	       }

	       if (connection != null) {
               connection.disconnect();
	       }
	    }
	    return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user 
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        long totalTime = (System.currentTimeMillis() - startTime)/1000;
        
        if (result != null)
            Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();

        appendLog("Download completed in " + totalTime + " seconds, Throughput: " +
        		fileLength/totalTime + " bytes/second");
    }
    
    public void appendLog(String text) {
        Log.i("Edx", text);

 	    File logFile = new File(fileName);
 	    String time = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss",
				Locale.getDefault()).format(new Date());
 	    
 	    text = "Time: " + time + ", Message: " + text;
 	   
 	    if (!logFile.exists()) {
 	        try {
 	            logFile.createNewFile();
 	        } 
 	        catch (IOException e) {
 	            // TODO Auto-generated catch block
 	            e.printStackTrace();
 	        }
 	    }
 	    try {
 	        //BufferedWriter for performance, true to set append to file flag
 	        BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
 	        buf.append(text);
 	        buf.newLine();
 	        buf.close();
 	    }
 	    catch (IOException e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
 	    }
 	}
}
