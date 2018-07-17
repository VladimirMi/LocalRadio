package io.github.vladimirmi.localradio.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vladimir Mikhalev 05.04.2018.
 */

@SuppressWarnings("unused")
public class CustomArrayAdapter<T> extends ArrayAdapter<T> {

    private List<T> data;
    private OnFilteringListener<T> onFilteringListener;

    private ListFilter listFilter = new ListFilter();
    private List<T> originalValues;
    private final Object lock = new Object();

    public CustomArrayAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        data = objects;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public T getItem(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    public void setOnFilteringListener(OnFilteringListener<T> onFilteringListener) {
        this.onFilteringListener = onFilteringListener;
    }

    public void setData(List<T> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public T findItem(String text) {
        for (T t : data) {
            if (t.toString().equals(text)) return t;
        }
        return null;
    }

    public interface OnFilteringListener<T> {

        void onFiltering(List<T> filteredData);
    }

    private class ListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            final FilterResults results = new FilterResults();

            if (originalValues == null) {
                synchronized (lock) {
                    originalValues = new ArrayList<>(data);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                final ArrayList<T> list;
                synchronized (lock) {
                    list = new ArrayList<>(originalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                final String prefixString = prefix.toString().toLowerCase();

                final ArrayList<T> values;
                synchronized (lock) {
                    values = new ArrayList<>(originalValues);
                }

                final ArrayList<T> newValues = new ArrayList<>();

                for (T value : values) {
                    final String valueText = value.toString().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        for (String word : words) {
                            if (word.startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            data = (List<T>) results.values;
            if (onFilteringListener != null) {
                onFilteringListener.onFiltering(data);
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
