package me.yourbay.litehttp_sample;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import me.yourbay.litehttp.LiteHttp;

public class MainActivity extends AppCompatActivity {

    private TextView mTvResult;
    private Handler mHandler;
    private Button mBtnDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvResult = (TextView) findViewById(R.id.tv_result);
        findViewById(R.id.btn_get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGetClick(v);
            }
        });
        mBtnDown = (Button) findViewById(R.id.btn_download);
        mBtnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDownClick(v);
            }
        });
        //
        HandlerThread handlerThread = new HandlerThread("HttpRetriever");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    public void onGetClick(View v) {
        run(new Runnable() {
            @Override
            public void run() {
                final String url = "http://api.t.sina.com.cn/short_url/shorten.json?source=2483680040&url_long=" + URLEncoder.encode("http://yourbay.me");
                final String result = new LiteHttp.Request().setUrl(url).connect().result;
                showResult(result);
            }
        });
    }

    public void onDownClick(View v) {
        run(new Runnable() {
            @Override
            public void run() {
                final String url = "http://cdn.lamborghini.com/content/models/Huracan_LP_580-2/huracan-lp580-2_hook_1000x1000.jpg";
                final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "lp580" + System.currentTimeMillis() + ".jpg");
                final String result =
                        new LiteHttp.Request().setUrl(url)//
                                .setStreamListener(new LiteHttp.StreamListener() {
                                    @Override
                                    public OutputStream getOutStream() {
                                        try {
                                            file.createNewFile();
                                            return new BufferedOutputStream(new FileOutputStream(file), 16 * 1024);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }

                                    @Override
                                    public InputStream getInStream() {
                                        // if you need to upload file or add body, you can override this
                                        return null;
                                    }
                                })//
                                .setProgressListener(new LiteHttp.ProgressListener() {
                                    int lastProgress = -1;

                                    @Override
                                    public void onProgress(OutputStream os, InputStream is, long total, long current, int progress) {
                                        lastProgress = progress;
                                        mBtnDown.setText("Down" + "(" + current + "B)");
                                    }
                                })//
                                .connect().result;
                showResult(file.getAbsolutePath() + "\nDownload " + (file.length() > 0 ? "Succeed" : "Failed") + (TextUtils.isEmpty(result) ? "" : ("\n" + result)));
            }
        });
    }

    private void showResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final CharSequence preStr = mTvResult.getText();
                mTvResult.setText(result);
                mTvResult.append("\n");
                mTvResult.append(preStr);
            }
        });
    }

    private void run(Runnable runnable) {
        mHandler.post(runnable);
    }

}
