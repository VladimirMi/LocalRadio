package io.github.vladimirmi.localradio.data.repository;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;

import java.util.Collections;
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

    private List<Station> favoriteStations = Collections.emptyList();

    @SuppressWarnings("WeakerAccess")
    @Inject
    public FavoriteRepository(ContentResolver contentResolver,
                              Preferences preferences) {
        this.contentResolver = contentResolver;
        this.preferences = preferences;
    }

    public Completable initFavorites() {
        Uri uri = StationContract.StationEntry.CONTENT_URI;
        return Completable.fromAction(() -> {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            favoriteStations = ValuesMapper.getList(cursor, ValuesMapper::cursorToStation);
        });
    }

    public Completable addFavorite(Station station) {
        Uri uri = StationContract.StationEntry.CONTENT_URI;
        return Completable.fromAction(() -> contentResolver.insert(uri, ValuesMapper.createValue(station)));
    }

    public Completable removeFavorite(Station station) {
        Uri uri = ContentUris.withAppendedId(StationContract.StationEntry.CONTENT_URI, station.getId());
        return Completable.fromAction(() -> contentResolver.delete(uri, null, null));
    }

    public Station findCurrentFavoriteStation() {
        int currentId = preferences.currentStationId.get();
        // return first if current station is null object
        if (currentId == 0 && !favoriteStations.isEmpty()) return favoriteStations.get(0);
        for (Station station : favoriteStations) {
            if (station.getId() == currentId) {
                return station;
            }
        }
        return null;
    }

    public void setFavoriteStations(List<Station> favoriteStations) {
        this.favoriteStations = favoriteStations;
    }

    public List<Station> getFavoriteStations() {
        return favoriteStations;
    }
}
