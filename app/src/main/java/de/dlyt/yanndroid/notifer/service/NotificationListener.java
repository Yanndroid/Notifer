package de.dlyt.yanndroid.notifer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import de.dlyt.yanndroid.notifer.utils.ColorUtil;
import de.dlyt.yanndroid.notifer.utils.HttpRequest;
import de.dlyt.yanndroid.notifer.utils.Preferences;

public class NotificationListener extends NotificationListenerService {

    private Preferences mPreferences;
    private HashMap<String, Integer> mEnabledPackages;
    private List<Preferences.ServerInfo> mServers;

    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = new Preferences(this);

        mEnabledPackages = mPreferences.getEnabledPackages(enabledPackages -> mEnabledPackages = enabledPackages);
        mServers = mPreferences.getServers(servers -> mServers = servers);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        transmitNotification(sbn, false);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        transmitNotification(sbn, true);
    }

    private void transmitNotification(StatusBarNotification sbn, boolean removed) {
        String packageName = sbn.getPackageName();
        if (mPreferences.isServiceEnabled() && mEnabledPackages.containsKey(packageName)) {
            try {
                JSONObject body = makeBody(sbn, removed);

                for (Preferences.ServerInfo mServer : mServers) {
                    body.put("color", ColorUtil.convertColor(
                            mEnabledPackages.get(packageName),
                            mServer.colorFormat));
                    HttpRequest.post(mServer.url, body);
                }

            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject makeBody(StatusBarNotification sbn, boolean removed) throws JSONException, NullPointerException {
        Bundle bundle = sbn.getNotification().extras;
        String packageName = sbn.getPackageName();
        CharSequence label;

        try {
            PackageManager pm = getPackageManager();
            label = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0));
        } catch (PackageManager.NameNotFoundException e) {
            label = packageName;
        }

        return HttpRequest.makeBody(
                label,
                packageName,
                sbn.getId(),
                sbn.getPostTime(),
                sbn.isOngoing(),
                sbn.getNotification().extras.getString(Notification.EXTRA_TEMPLATE),
                removed,
                bundle.getString("android.title"),
                bundle.getString("android.text"),
                bundle.getString("android.subText"),
                bundle.getString("android.title.big"),
                bundle.getString("android.bigText"),
                bundle.getBoolean("android.progressIndeterminate"),
                bundle.getInt("android.progressMax"),
                bundle.getInt("android.progress"),
                mNotificationManager.getCurrentInterruptionFilter()
        );
    }

}
