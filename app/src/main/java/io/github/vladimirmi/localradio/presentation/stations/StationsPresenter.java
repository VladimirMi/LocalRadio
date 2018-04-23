package io.github.vladimirmi.localradio.presentation.stations;

import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.domain.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsPresenter extends BasePresenter<StationsView> {

    private final StationsInteractor stationsInteractor;
    private final PlayerControlInteractor controlInteractor;

    @Inject
    StationsPresenter(StationsInteractor stationsInteractor, PlayerControlInteractor controlInteractor) {
        this.stationsInteractor = stationsInteractor;
        this.controlInteractor = controlInteractor;
    }

    @Override
    protected void onFirstAttach(@Nullable StationsView view, CompositeDisposable disposables) {
        disposables.add(stationsInteractor.getStationsObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<List<Station>>(view) {
                    @Override
                    public void onNext(List<Station> stations) {
                        if (view != null) view.setStations(stations);
                    }
                }));

        disposables.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Station>(view) {
                    @Override
                    public void onNext(Station station) {
                        if (view != null) view.selectStation(station);
                    }
                }));

        disposables.add(controlInteractor.getPlaybackStateObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<PlaybackStateCompat>(view) {
                    @Override
                    public void onNext(PlaybackStateCompat state) {
                        if (view != null) {
                            view.setSelectedPlaying(state.getState() == PlaybackStateCompat.STATE_PLAYING);
                        }
                    }
                }));
    }

    public void selectStation(Station station) {
        stationsInteractor.setCurrentStation(station);
    }

    public void filterStations(String filter) {
        stationsInteractor.filterStations(filter);
    }

    public String getFilter() {
        return stationsInteractor.getFilter();
    }
}
