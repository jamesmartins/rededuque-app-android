package br.com.android.postovalle;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.IOException;

import br.com.android.postovalle.model.UrlServer;
import br.com.android.postovalle.model.User;
import br.com.android.postovalle.utils.Constants;
import br.com.android.postovalle.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private WebView mWebView;
    private ProgressBar progressBar;
    private AlarmManager alarmManager;
    Button stop;
    private PendingIntent pendingIntent;
    private String userId;
    private UrlServer mUrlServer = null;
    private OkHttpClient okHttpClient = new OkHttpClient();

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private boolean mAlreadyStartedService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        OneSignal.startInit(this)
//                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
//                .unsubscribeWhenNotificationsAreDisabled(true)
//                .autoPromptLocation(true)
//                .init();

        if (!checkPermission())
            requestPermission();

        initViews();
        doLoadRequest();

        //initLocationTrackingService();

        //OneSignal.addSubscriptionObserver(this);
    }

    private void sendLocationUpdates(String longitude, String latitude) {
        String userId = Utils.readFromPreferences(getApplicationContext(), "UserSAVED", "null");
        if (userId != "null" && userId.length() != 0) {
            String lat = latitude;
            String longt = longitude;
            String url = "http://adm.bunker.mk/wsjson/LATLNG.do?lat=" + lat + "&long=" + longt + "&UserID=" + userId;
            doSendGetRequest(url);
        } else {
            Log.d(TAG, "The user no logged...");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mUrlServer != null)
//            loadContent(mUrlServer.urlDefault);

    }


    /**
     * Start permissions requests.
     */
    private void requestPermissions() {

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);


        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i("Resquest Location...", "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }


    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;

    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If img_user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i(TAG, "Permission granted, updates requested, starting location updates");

            } else {
                // Permission denied.

                // Notify the img_user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the img_user for permission (device policy or "Never ask
                // again" prompts). Therefore, a img_user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    public void doLoadRequest(){
        boolean isConnected = Utils.isNetworkConnected(getApplicationContext());
        if (!isConnected) {
            Toast.makeText(this, "Falta de Conexão!", Toast.LENGTH_SHORT).show();
            return;
        }

        loadContent(Constants.mUrl.trim());
    }

    public void doSendGetRequest(String url){
        Log.d(TAG, "Sending Settings to Duque Server...");
        boolean isConnected = Utils.isNetworkConnected(getApplicationContext());
        if (!isConnected) {
            Toast.makeText(this, "Falta de Conexão!", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        // Error
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Toast.makeText(MainActivity.this, "Erro de Comunicação!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String result = response.body().string();

                    }
                });
    }


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : Utils.REQUEST_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermission () {
        ActivityCompat.requestPermissions(this, Utils.REQUEST_PERMISSIONS, Utils.REQUEST_PERMISSION_CODE);
    }

    private void checkPermissionINline() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    private void loadContent(String url) {
        boolean isConnected = Utils.isNetworkConnected(getApplicationContext());
        if (mWebView != null) {
            if (isConnected) {
                if (!url.equals(""))
                    mWebView.loadUrl(url.trim());
                else
                    Toast.makeText(getApplicationContext(), "Erro no carregamento da página!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Sem Conexão!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        progressBar = findViewById(R.id.progress);
        mWebView = findViewById(R.id.mwebview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setBuiltInZoomControls(false);

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new CustomWebViewClient());
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {}

    private class CustomWebViewClient extends WebViewClient {

        private String cookies;
        private User user;
        private JSONObject userObj;

        public String getCookie(String url,String cookieName){
            String CookieValue = null;

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(mWebView, true);
            } else {
                cookieManager.setAcceptCookie(true);
            }

            String cookies = cookieManager.getCookie(url);
            String[] temp=cookies.split(";");
            for (String ar1 : temp ){
                if(ar1.indexOf(cookieName) == 0){
                    String[] temp1=ar1.split("=");
                    CookieValue = temp1[1];
                    break;
                }
            }
            return CookieValue;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
            MainActivity.this.progressBar.setProgress(100);

//            // Get Data Cookies objects
//            if (url.contains("log=1")) {
//                cookies =  getCookie(url, "POSTOS_VALLE");
//                try {
//                    cookies = URLDecoder.decode(cookies, "UTF-8");
//                    user = Json.toUser(cookies);
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//                OneSignal.sendTags(user.jsonObject);
//                if (!user.email.isEmpty() && user.email.length() > 0)
//                    OneSignal.setEmail(user.email);
//
//                Utils.saveToPreference(getApplicationContext(), "UserSAVED", user.id);
//                Log.i("OneSignal", "Tags Sent.......");
//            }

            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);

//            // Intecept Data Valiables objects
            if(url.contains("waze://")){
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    intent =
                            new Intent( Intent.ACTION_VIEW, Uri.parse( "https://play.google.com/store/apps/details?id=com.waze"));
                    startActivity(intent);
                }
                view.stopLoading();
            }
//            //  https://maps.google.com/
            if(url.contains("maps.google.com")){
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    intent =
                            new Intent( Intent.ACTION_VIEW, Uri.parse( "https://play.google.com/store/apps/details?id=com.waze"));
                    startActivity(intent);
                }
                view.stopLoading();
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
            MainActivity.this.progressBar.setProgress(0);
            super.onPageStarted(view, url, favicon);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        // Otherwise defer to system default behavior.
        super.onBackPressed();
    }
}
