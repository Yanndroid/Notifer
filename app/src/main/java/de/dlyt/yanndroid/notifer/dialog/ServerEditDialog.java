package de.dlyt.yanndroid.notifer.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;

import de.dlyt.yanndroid.notifer.R;
import de.dlyt.yanndroid.notifer.utils.ColorUtil;
import de.dlyt.yanndroid.notifer.utils.Preferences;

public class ServerEditDialog {

    public interface ServerEditListener {
        void onAdd(Preferences.ServerInfo serverInfo);

        void onReplaced(Preferences.ServerInfo oldServer);
    }

    private Context mContext;
    private AlertDialog mDialog;

    public ServerEditDialog(Context context, Preferences.ServerInfo serverInfo, ServerEditListener listener) {
        mContext = context;
        View content = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_server, null);
        AppCompatEditText serverName = content.findViewById(R.id.server_name);
        AppCompatEditText serverUrl = content.findViewById(R.id.server_url);

        if (serverInfo != null) {
            serverName.setText(serverInfo.name);
            serverUrl.setText(serverInfo.url);
        }

        mDialog = new AlertDialog.Builder(context)
                .setView(content)
                .setTitle(serverInfo == null ? R.string.server_add : R.string.server_edit)
                .setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, null)
                .setPositiveButton(dev.oneuiproject.oneui.design.R.string.oui_common_done, (dialog, which) -> {
                    if (serverInfo != null) listener.onReplaced(serverInfo);
                    listener.onAdd(new Preferences.ServerInfo(
                            serverName.getText().toString(),
                            serverUrl.getText().toString()));
                })
                .create();
    }

    public void show() {
        mDialog.show();
    }

}
