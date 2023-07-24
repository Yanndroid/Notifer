package de.dlyt.yanndroid.notifer.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.dlyt.yanndroid.notifer.R;
import de.dlyt.yanndroid.notifer.utils.Preferences;
import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.utils.internal.ReflectUtils;

public abstract class ListActivity<VH extends ItemDecoration.ViewHolder> extends AppCompatActivity {

    protected Context mContext;
    protected Preferences mPreferences;

    private ToolbarLayout mToolbarLayout;
    private RecyclerView mRecyclerView;
    private TextView mPlaceHolderText;

    private String mHighlightColor;
    protected String mSearchText;

    protected FilterAdapter mListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mContext = this;
        mToolbarLayout = findViewById(R.id.toolbar_layout);
        mRecyclerView = findViewById(R.id.app_list);
        mPlaceHolderText = findViewById(R.id.placeholder_text);

        mPreferences = new Preferences(mContext);

        initHighlightColor();
        initToolbar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            mToolbarLayout.showSearchMode();
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

    protected void highlightText(TextView textView, String text) {
        int index = text.toLowerCase().indexOf(mSearchText.toLowerCase());
        if (index == -1) {
            textView.setText(text);
        } else {
            String match = text.substring(index, index + mSearchText.length());
            textView.setText(Html.fromHtml(text.replace(match, "<b><font color=\"" + mHighlightColor + "\">" + match + "</font></b>"), Html.FROM_HTML_MODE_LEGACY));
        }
    }

    protected void onListSizeChanged(int size) {
        mRecyclerView.setVisibility(size == 0 ? View.GONE : View.VISIBLE);
        mToolbarLayout.setExpandedSubtitle(String.valueOf(size));
    }

    protected void initRecycler() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.addItemDecoration(new ItemDecoration<VH>(mContext));

        mRecyclerView.seslSetFillBottomEnabled(true);
        mRecyclerView.seslSetFastScrollerEnabled(true);
        mRecyclerView.seslSetGoToTopEnabled(true);
        mRecyclerView.seslSetSmoothScrollEnabled(true);
    }

    @SuppressLint("RestrictedApi")
    private void initToolbar() {
        mToolbarLayout.setTitle(getTitle());
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
        mToolbarLayout.getAppBarLayout().addOnOffsetChangedListener((layout, verticalOffset) -> {
            int totalScrollRange = layout.getTotalScrollRange();
            if (totalScrollRange != 0) {
                mPlaceHolderText.setTranslationY(((float) (Math.abs(verticalOffset) - totalScrollRange)) / 2.0f);
            } else {
                totalScrollRange = (int) ReflectUtils.genericInvokeMethod(InputMethodManager.class, getSystemService(INPUT_METHOD_SERVICE), "getInputMethodWindowVisibleHeight");
                mPlaceHolderText.setTranslationY(((float) (Math.abs(verticalOffset) - totalScrollRange)) / 2.0f);
            }
        });
    }

}