package leancloud.cordova.leanpush;

import java.util.Arrays;
import java.util.ArrayList;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.avos.avoscloud.PushService;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;

/**
 * This class echoes a string called from JavaScript.
 */
public class LeanPush extends CordovaPlugin {

    public static final String PROP_KEY_LEANCLOUD_APP_ID = "leancloud-appid";
    public static final String PROP_KEY_LEANCLOUD_APP_KEY = "leancloud-appkey";

    public static final String ACTION_SUBSCRIBE = "subscribe";
    public static final String ACTION_UNSUBSCRIBE = "unsubscribe";
    public static final String ACTION_CLEAR_SUBSCRIPTION = "clearSubscription";
    public static final String ACTION_GET_INSTALLTION = "getInstallation";
    public static final String ACTION_ON_NOTIFICATION_RECEIVED = "onNotificationReceived";
    private static CordovaWebView mWebView;
    private static String callback;
    private static String cacheResult;


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mWebView = webView;
        if(cacheResult != null){
            sendJsonString(cacheResult, "closed");
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_SUBSCRIBE)) {
            this.subscribe(args.getString(0), callbackContext);
            return true;
        }
        if (action.equals(ACTION_UNSUBSCRIBE)) {
            this.unsubscribe(args.getString(0), callbackContext);
            return true;
        }
        if (action.equals(ACTION_CLEAR_SUBSCRIPTION)) {
            this.clearSubscription(callbackContext);
            return true;
        }
        if(action.equals(ACTION_GET_INSTALLTION)){
            this.getInstallation(callbackContext);
            return true;
        }
        if(action.equals(ACTION_ON_NOTIFICATION_RECEIVED)){
            onNotificationReceived(args.getString(0), callbackContext);
            return true;
        }
        if(action.equals("getCacheResult")){
            getCacheResult(callbackContext);
            return true;
        }

        return false;
    }

    private static void onNotificationReceived(final String cb, final CallbackContext callbackContext){
        callback = cb;
        callbackContext.success();
    }

    private void getInstallation(final CallbackContext callbackContext) {
        String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
        if (installationId == null) {
            callbackContext.error("Fail to get Installation.");
        } else {
            callbackContext.success("android," + installationId);
        }
    }

    private void subscribe(final String channel, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    PushService.subscribe(cordova.getActivity(), channel, cordova.getActivity().getClass());
                    callbackContext.success();
                }
            });
    }

    private void unsubscribe(final String channel, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    PushService.unsubscribe(cordova.getActivity(), channel);
                    callbackContext.success();
                }
            });
    }

    private void clearSubscription(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    AVInstallation currentInstallation = AVInstallation.getCurrentInstallation();
                    currentInstallation.put("channels", new ArrayList());
                    currentInstallation.saveInBackground();
                    callbackContext.success();
                }
            });
    }

    private static void send(final String x, final String status){
        mWebView.loadUrl("javascript:" + callback +"('" +  x + "', '" + status + "');");
    }


    public static void sendJsonString(final String x, final String status){
        if(mWebView != null && callback != null){
            send(x, status);
            cacheResult = null;
        }else{
            cacheResult = x;
        }
    }

    private void getCacheResult(final CallbackContext callbackContext){
    callbackContext.success(cacheResult);
    }
}
