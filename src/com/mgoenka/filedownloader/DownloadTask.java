package com.mgoenka.filedownloader;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    int contentLength;
    
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
        	File dest_file = new File("/sdcard/File.pdf");
            URL u = new URL(sUrl[0]);
            URLConnection conn = u.openConnection();
            contentLength = conn.getContentLength();
            
            long totalRead = 0;

            DataInputStream stream = new DataInputStream(u.openStream());
            
            long lastLogTime = System.currentTimeMillis();
            
            appendLog("Download started in " + (lastLogTime - startTime) + " miliseconds");
            
            byte[] buffer = new byte[contentLength];
            
            while (totalRead < contentLength)
            {
                int bytesRead = stream.read(buffer);
                if (bytesRead < 0)
                    throw new IOException("Data stream ended prematurely");
                totalRead += bytesRead;
                int progress = (int)((totalRead * 100) / contentLength);
                
                if (System.currentTimeMillis() > (lastLogTime + 5000)) {
                	appendLog("Download progress: " + progress + "%, Throughput: " + totalRead/5 + " bytes/second");
                	lastLogTime = System.currentTimeMillis();
                }
            }
            
            stream.close();
            DataOutputStream fos = new DataOutputStream(new FileOutputStream(dest_file));
            fos.write(buffer);
            fos.flush();
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
        long totalTime = System.currentTimeMillis() - startTime;
        long throughput = contentLength / (totalTime / 1000);
        
        if (result != null)
            Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();

        appendLog("Download completed in " + totalTime + " miliseconds");
        appendLog("Average throughput is " + throughput + " bytes/second");
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
