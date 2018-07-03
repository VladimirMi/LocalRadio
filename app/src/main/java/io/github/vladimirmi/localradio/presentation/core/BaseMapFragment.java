package io.github.vladimirmi.localradio.presentation.core;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by Vladimir Mikhalev 03.07.2018.
 */
public abstract class BaseMapFragment<P extends BasePresenter> extends BaseFragment<P>
        implements OnMapReadyCallback {

    private static final String MAP_BUNDLE_KEY = "MapBundleKey";

    protected abstract MapView getMapView();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = savedInstanceState == null ? null : savedInstanceState.getBundle(MAP_BUNDLE_KEY);
        getMapView().onCreate(bundle);
        getMapView().getMapAsync(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getMapView().onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        getMapView().onStart();
    }

    @Override
    public void onStop() {
        getMapView().onStop();
        super.onStop();
    }

    @Override
    public void onPause() {
        getMapView().onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        getMapView().onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        getMapView().onLowMemory();
        super.onLowMemory();
    }
}
