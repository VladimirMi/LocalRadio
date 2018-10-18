package io.github.vladimirmi.localradio.presentation.core;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        super.onStop();
        getMapView().onStop();
    }

    @Override
    public void onPause() {
        getMapView().onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        getMapView().onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        getMapView().onLowMemory();
    }
}
