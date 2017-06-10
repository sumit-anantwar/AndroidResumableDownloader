package com.sumitanantwar.android_resumable_downloader.sample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sumitanantwar.android_resumable_downloader.DownloadRequest;
import com.sumitanantwar.android_resumable_downloader.DownloadRequestCallback;
import com.sumitanantwar.android_resumable_downloader.DownloadRequestError;
import com.sumitanantwar.android_resumable_downloader.Downloadable;
import com.sumitanantwar.android_resumable_downloader.RetryMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

                String baseUrl = "http://staticfiles.popguide.me/";
                String extn = ".tgz";
                String baseDestination = getApplicationContext().getFilesDir() + File.separator + "Packages" + File.separator;

                String[] files = {"it", "ja", "ko", "pl", "pt", "ru", "en"};

                List<Downloadable> downloadables = new ArrayList<Downloadable>();
                int tag = 0;
                for (String fn : files) {

                    String url = baseUrl + fn + extn;
                    String destn = baseDestination + fn + extn;

                    Downloadable d = new Downloadable(url, destn);
                    d.setOnDownloadListener(new Downloadable.OnDownloadListener()
                    {
                        @Override
                        public void onDownloadComplete(Downloadable downloadable)
                        {
                            Log.i(LOG_TAG, "Downloaded :  Tag - " + downloadable.getTag() + " - " + downloadable.getTargetUrl() + " - to : " + downloadable.getDestinationPath());
                        }

                        @Override
                        public void onDownloadFailure(int responseCode, Map<String, List<String>> headerMap)
                        {
                            Log.i(LOG_TAG, "Downloaded Failed :  Response Code - " + responseCode);
                        }
                    });
                    downloadables.add(d);
                    d.setTag(tag);

                    tag++;
                }

//                downloadables.clear();
                Downloadable dldbl1 = new Downloadable("https://assets.popguide.me/uploads/home_location/image/3/mobile_Screenshot_from_2016-11-23_13-11-32.png", "/data/user/0/io.populi.pop_app_android/files/homeLocationImages/mobile_Screenshot_from_2016-11-23_13-11-32.png");
                downloadables.add(dldbl1);
                Downloadable dldbl2 = new Downloadable("https://assets.popguide.me/uploads/home_location/image/2/mobile_Rome.jpeg", "/data/user/0/io.populi.pop_app_android/files/homeLocationImages/mobile_Rome.jpeg");
                downloadables.add(dldbl2);

                DownloadRequest request = new DownloadRequest(context);
                request.download(downloadables, callback);
            }
        });
    }

    private DownloadRequestCallback callback = new DownloadRequestCallback()
    {
        @Override
        public void onDownloadComplete() {

            Toast.makeText(context, "Download Complete", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDownloadIncomplete(List<Downloadable> incompleteDownloadables)
        {
            Toast.makeText(context, "Download Incomplete", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDownloadProgress(long completedBytes, long totalBytes) {

            Log.i(LOG_TAG, String.format("Download Progress : %s / %s", completedBytes, totalBytes));
        }

        @Override
        public void onDownloadFailure(DownloadRequestError error) {

            String msg = "Download Failed with Error : " + error.name();
            Log.e(LOG_TAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    };

}
