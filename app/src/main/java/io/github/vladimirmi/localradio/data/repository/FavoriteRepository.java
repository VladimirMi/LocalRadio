package io.github.vladimirmi.localradio.data.repository;

import android.content.ContentResolver;
import android.content.ContentUris;
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

    public Station findCurrentFavoriteStation() {
        int curId = preferences.currentStationId.get();
        for (Station station : favoriteStations) {
            if (station.getId() == curId) {
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

    // TODO: 4/26/18 move logic to favorite interactor
    public boolean updateStationsIfFavorite(List<Station> stations) {
        boolean updated = false;
        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            boolean isFavorite = false;
            for (Station favoriteStation : getFavoriteStations()) {
                if (station.getId() == favoriteStation.getId()) {
                    isFavorite = true;
                    break;
                }
            }
            if (station.isFavorite() != isFavorite) {
                stations.set(i, station.copy(isFavorite));
                updated = true;
            }
        }
        return updated;
    }
}
