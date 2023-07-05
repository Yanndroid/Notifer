package de.dlyt.yanndroid.notifer.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatRadioButton;

import de.dlyt.yanndroid.notifer.AppPickerActivity;
import de.dlyt.yanndroid.notifer.R;

public class FilterDialog {

    public interface FilterListener {
        void onApply(AppPickerActivity.Filter user, AppPickerActivity.Filter checked);
    }

    private Context mContext;
    private AlertDialog mDialog;

    public FilterDialog(Context context, AppPickerActivity.Filter user, AppPickerActivity.Filter checked, FilterListener listener) {
        mContext = context;
        View content = LayoutInflater.from(mContext).inflate(R.layout.dialog_filter, null);
        AppCompatRadioButton apps_all = content.findViewById(R.id.filter_apps_all);
        AppCompatRadioButton apps_user = content.findViewById(R.id.filter_apps_user);
        AppCompatRadioButton apps_system = content.findViewById(R.id.filter_apps_system);
        AppCompatRadioButton state_all = content.findViewById(R.id.filter_state_all);
        AppCompatRadioButton state_checked = content.findViewById(R.id.filter_state_checked);
        AppCompatRadioButton state_unchecked = content.findViewById(R.id.filter_state_unchecked);

        mDialog = new AlertDialog.Builder(context)
                .setView(content)
                .setTitle(R.string.filter)
                .setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, null)
                .setPositiveButton(dev.oneuiproject.oneui.design.R.string.oui_common_done, (dialog, which) -> listener.onApply(
                        apps_all.isChecked() ? AppPickerActivity.Filter.ALL : apps_user.isChecked() ? AppPickerActivity.Filter.TRUE : AppPickerActivity.Filter.FALSE,
                        state_all.isChecked() ? AppPickerActivity.Filter.ALL : state_checked.isChecked() ? AppPickerActivity.Filter.TRUE : AppPickerActivity.Filter.FALSE
                ))
                .create();

        switch (checked) {
            case ALL:
                state_all.setChecked(true);
                break;
            case TRUE:
                state_checked.setChecked(true);
                break;
            case FALSE:
                state_unchecked.setChecked(true);
                break;
        }

        switch (user) {
            case ALL:
                apps_all.setChecked(true);
                break;
            case TRUE:
                apps_user.setChecked(true);
                break;
            case FALSE:
                apps_system.setChecked(true);
                break;
        }

    }

    public void show() {
        mDialog.show();
    }

}
