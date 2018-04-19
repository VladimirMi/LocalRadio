package io.github.vladimirmi.localradio.presentation.stations;

import android.support.v4.media.session.PlaybackStateCompat;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.domain.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsPresenter extends BasePresenter<StationsView> {

    private final StationsInteractor stationsInteractor;
    private final PlayerControlInteractor controlInteractor;

    @Inject
    public StationsPresenter(StationsInteractor stationsInteractor, PlayerControlInteractor controlInteractor) {
        this.stationsInteractor = stationsInteractor;
        this.controlInteractor = controlInteractor;
    }

    @Override
    protected void onAttach(StationsView view) {
        compDisp.add(stationsInteractor.getStationsObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::setStations));

        compDisp.add(stationsInteractor.getCurrentStationObs()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObservableObserver<Station>(view) {
                    @Override
                    public void onNext(Station station) {
                        view.selectStation(station);
                    }
                }));

        compDisp.add(controlInteractor.getPlaybackStateObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObservableObserver<PlaybackStateCompat>(view) {
                    @Override
                    public void onNext(PlaybackStateCompat state) {
                        view.setSelectedPlaying(state.getState() == PlaybackStateCompat.STATE_PLAYING);
                    }
                }));
    }


    public void selectStation(Station station) {
        compDisp.add(stationsInteractor.setCurrentStation(station)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }

    public void filterStations(String filter) {
        stationsInteractor.filterStations(filter);
    }

    public String getFilter() {
        return stationsInteractor.getFilter();
    }
}
