package io.github.vladimirmi.localradio.utils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposables;

/**
 * Created by Vladimir Mikhalev 18.07.2018.
 */
public class MapUtils {

    public static Observable<LatLngBounds> observeCameraMove(GoogleMap map) {
        return Observable.create((ObservableOnSubscribe<LatLngBounds>) emitter -> {
            GoogleMap.OnCameraMoveListener listener = () -> {
                if (!emitter.isDisposed()) {
                    emitter.onNext(map.getProjection().getVisibleRegion().latLngBounds);
                }
            };
            map.setOnCameraMoveListener(listener);
            emitter.onNext(map.getProjection().getVisibleRegion().latLngBounds);

            emitter.setDisposable(Disposables.fromRunnable(() -> {
                map.setOnCameraMoveListener(null);
            }));
        }).sample(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread(), true);
    }
}
