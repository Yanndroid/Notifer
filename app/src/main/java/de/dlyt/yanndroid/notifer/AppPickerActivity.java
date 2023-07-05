package de.dlyt.yanndroid.notifer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.palette.graphics.Palette;
import androidx.picker3.app.SeslColorPickerDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.dlyt.yanndroid.notifer.dialog.FilterDialog;
import de.dlyt.yanndroid.notifer.recyclerview.FilterAdapter;
import de.dlyt.yanndroid.notifer.recyclerview.ItemDecoration;
import de.dlyt.yanndroid.notifer.utils.Preferences;
import dev.oneuiproject.oneui.dialog.ProgressDialog;
import dev.oneuiproject.oneui.layout.ToolbarLayout;

public class AppPickerActivity extends AppCompatActivity {

    public enum Filter {ALL, TRUE, FALSE}

    private Context mContext;
    private Preferences mPreferences;
    private PackageManager mPackageManager;

    private ToolbarLayout mToolbarLayout;
    private RecyclerView mRecyclerView;
    private AppsAdapter mListAdapter;

    private List<AppInfo> mAppList = new ArrayList<>();
    private HashMap<String, Integer> mEnabledPackages = new HashMap<>();

    private String mHighlightColor;
    private String mSearchText;
    private Filter mFilterUser = Filter.TRUE;
    private Filter mFilterChecked = Filter.ALL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mContext = this;
        mToolbarLayout = findViewById(R.id.toolbar_layout);
        mRecyclerView = findViewById(R.id.app_list);

        mPreferences = new Preferences(mContext);
        mPackageManager = getPackageManager();

        initHighlightColor();
        initToolbar();
        loadApps();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreferences.setEnabledPackages(mEnabledPackages);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.app_picker_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                mToolbarLayout.showSearchMode();
                return true;
            case R.id.action_filter:
                showFilterDialog();
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
        mToolbarLayout.setTitle(getString(R.string.preference_apps_title));
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

    private void loadApps() {
        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.picker_loading_apps));
        progressDialog.setButton(
                ProgressDialog.BUTTON_NEGATIVE,
                getString(dev.oneuiproject.oneui.design.R.string.oui_common_cancel),
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        progressDialog.show();

        Handler handler = new Handler();
        new Thread(() -> {
            mEnabledPackages = mPreferences.getEnabledPackages(null);

            List<PackageInfo> installedPackages = mPackageManager.getInstalledPackages(0);
            int total = installedPackages.size();
            handler.post(() -> progressDialog.setMax(total));

            for (int i = 0; i < total; i++) {
                mAppList.add(new AppInfo(mContext, installedPackages.get(i)));

                int progress = i;
                handler.post(() -> progressDialog.setProgress(progress));
            }

            handler.post(() -> {
                progressDialog.dismiss();
                initRecycler();
            });
        }).start();
    }

    private void initRecycler() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mListAdapter = new AppsAdapter(mContext, mAppList));
        mRecyclerView.addItemDecoration(new ItemDecoration<AppsAdapter.ViewHolder>(mContext));

        mRecyclerView.seslSetFillBottomEnabled(true);
        mRecyclerView.seslSetFastScrollerEnabled(true);
        mRecyclerView.seslSetGoToTopEnabled(true);
        mRecyclerView.seslSetSmoothScrollEnabled(true);
    }

    private void showFilterDialog() {
        new FilterDialog(mContext, mFilterUser, mFilterChecked, (user, checked) -> {
            mFilterUser = user;
            mFilterChecked = checked;
            mListAdapter.applyFilter();
        }).show();
    }

    public class AppInfo {

        public final String label, packageName;
        public final Drawable icon;
        public final boolean userApp;
        public boolean checked;
        public int color, def_color = 0;
        public int[] iconColors = new int[0];

        public AppInfo(Context context, PackageInfo packageInfo) {
            PackageManager packageManager = context.getPackageManager();
            this.packageName = packageInfo.packageName;
            this.label = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
            this.icon = packageManager.getApplicationIcon(packageInfo.applicationInfo);
            this.checked = mEnabledPackages.containsKey(this.packageName);
            this.userApp = ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);

            if (this.checked) this.color = mEnabledPackages.get(this.packageName);
        }

        public int getDefaultColor(Context context) {
            if (this.def_color != 0) return this.def_color;
            return this.def_color = loadColorPalette(context);
        }

        private int loadColorPalette(Context context) {
            try {
                Drawable appIconDrawable = context.getPackageManager().getApplicationIcon(this.packageName);
                Bitmap appIconBitmap = Bitmap.createBitmap(appIconDrawable.getIntrinsicWidth(), appIconDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(appIconBitmap);
                appIconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                appIconDrawable.draw(canvas);
                Palette palette = Palette.from(appIconBitmap).generate();
                List<Palette.Swatch> swatches = palette.getSwatches();
                this.iconColors = new int[swatches.size()];
                for (int i = 0; i < swatches.size(); i++) {
                    this.iconColors[i] = swatches.get(i).getRgb();
                }
                return palette.getDominantColor(0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    public class AppsAdapter extends FilterAdapter<AppInfo, AppsAdapter.ViewHolder> {

        private final Context mContext;

        public AppsAdapter(Context context, List<AppInfo> items) {
            super(AppInfo.class, items);
            mContext = context;
        }

        @Override
        public int compareItem(AppInfo item1, AppInfo item2) {
            return item1.label.compareToIgnoreCase(item2.label);
        }

        @Override
        public boolean isSameItemContent(AppInfo item1, AppInfo item2) {
            return item1.label.equals(item2.label);
        }

        @Override
        public boolean filterCondition(AppInfo item) {
            boolean searchEmpty = mSearchText == null || mSearchText.isEmpty();

            boolean labelContainsSearch = searchEmpty || item.label.toLowerCase().contains(mSearchText.toLowerCase());
            boolean packageContainsSearch = searchEmpty || item.packageName.toLowerCase().contains(mSearchText.toLowerCase());
            boolean userFilter = mFilterUser == Filter.ALL || (mFilterUser == Filter.TRUE) == item.userApp;
            boolean checkedFilter = mFilterChecked == Filter.ALL || (mFilterChecked == Filter.TRUE) == item.checked;

            return (labelContainsSearch || packageContainsSearch) && userFilter && checkedFilter;
        }

        @Override
        public void onListSizeChanged(int size) {
            mRecyclerView.setVisibility(size == 0 ? View.GONE : View.VISIBLE);
            mToolbarLayout.setExpandedSubtitle(String.valueOf(size));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_app_picker, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            final AppInfo appInfo = getItem(position);

            setListItemText(holder, appInfo);
            holder.appIcon.setImageDrawable(appInfo.icon);

            holder.appSwitch.setChecked(appInfo.checked);
            initColorPicker(holder, appInfo);

            holder.itemView.setOnClickListener(v -> holder.appSwitch.toggle());
            holder.appSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                appInfo.checked = isChecked;

                if (isChecked) {
                    mEnabledPackages.put(appInfo.packageName, appInfo.color = appInfo.getDefaultColor(mContext));
                } else {
                    mEnabledPackages.remove(appInfo.packageName);
                }

                initColorPicker(holder, appInfo);
            });
        }

        private void setListItemText(ViewHolder holder, AppInfo appInfo) {
            if (holder.appTitle == null || holder.appPackage == null) return;
            if (mSearchText != null && !mSearchText.isEmpty()) {
                highlightSearch(holder.appTitle, appInfo.label);
                highlightSearch(holder.appPackage, appInfo.packageName);
            } else {
                holder.appTitle.setText(appInfo.label);
                holder.appPackage.setText(appInfo.packageName);
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

        private void initColorPicker(ViewHolder holder, AppInfo appInfo) {
            if (appInfo.checked) {
                if (appInfo.color == 0) appInfo.color = appInfo.getDefaultColor(mContext);
                holder.appColor.setVisibility(View.VISIBLE);

                GradientDrawable drawable = (GradientDrawable) mContext.getDrawable(dev.oneuiproject.oneui.design.R.drawable.oui_preference_color_picker_preview).mutate();
                drawable.setColor(appInfo.color);
                holder.appColor.setImageDrawable(drawable);

                holder.appColor.setOnClickListener(v -> {
                    appInfo.getDefaultColor(mContext);
                    SeslColorPickerDialog dialog = new SeslColorPickerDialog(mContext, color -> {
                        mEnabledPackages.put(appInfo.packageName, color);
                        appInfo.color = color;
                        GradientDrawable drawable1 = (GradientDrawable) mContext.getDrawable(dev.oneuiproject.oneui.design.R.drawable.oui_preference_color_picker_preview).mutate();
                        drawable1.setColor(color);
                        holder.appColor.setImageDrawable(drawable1);
                    }, appInfo.color, appInfo.iconColors, false);
                    dialog.show();
                });
            } else {
                holder.appColor.setVisibility(View.GONE);
                holder.appColor.setOnClickListener(null);
            }
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            super.onViewRecycled(holder);
            if (holder.appSwitch != null) holder.appSwitch.setOnCheckedChangeListener(null);
            if (holder.appColor != null) holder.appColor.setOnClickListener(null);
        }

        public class ViewHolder extends ItemDecoration.ViewHolder {
            public ImageView appIcon, appColor;
            public TextView appTitle, appPackage;
            public SwitchCompat appSwitch;

            ViewHolder(View itemView) {
                super(itemView);

                appIcon = itemView.findViewById(R.id.list_item_app_icon);
                appColor = itemView.findViewById(R.id.list_item_app_color);
                appTitle = itemView.findViewById(R.id.list_item_app_label);
                appPackage = itemView.findViewById(R.id.list_item_app_package);
                appSwitch = itemView.findViewById(R.id.list_item_app_switch);
            }

            @Override
            public boolean isSeparator() {
                return false;
            }
        }
    }


}