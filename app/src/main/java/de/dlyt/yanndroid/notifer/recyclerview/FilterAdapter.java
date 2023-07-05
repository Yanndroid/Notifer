package de.dlyt.yanndroid.notifer.recyclerview;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import java.util.List;

public abstract class FilterAdapter<I, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final List<I> mItemList;
    private final SortedList<I> mSortedList;

    private final SortedList.Callback<I> mSortedListCallback = new SortedList.Callback<I>() {
        @Override
        public int compare(I o1, I o2) {
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            return compareItem(o1, o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(I oldItem, I newItem) {
            return isSameItemContent(oldItem, newItem);
        }

        @Override
        public boolean areItemsTheSame(I item1, I item2) {
            return item1.equals(item2);
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
            onListSizeChanged(getItemCount());
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
            onListSizeChanged(getItemCount());
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }
    };

    public abstract void onListSizeChanged(int size);

    public abstract int compareItem(I item1, I item2);

    public abstract boolean isSameItemContent(I item1, I item2);

    public abstract boolean filterCondition(I item);

    public FilterAdapter(Class<I> itemClass, List<I> items) {
        mItemList = items;
        mSortedList = new SortedList<>(itemClass, mSortedListCallback);
        applyFilter();
    }

    public void add(I item) {
        mItemList.add(item);
        if (filterCondition(item))
            mSortedList.add(item);
    }

    public void remove(I item) {
        mItemList.remove(item);
        mSortedList.remove(item);
    }

    public void applyFilter() {
        mSortedList.beginBatchedUpdates();
        for (I item : mItemList) {
            if (filterCondition(item)) {
                mSortedList.add(item);
                notifyItemChanged(mSortedList.indexOf(item));
            } else {
                mSortedList.remove(item);
            }
        }
        mSortedList.endBatchedUpdates();
        onListSizeChanged(getItemCount());
    }

    public I getItem(int position) {
        return mSortedList.get(position);
    }

    @Override
    public int getItemCount() {
        return mSortedList.size();
    }
}
