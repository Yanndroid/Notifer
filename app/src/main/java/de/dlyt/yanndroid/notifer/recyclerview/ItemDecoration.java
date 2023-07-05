package de.dlyt.yanndroid.notifer.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.util.SeslRoundedCorner;
import androidx.appcompat.util.SeslSubheaderRoundedCorner;
import androidx.recyclerview.widget.RecyclerView;

public class ItemDecoration<VH extends ItemDecoration.ViewHolder> extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;
    private final SeslSubheaderRoundedCorner mRoundedCorner;

    public ItemDecoration(@NonNull Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.isLightTheme, outValue, true);

        mDivider = context.getDrawable(outValue.data == 0
                ? androidx.appcompat.R.drawable.sesl_list_divider_dark
                : androidx.appcompat.R.drawable.sesl_list_divider_light);

        mRoundedCorner = new SeslSubheaderRoundedCorner(context);
        mRoundedCorner.setRoundedCorners(SeslRoundedCorner.ROUNDED_CORNER_ALL);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent,
                       @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            VH holder = (VH) parent.getChildViewHolder(child);
            if (!holder.isSeparator()) {
                final int top = child.getBottom()
                        + ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;
                final int bottom = mDivider.getIntrinsicHeight() + top;

                mDivider.setBounds(parent.getLeft(), top, parent.getRight(), bottom);
                mDivider.draw(c);
            }
        }
    }

    @Override
    public void seslOnDispatchDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            VH holder = (VH) parent.getChildViewHolder(child);
            if (holder.isSeparator()) {
                mRoundedCorner.drawRoundedCorner(child, c);
            }
        }
    }

    public abstract static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract boolean isSeparator();
    }
}
