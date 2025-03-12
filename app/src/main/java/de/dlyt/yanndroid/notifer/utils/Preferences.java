package de.dlyt.yanndroid.notifer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

import de.dlyt.yanndroid.notifer.R;

public class Preferences {

    private SharedPreferences.OnSharedPreferenceChangeListener mEnabledPackagesListener; //strong reference to avoid garbage collection
    private SharedPreferences.OnSharedPreferenceChangeListener mServersListener; //strong reference to avoid garbage collection
    private SharedPreferences.OnSharedPreferenceChangeListener mPrivateModeListener; //strong reference to avoid garbage collection

    public interface PreferencesListener<T> {
        void onChange(T value);
    }

    public static class ServerInfo implements Comparable<ServerInfo> {
        public String name;
        public String url;

        public ServerInfo(String name, String url) {
            this.name = name;
            this.url = url;
        }

        @Override
        public int compareTo(ServerInfo o) {
            return this.name.compareToIgnoreCase(o.name);
        }
    }

    private static final String ENABLED_PACKAGES_KEY = "enabled_packages";
    private static final String SERVERS_KEY = "servers";
    private static final String PRIVATE_MODE_KEY = "private_mode";

    private final Context mContext;
    private final SharedPreferences mSharedPreferences;

    public Preferences(Context context) {
        this.mContext = context;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isServiceEnabled() {
        return mSharedPreferences.getBoolean(mContext.getString(R.string.preference_service_enabled_key), false);
    }

    public void setServiceEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(mContext.getString(R.string.preference_service_enabled_key), enabled).apply();
    }

    public HashMap<String, Integer> getEnabledPackages(PreferencesListener<HashMap<String, Integer>> listener) {
        if (listener != null) {
            mEnabledPackagesListener = (sharedPreferences, key) -> {
                if (ENABLED_PACKAGES_KEY.equals(key))
                    listener.onChange(loadEnabledPackages());
            };
            mSharedPreferences.registerOnSharedPreferenceChangeListener(mEnabledPackagesListener);
        }
        return loadEnabledPackages();
    }

    private HashMap<String, Integer> loadEnabledPackages() {
        return new Gson().fromJson(mSharedPreferences.getString(ENABLED_PACKAGES_KEY, "{}"), new TypeToken<HashMap<String, Integer>>() {
        }.getType());
    }

    public void setEnabledPackages(HashMap<String, Integer> enabledPackages) {
        mSharedPreferences.edit().putString(ENABLED_PACKAGES_KEY, new Gson().toJson(enabledPackages)).apply();
    }

    public List<ServerInfo> getServers(PreferencesListener<List<ServerInfo>> listener) {
        if (listener != null) {
            mServersListener = (sharedPreferences, key) -> {
                if (SERVERS_KEY.equals(key))
                    listener.onChange(loadServers());
            };
            mSharedPreferences.registerOnSharedPreferenceChangeListener(mServersListener);
        }
        return loadServers();
    }

    private List<ServerInfo> loadServers() {
        return new Gson().fromJson(mSharedPreferences.getString(SERVERS_KEY, "[]"), new TypeToken<List<ServerInfo>>() {
        }.getType());
    }

    public void setServers(List<ServerInfo> servers) {
        mSharedPreferences.edit().putString(SERVERS_KEY, new Gson().toJson(servers)).apply();
    }

    public boolean getPrivateMode(PreferencesListener<Boolean> listener) {
        if (listener != null) {
            mPrivateModeListener = (sharedPreferences, key) -> {
                if (PRIVATE_MODE_KEY.equals(key))
                    listener.onChange(loadPrivateMode());
            };
            mSharedPreferences.registerOnSharedPreferenceChangeListener(mPrivateModeListener);
        }
        return loadPrivateMode();
    }

    private boolean loadPrivateMode() {
        return mSharedPreferences.getBoolean(PRIVATE_MODE_KEY, true);
    }

    public void setPrivateMode(boolean enabled) {
        mSharedPreferences.edit().putBoolean(PRIVATE_MODE_KEY, enabled).apply();
    }

}
