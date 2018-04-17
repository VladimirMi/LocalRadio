package io.github.vladimirmi.localradio.data.repository;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.StationContract;
import io.github.vladimirmi.localradio.data.db.ValuesMapper;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.reactivex.Completable;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoriteRepository {

    private final ContentResolver contentResolver;
    private final Preferences preferences;

    @Inject
    public FavoriteRepository(ContentResolver contentResolver,
                              Preferences preferences) {
        this.contentResolver = contentResolver;
        this.preferences = preferences;
    }

    public Completable addFavorite(Station station) {
        Uri uri = StationContract.StationEntry.CONTENT_URI;
        return Completable.fromAction(() -> contentResolver.insert(uri, ValuesMapper.createValue(station)));
    }

    public Completable removeFavorite(Station station) {
        Uri uri = ContentUris.withAppendedId(StationContract.StationEntry.CONTENT_URI, station.getId());
        return Completable.fromAction(() -> contentResolver.delete(uri, null, null));
    }

    public Station findCurrentFavoriteStation(List<Station> stations) {
        int curId = preferences.currentStation.get();
        for (Station station : stations) {
            if (station.getId() == curId) {
                return station;
            }
        }
        return null;
    }
}
