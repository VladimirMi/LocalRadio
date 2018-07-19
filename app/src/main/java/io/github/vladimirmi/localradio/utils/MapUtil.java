package io.github.vladimirmi.localradio.utils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Function;

/**
 * Created by Vladimir Mikhalev 18.07.2018.
 */
public class MapUtil {

    private static final double LOAD = 0.5;
    private static final double THRESHOLD = 0.3;

    private GoogleMap map;
    private LatLng target;
    private VisibleRegion visible;
    private double thresholdTop;
    private double thresholdBottom;
    private double thresholdRight;
    private double thresholdLeft;

    public void init(GoogleMap map) {
        this.map = map;
        target = map.getCameraPosition().target;
        visible = map.getProjection().getVisibleRegion();
        calculateBounds();

        observeCameraMove(googleMap -> googleMap.getCameraPosition().zoom)
                .distinctUntilChanged();

    }

    private void calculateBounds() {
        double top = visible.farLeft.latitude;
        double bottom = visible.nearLeft.latitude;
        double right = visible.nearRight.longitude;
        double left = visible.nearLeft.longitude;

        double visibleHeight = Math.abs(top - bottom);
        double visibleWidth;
        if (left > right) {
            visibleWidth = 360 + right - left;
        } else {
            visibleWidth = right - left;
        }

        thresholdTop = Math.min(90.0, top + visibleHeight * THRESHOLD);
        thresholdBottom = Math.max(-90.0, bottom - visibleHeight * THRESHOLD);
        thresholdRight = (right + visibleWidth + 180.0) % 360.0;
        thresholdLeft = 180 + (left - visibleWidth - 180.0) % 360.0;
    }

    private <T> Observable<T> observeCameraMove(Function<GoogleMap, T> fun) {
        return Observable.create((ObservableOnSubscribe<T>) emitter -> {
            GoogleMap.OnCameraMoveListener listener = () -> {
                try {
                    if (!emitter.isDisposed()) emitter.onNext(fun.apply(map));
                } catch (Exception e) {
                    emitter.tryOnError(e);
                }
            };
            map.setOnCameraMoveListener(listener);
            emitter.onNext(fun.apply(map));

            emitter.setDisposable(Disposables.fromRunnable(() -> {
                map.setOnCameraMoveListener(null);
            }));
        }).sample(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread(), true);
    }
}
