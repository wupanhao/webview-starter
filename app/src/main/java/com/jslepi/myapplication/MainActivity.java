package com.jslepi.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    WebView myWebView;
    //请求状态码
    private static final int REQUEST_PERMISSION_CODE = 1;
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    public static final int FILE_CHOOSER_RESULT_CODE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private ValueCallback mFilePathCallback;
    private String mCameraPhotoPath;
    ValueCallback<Uri> uploadMessage;
    ValueCallback<Uri[]> uploadMessageAboveL;
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Window win = getWindow();
//        No Statusbar
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        No Titlebar
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
//        开启此选项才支持localStorage
        webSettings.setDomStorageEnabled(true);
//        设置true,才能让Webivew支持<meta>标签的viewport属性
        webSettings.setUseWideViewPort(true);
//        设置可以支持放缩
        webSettings.setSupportZoom(true);
//         设置出现缩放工具
        webSettings.setBuiltInZoomControls(true);
//         设定缩放控件隐藏
        webSettings.setDisplayZoomControls(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //设置允许JS弹窗

        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

//        For Debug Option
//        webSettings.setDisplayZoomControls(true);
//        myWebView.loadUrl("http://192.168.50.76:8601/editor.html");
//        myWebView.loadUrl("http://192.168.50.243:20110/build/editor.html");
//        myWebView.loadUrl("https://turbowarp.org/editor");


        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        myWebView.setDownloadListener(new DownloadListener() {

            public void downBlobUrl(WebView web,String url) {
                if (url.startsWith("blob")) {
                    String blob = "  var request = new XMLHttpRequest();" +
                            "        request.open('GET', '" + url + "', true);" +
                            "        request.setRequestHeader('Content-type', 'text/plain');" +
                            "        request.responseType = 'blob';" +
                            "        request.onload = function (e) {" +
                            "            if (this.status === 200) {" +
                            "                var blobFile = this.response;" +
                            "                var reader = new FileReader();" +
                            "                reader.readAsDataURL(blobFile);" +
                            "                reader.onloadend = function() {" +
                            "                var base64data = reader.result;" +
                            "                window.android.download(base64data);" +
                            "                }" +
                            "             }" +
                            "        };" +
                            "        request.send();";
                    String js = "javascript:"+blob;
                    web.evaluateJavascript(js,null);
                }
            }

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Log.d(TAG, url+'/'+contentDisposition+ '/'+mimetype);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    checkAndAskForPermissions();
                }

                downBlobUrl(myWebView,url);
//                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.replace("null", "file://")));
//                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
//                request.setDescription("Downloading file...");
//                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
//                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//                dm.enqueue(request);
//                Toast.makeText(getApplicationContext(), "Downloading...", Toast.LENGTH_SHORT).show();
//                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }

//            BroadcastReceiver onComplete = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    Toast.makeText(getApplicationContext(), "Downloading Complete", Toast.LENGTH_SHORT).show();
//                }
//            };
        });


        myWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(final PermissionRequest request) {

                MainActivity.this.runOnUiThread(new Runnable(){
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        Log.d(TAG,"called onPermissionRequest");
                        request.grant(request.getResources());
                    }// run
                });// MainActivity

            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }


            //For Android API >= 21 (5.0 OS)
            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openFileChooserActivity();
                return true;
            }

        });

        myWebView.addJavascriptInterface(new webAppInterface(), "android");

        checkAndAskForPermissions();
//        myWebView.loadUrl("https://webrtc.github.io/samples/src/content/getusermedia/gum/");
//        myWebView.loadUrl("https://marcusbelcher.github.io/wasm-asm-camera-webgl-test/index.html");
        myWebView.loadUrl("file:///android_asset/build/editor.html");
//        myWebView.loadUrl("http://192.168.50.243:20110/build/editor.html");

    }

    private void openFileChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i, "File Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    // To handle "Back" key press event for WebView to go back to previous screen.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }




//    可自定义处理横竖屏切换等事件
    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }
    */

    private void checkAndAskForPermissions() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {

            } else ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else {
            // Permission has already been granted

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }


}

class webAppInterface {
    @JavascriptInterface
    public void download(String base64)  {
        String path = Environment.getExternalStorageDirectory().toString() + "/Download/乐派作品.sb3";
        try{
            Base64Utils.decoderBase64File(base64.replace("data:application/x.scratch.sb3;base64,",""),path);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class Base64Utils {
    /**
     * encodeBase64File:(将文件转成base64 字符串).
     *
     * @param path 文件路径
     * @return
     * @throws Exception
     * @since JDK 1.6
     */
    public static String encodeBase64File(String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

    /**
     * decoderBase64File:(将base64字符解码保存文件).
     *
     * @param base64Code 编码后的字串
     * @param savePath   文件保存路径
     * @throws Exception
     * @since JDK 1.6
     */
    public static void decoderBase64File(String base64Code, String savePath) throws Exception {
        File file = new File(savePath);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
        byte[] buffer = Base64.decode(base64Code, Base64.DEFAULT);
        FileOutputStream out = new FileOutputStream(savePath);
        out.write(buffer);
        out.close();
    }
}

