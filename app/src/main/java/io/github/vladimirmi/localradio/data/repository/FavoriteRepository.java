package io.github.vladimirmi.localradio.data.repository;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.StationContract;
import io.github.vladimirmi.localradio.data.db.ValuesMapper;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.reactivex.Completable;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoriteRepository {

    private final ContentResolver contentResolver;

    @Inject
    public FavoriteRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public Completable addFavorite(Station station) {
        Uri uri = StationContract.StationEntry.CONTENT_URI;
        return Completable.fromAction(() -> contentResolver.insert(uri, ValuesMapper.createValue(station)));
    }

    public Completable removeFavorite(Station station) {
        Uri uri = ContentUris.withAppendedId(StationContract.StationEntry.CONTENT_URI, station.getId());
        return Completable.fromAction(() -> contentResolver.delete(uri, null, null));
    }
}
