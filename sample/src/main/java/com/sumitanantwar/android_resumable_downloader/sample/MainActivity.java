package com.sumitanantwar.android_resumable_downloader.sample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sumitanantwar.android_resumable_downloader.DownloadCallback;
import com.sumitanantwar.android_resumable_downloader.DownloadRequest;
import com.sumitanantwar.android_resumable_downloader.RetryMode;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = "http://staticfiles.popguide.me/de.tgz";
                String destination = getApplicationContext().getFilesDir() + File.separator + "sample.tgz";


                DownloadRequest request = new DownloadRequest();
                request.downloadURLtoFile(url, destination, new DownloadCallback() {
                    @Override
                    public void onDownloadComplete() {

                        Toast.makeText(context, "Download Complete", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onDownloadProgress(long completedBytes, long totalBytes) {

                        Log.i(LOG_TAG, String.format("Download Progress : %s / %s", completedBytes, totalBytes));
                    }

                    @Override
                    public void onDownloadFailure(RetryMode retryMode) {

                        Toast.makeText(context, "Download Failed", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

}
