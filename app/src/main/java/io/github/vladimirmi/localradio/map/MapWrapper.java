package io.github.vladimirmi.localradio.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposables;

/**
 * Created by Vladimir Mikhalev 24.07.2018.
 */
public class MapWrapper {

    private GoogleMap map;
    private GoogleMap.OnCameraMoveListener cameraMoveListener;

    private final Object emit = new Object();
    private Observable<Object> cameraMoveObservable = Observable.create(emitter -> {
        cameraMoveListener = () -> {
            try {
                if (!emitter.isDisposed()) emitter.onNext(emit);
            } catch (Exception e) {
                emitter.tryOnError(e);
            }
        };
        if (isMapReady()) getMap().setOnCameraMoveListener(cameraMoveListener);
        emitter.onNext(emit);

        emitter.setDisposable(Disposables.fromRunnable(() -> {
            cameraMoveListener = null;
            if (isMapReady()) getMap().setOnCameraMoveListener(null);
        }));
    }).sample(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread(), true)
            .unsubscribeOn(AndroidSchedulers.mainThread())
            .share();


    public GoogleMap getMap() {
        if (!isMapReady()) throw new IllegalStateException("Map not ready");
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
        configureMap();
        if (cameraMoveListener != null) {
            map.setOnCameraMoveListener(cameraMoveListener);
        }
    }

    public boolean isMapReady() {
        return map != null;
    }

    public Observable<Object> getCameraMoveObservable() {
        return cameraMoveObservable;
    }

    private void configureMap() {
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
    }
}
