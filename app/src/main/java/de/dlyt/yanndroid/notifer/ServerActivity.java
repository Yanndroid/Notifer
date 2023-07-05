package de.dlyt.yanndroid.notifer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.notifer.dialog.ServerEditDialog;
import de.dlyt.yanndroid.notifer.recyclerview.FilterAdapter;
import de.dlyt.yanndroid.notifer.recyclerview.ItemDecoration;
import de.dlyt.yanndroid.notifer.utils.Preferences;
import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.utils.DialogUtils;

public class ServerActivity extends AppCompatActivity {

    private Context mContext;
    private Preferences mPreferences;

    private ToolbarLayout mToolbarLayout;
    private RecyclerView mRecyclerView;
    private ServerAdapter mListAdapter;

    private List<Preferences.ServerInfo> mServerList = new ArrayList<>();

    private String mHighlightColor;
    private String mSearchText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mContext = this;
        mToolbarLayout = findViewById(R.id.toolbar_layout);
        mRecyclerView = findViewById(R.id.app_list);

        mPreferences = new Preferences(mContext);

        mServerList = mPreferences.getServers(null);

        initHighlightColor();
        initToolbar();
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
        switch (item.getItemId()) {
            case R.id.action_search:
                mToolbarLayout.showSearchMode();
                return true;
            case R.id.action_new:
                showEditServerDialog(null);
                return true;
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mToolbarLayout.onSearchModeVoiceInputResult(intent);
    }

    private void initHighlightColor() {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, value, true);
        mHighlightColor = "#" + Integer.toHexString(Color.red(value.data)) + Integer.toHexString(Color.green(value.data)) + Integer.toHexString(Color.blue(value.data));
    }

    private void initToolbar() {
        mToolbarLayout.setTitle(getString(R.string.preference_servers_title));
        mToolbarLayout.setNavigationButtonAsBack();
        mToolbarLayout.setSearchModeListener(new ToolbarLayout.SearchModeListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mListAdapter != null) {
                    mSearchText = newText;
                    mListAdapter.applyFilter();
                }
                return true;
            }

            @Override
            public void onSearchModeToggle(SearchView searchView, boolean visible) {

            }
        });
    }

    private void initRecycler() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mListAdapter = new ServerAdapter(mServerList));
        mRecyclerView.addItemDecoration(new ItemDecoration<ServerAdapter.ViewHolder>(mContext));

        mRecyclerView.seslSetFillBottomEnabled(true);
        mRecyclerView.seslSetFastScrollerEnabled(true);
        mRecyclerView.seslSetGoToTopEnabled(true);
        mRecyclerView.seslSetSmoothScrollEnabled(true);
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
        public int compareItem(Preferences.ServerInfo item1, Preferences.ServerInfo item2) {
            return item1.name.compareToIgnoreCase(item2.name);
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
            mRecyclerView.setVisibility(size == 0 ? View.GONE : View.VISIBLE);
            mToolbarLayout.setExpandedSubtitle(String.valueOf(size));
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
                highlightSearch(holder.serverName, serverInfo.name);
                highlightSearch(holder.serverUrl, serverInfo.url);
            } else {
                holder.serverName.setText(serverInfo.name);
                holder.serverUrl.setText(serverInfo.url);
            }
        }

        private void highlightSearch(TextView textView, String text) {
            int index = text.toLowerCase().indexOf(mSearchText.toLowerCase());
            if (index == -1) {
                textView.setText(text);
            } else {
                String match = text.substring(index, index + mSearchText.length());
                textView.setText(Html.fromHtml(text.replace(match, "<b><font color=\"" + mHighlightColor + "\">" + match + "</font></b>"), Html.FROM_HTML_MODE_LEGACY));
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