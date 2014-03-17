package com.mgoenka.filedownloader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class FileDownloaderActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_downloader);
	}

	public void onDownload(View v) {
		// execute this when the downloader must be fired
		final DownloadTask downloadTask = new DownloadTask(FileDownloaderActivity.this);

		downloadTask.execute("http://web.mit.edu/bentley/www/papers/a30-bentley.pdf");
		// downloadTask.execute("http://www.mohitgoenka.com/Resume.pdf");
	}
}
