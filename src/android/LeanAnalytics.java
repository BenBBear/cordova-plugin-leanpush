package leancloud.cordova.leanpush;

import java.util.HashMap;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.avos.avoscloud.AVAnalytics;

/**
 * This class echoes a string called from JavaScript.
 */
public class LeanAnalytics extends CordovaPlugin {

    public static final String ACTION_VIEW_START = "onViewStart";
    public static final String ACTION_VIEW_END = "onViewEnd";
    public static final String ACTION_EVENT = "event";
    public static final String ACTION_EVENT_START = "onEventStart";
    public static final String ACTION_EVENT_END = "onEventEnd";
    public static final String ACTION_KV_EVENT_START = "onKVEventStart";
    public static final String ACTION_KV_EVENT_END = "onKVEventEnd";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_VIEW_START)) {
            this.onViewStart(args.getString(0), callbackContext);
            return true;
        }
        if (action.equals(ACTION_VIEW_END)) {
            this.onViewEnd(args.getString(0), callbackContext);
            return true;
        }
        if (action.equals(ACTION_EVENT)) {
            if (args.length() == 1) {
                this.event(args.getString(0), callbackContext);
            } else if (args.length() == 2) {
                this.event(args.getString(0), args.getString(1), callbackContext);
            }
            return true;
        }
        if (action.equals(ACTION_EVENT_START)) {
            if (args.length() == 1) {
                this.onEventStart(args.getString(0), callbackContext);
            } else if (args.length() == 2) {
                this.onEventStart(args.getString(0), args.getString(1), callbackContext);
            }
            return true;
        }
        if (action.equals(ACTION_EVENT_END)) {
            if (args.length() == 1) {
                this.onEventEnd(args.getString(0), callbackContext);
            } else if (args.length() == 2) {
                this.onEventEnd(args.getString(0), args.getString(1), callbackContext);
            }
            return true;
        }
        if (action.equals(ACTION_KV_EVENT_START)) {
            this.onKVEventStart(args.getString(0), args.getString(1), args.getString(2), args.getString(3), callbackContext);
            return true;
        }
        if (action.equals(ACTION_KV_EVENT_END)) {
            this.onKVEventEnd(args.getString(0), args.getString(1), callbackContext);
            return true;
        }
        return false;
    }

    private void onViewStart(final String viewId, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                AVAnalytics.onFragmentStart(viewId);
                callbackContext.success();
            }
        });
    }

    private void onViewEnd(final String viewId, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                AVAnalytics.onFragmentEnd(viewId);
                callbackContext.success();
            }
        });
    }

    private void event(final String eventId, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                AVAnalytics.onEvent(cordova.getActivity(), eventId);
                callbackContext.success();
            }
        });
    }

    private void event(final String eventId, final String label, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                AVAnalytics.onEvent(cordova.getActivity(), eventId, label);
                callbackContext.success();
            }
        });
    }

    private void onEventStart(final String eventId, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                AVAnalytics.onEventBegin(cordova.getActivity(), eventId);
                callbackContext.success();
            }
        });
    }

    private void onEventStart(final String eventId, final String label, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                AVAnalytics.onEventBegin(cordova.getActivity(), eventId, label);
                callbackContext.success();
            }
        });
    }

    private void onEventEnd(final String eventId, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                AVAnalytics.onEventEnd(cordova.getActivity(), eventId);
                callbackContext.success();
            }
        });
    }

    private void onEventEnd(final String eventId, final String label, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                AVAnalytics.onEventEnd(cordova.getActivity(), eventId, label);
                callbackContext.success();
            }
        });
    }

    private void onKVEventStart(final String eventId, final String keyName, final String attr, final String value, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                HashMap<String, String> kv = new HashMap<String, String>();
                AVAnalytics.onKVEventBegin(cordova.getActivity(), eventId, kv, keyName);
                callbackContext.success();
            }
        });
    }

    private void onKVEventEnd(final String eventId, final String keyName, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                AVAnalytics.onKVEventEnd(cordova.getActivity(), eventId, keyName);
                callbackContext.success();
            }
        });
    }
}
