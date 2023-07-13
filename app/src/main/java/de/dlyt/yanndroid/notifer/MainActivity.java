package de.dlyt.yanndroid.notifer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.dlyt.yanndroid.notifer.utils.ColorUtil;
import de.dlyt.yanndroid.notifer.utils.HttpRequest;
import de.dlyt.yanndroid.notifer.utils.PlayUpdater;
import de.dlyt.yanndroid.notifer.utils.Preferences;
import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.preference.SwitchBarPreference;
import dev.oneuiproject.oneui.preference.TipsCardPreference;
import dev.oneuiproject.oneui.preference.internal.PreferenceRelatedCard;
import dev.oneuiproject.oneui.utils.ActivityUtils;
import dev.oneuiproject.oneui.utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity {

    private static final String SETTINGS_ACTION = "com.android.settings.action.IA_SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);

        if (SETTINGS_ACTION.equals(getIntent().getAction()))
            toolbarLayout.setNavigationButtonAsBack();

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private Context mContext;
        private TipsCardPreference mTipCard;
        private PreferenceRelatedCard mRelativeLinkCard;
        private SwitchBarPreference mSwitchBarPreference;

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            mContext = context;
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String str) {
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            initPreferences();
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getView().setBackgroundColor(mContext.getColor(dev.oneuiproject.oneui.design.R.color.oui_background_color));
            getListView().seslSetLastRoundedCorner(false);
        }

        @Override
        public void onResume() {
            setRelativeLinkCard();
            super.onResume();
            boolean hasNotificationAccess = hasNotificationAccess();
            if (hasNotificationAccess) removeTipCard();
            mSwitchBarPreference.setEnabled(hasNotificationAccess);
        }

        private void initPreferences() {
            mSwitchBarPreference = findPreference(getString(R.string.preference_service_enabled_key));

            mTipCard = findPreference(getString(R.string.preference_noti_access_tip_key));
            mTipCard.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                return false;
            });
            mTipCard.setOnCancelClickListener(v -> removeTipCard());

            Preference test_servers = findPreference(getString(R.string.preference_test_key));
            test_servers.setOnPreferenceClickListener(preference -> {
                sendTestNotification();
                return true;
            });

            Preference about_app = findPreference(getString(R.string.preference_about_app_key));
            about_app.setOnPreferenceClickListener(preference -> {
                ActivityUtils.startPopOverActivity(mContext,
                        new Intent(mContext, AboutActivity.class),
                        null,
                        ActivityUtils.POP_OVER_POSITION_BOTTOM | ActivityUtils.POP_OVER_POSITION_CENTER_HORIZONTAL);
                return false;
            });

            new PlayUpdater(mContext).checkUpdate(new PlayUpdater.Callback() {
                @Override
                public void updateAvailable(AppUpdateInfo appUpdateInfo) {
                    about_app.setWidgetLayoutResource(R.layout.sesl_preference_badge);
                }
            });
        }

        private boolean hasNotificationAccess() {
            String enabledNotificationListeners = Settings.Secure.getString(mContext.getContentResolver(), "enabled_notification_listeners");
            return enabledNotificationListeners != null && enabledNotificationListeners.contains(mContext.getPackageName());
        }

        private void sendTestNotification() {
            try {
                List<Preferences.ServerInfo> mServers = new Preferences(mContext).getServers(null);

                JSONObject body = HttpRequest.makeBody(
                        getString(R.string.app_name), mContext.getPackageName(), 0,
                        System.currentTimeMillis(), false, "", false,
                        "Test Notification", "This is a test notification", "", "",
                        "", false, 0, 0);

                for (Preferences.ServerInfo mServer : mServers) {
                    body.put("color", ColorUtil.convertColor(Color.BLUE, mServer.colorFormat));
                    HttpRequest.post(mServer.url, body);
                }
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        private void removeTipCard() {
            mTipCard.setVisible(false);
            findPreference(getString(R.string.preference_tip_space_key)).setVisible(false);
        }

        private void setRelativeLinkCard() {
            if (mRelativeLinkCard == null) {
                mRelativeLinkCard = PreferenceUtils.createRelatedCard(mContext);
                mRelativeLinkCard.addButton(getString(R.string.main_notifications), v -> startActivity(new Intent("android.settings.NOTIFICATION_SETTINGS")))
                        .addButton(getString(R.string.main_notification_access), v -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")))
                        .addButton(getString(R.string.main_sounds_and_vibration), v -> startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS)))
                        .show(this);
            }
        }

    }
}