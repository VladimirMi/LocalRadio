package io.github.vladimirmi.localradio.presentation.favorite;

import android.database.Cursor;

import io.github.vladimirmi.localradio.data.db.ValuesMapper;
import io.github.vladimirmi.localradio.presentation.stations.StationsAdapter;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoriteAdapter extends StationsAdapter {

    public FavoriteAdapter(StationsAdapter.onStationListener listener) {
        super(listener);
    }

    public void swapCursor(Cursor cursor) {
        submitList(ValuesMapper.getList(cursor, ValuesMapper::cursorToStation));
    }
}
