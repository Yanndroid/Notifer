package de.dlyt.yanndroid.notifer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.notifer.dialog.ServerEditDialog;
import de.dlyt.yanndroid.notifer.recyclerview.FilterAdapter;
import de.dlyt.yanndroid.notifer.recyclerview.ItemDecoration;
import de.dlyt.yanndroid.notifer.recyclerview.ListActivity;
import de.dlyt.yanndroid.notifer.utils.Preferences;
import dev.oneuiproject.oneui.utils.DialogUtils;

public class ServerActivity extends ListActivity<ServerActivity.ServerAdapter.ViewHolder> {

    private List<Preferences.ServerInfo> mServerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle(R.string.preference_servers_title);
        super.onCreate(savedInstanceState);

        mServerList = mPreferences.getServers(null);
        mListAdapter = new ServerAdapter(mServerList);
        initRecycler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreferences.setServers(mServerList);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.server_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new) {
            showEditServerDialog(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditServerDialog(Preferences.ServerInfo serverInfo) {
        new ServerEditDialog(mContext, serverInfo, new ServerEditDialog.ServerEditListener() {
            @Override
            public void onAdd(Preferences.ServerInfo serverInfo) {
                mListAdapter.add(serverInfo);
            }

            @Override
            public void onReplaced(Preferences.ServerInfo oldServer) {
                mListAdapter.remove(oldServer);
            }
        }).show();
    }

    public class ServerAdapter extends FilterAdapter<Preferences.ServerInfo, ServerAdapter.ViewHolder> {

        public ServerAdapter(List<Preferences.ServerInfo> items) {
            super(Preferences.ServerInfo.class, items);
        }

        @Override
        public boolean isSameItemContent(Preferences.ServerInfo item1, Preferences.ServerInfo item2) {
            return item1.name.equals(item2.name);
        }

        @Override
        public boolean filterCondition(Preferences.ServerInfo item) {
            boolean searchEmpty = mSearchText == null || mSearchText.isEmpty();

            boolean labelContainsSearch = searchEmpty || item.name.toLowerCase().contains(mSearchText.toLowerCase());
            boolean packageContainsSearch = searchEmpty || item.url.toLowerCase().contains(mSearchText.toLowerCase());

            return labelContainsSearch || packageContainsSearch;
        }

        @Override
        public void onListSizeChanged(int size) {
            ServerActivity.this.onListSizeChanged(size);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_server, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            final Preferences.ServerInfo serverInfo = getItem(position);

            setListItemText(holder, serverInfo);

            holder.itemView.setOnClickListener(v -> showEditServerDialog(serverInfo));
            holder.delete.setOnClickListener(v -> {
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle(R.string.server_delete)
                        .setMessage(serverInfo.name)
                        .setPositiveButton(R.string.delete, (dialog1, which) -> remove(serverInfo))
                        .setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, null)
                        .show();
                DialogUtils.setDialogButtonTextColor(dialog, AlertDialog.BUTTON_POSITIVE, getColor(dev.oneuiproject.oneui.design.R.color.oui_functional_red_color));
            });

        }

        private void setListItemText(ViewHolder holder, Preferences.ServerInfo serverInfo) {
            if (holder.serverName == null || holder.serverUrl == null) return;
            if (mSearchText != null && !mSearchText.isEmpty()) {
                highlightText(holder.serverName, serverInfo.name);
                highlightText(holder.serverUrl, serverInfo.url);
            } else {
                holder.serverName.setText(serverInfo.name);
                holder.serverUrl.setText(serverInfo.url);
            }
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            super.onViewRecycled(holder);
            if (holder.delete != null) holder.delete.setOnClickListener(null);
        }

        public class ViewHolder extends ItemDecoration.ViewHolder {
            public TextView serverName, serverUrl;
            public ImageView delete;


            ViewHolder(View itemView) {
                super(itemView);

                serverName = itemView.findViewById(R.id.list_item_server_name);
                serverUrl = itemView.findViewById(R.id.list_item_server_url);
                delete = itemView.findViewById(R.id.list_item_server_delete);
            }

            @Override
            public boolean isSeparator() {
                return false;
            }
        }
    }


}