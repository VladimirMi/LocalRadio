package io.github.vladimirmi.localradio.presentation.core;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;

/**
 * Created by Vladimir Mikhalev 03.07.2018.
 */
public abstract class BaseMapFragment<P extends BasePresenter> extends BaseFragment<P>
        implements OnMapReadyCallback {

    @BindView(R.id.mapView) MapView mapView;

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mapView.getMapAsync(this);
        super.onViewCreated(view, savedInstanceState);
    }
}
