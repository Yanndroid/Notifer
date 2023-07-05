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
        AppCompatSpinner serverColorFormat = content.findViewById(R.id.server_color_format);

        ArrayAdapter<ColorUtil.ColorFormat> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, ColorUtil.ColorFormat.values());
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        serverColorFormat.setAdapter(adapter);

        if (serverInfo != null) {
            serverName.setText(serverInfo.name);
            serverUrl.setText(serverInfo.url);
            serverColorFormat.setSelection(serverInfo.colorFormat.ordinal());
        }

        mDialog = new AlertDialog.Builder(context)
                .setView(content)
                .setTitle(serverInfo == null ? R.string.server_add : R.string.server_edit)
                .setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, null)
                .setPositiveButton(dev.oneuiproject.oneui.design.R.string.oui_common_done, (dialog, which) -> {
                    if (serverInfo != null) listener.onReplaced(serverInfo);
                    listener.onAdd(new Preferences.ServerInfo(
                            serverName.getText().toString(),
                            serverUrl.getText().toString(),
                            ColorUtil.ColorFormat.values()[serverColorFormat.getSelectedItemPosition()]));
                })
                .create();
    }

    public void show() {
        mDialog.show();
    }

}
