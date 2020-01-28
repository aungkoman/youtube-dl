package com.teamcs.pythondownloader;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    Button button,btn_browse;
    private WebView webView;
    private static final String TAG = "teamcs";
    String current_url;
    String loading_message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));

        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        current_url = "";
        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        btn_browse = (Button) findViewById(R.id.btn_browse);
        // webview initialization
        webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);


        loading_message = getString(R.string.loading_message);
        final ProgressDialog pd = ProgressDialog.show(this, "", loading_message,true);
        pd.dismiss();
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!pd.isShowing()) {
                    pd.show();
                }
                current_url = view.getUrl();
                editText.setText(current_url);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if(pd!=null && pd.isShowing())
                {
                    pd.dismiss();
                }
            }

            public void onReceivedError(WebView webView, int i, String str, String str2) {
                webView.loadUrl("file:///android_asset/error.html");
                getSupportActionBar().show();

            }

        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                final String filename= URLUtil.guessFileName(url, contentDisposition, mimetype);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show();
            }
        });



        webView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress) {
                pd.setMessage(loading_message+ " " +progress+" %");
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "button is clicked");
                // get text
                String url = editText.getText().toString();
                //url = "https://www.pornhub.com/view_video.php?viewkey=ph5e14b3f346fee";
                // we have to check url
                // not for now
                // call python console.
                Python py = Python.getInstance();
                Log.i(TAG, "Python is started");
                PyObject youtube_dl = py.getModule("youtube_dl");
                Log.i(TAG, "Youtube-dl is started");
                //PyObject os = py.getModule("os");
                //Log.i(TAG, "os is started");
                //os.get("environ").asMap().get("URL").toString();
                //os.get("environ").asMap().put(
                //        PyObject.fromJava("URL"),
                //       PyObject.fromJava("https://www.pornhub.com/view_video.php?viewkey=ph5e14b3f346fee"));
                //Log.i(TAG,"url is set in enviroment variable (url)");

                //Log.i(TAG, "url is "+os.get("environ").asMap().get("URL").toString());
                PyObject ydl = youtube_dl.callAttr("YoutubeDL");
                boolean tf = false;
                boolean tff = true;
                // 'nocheckcertificate:': True
                // new Kwarg("nocheckcertificate",tff)
                PyObject result = ydl.callAttr("extract_info",url, new Kwarg("download",tf));
                Log.i(TAG,"RESULT IS COME OUT");

                // entries or get 0
                try{
                    String video_url = result.get("entries").asList().get(0).asMap().get("url").toString();
                    Log.i(TAG,"video_url is "+video_url);

                    webView.loadUrl(video_url);
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(video_url));
                    //final String filename= URLUtil.guessFileName(url, contentDisposition, mimetype);
                    final String filename= "test.mp4";
                    //request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                            Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    Log.i(TAG,"Entries fail "+e.toString());
                    // os.get("environ").asMap().get("HELLO").toString();
                    String video_url = result.asMap().get("url").toString();
                    Log.i(TAG,"video_url is "+video_url);
                    webView.loadUrl(video_url);
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(video_url));
                    //final String filename= URLUtil.guessFileName(url, contentDisposition, mimetype);
                    final String filename= "test.mp4";
                    //request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                            Toast.LENGTH_LONG).show();
                }

            }
        });



        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "btn_browse is clicked");
                // get text
                String url = editText.getText().toString();
                //url = "https://www.pornhub.com/view_video.php?viewkey=ph5e14b3f346fee";
                // we have to check url
                // not for now
                // call python console.

                webView.loadUrl(url);

            }
        });
        // we need button
        // add listner
        // run python code for input url
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
