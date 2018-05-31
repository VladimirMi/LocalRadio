package io.github.vladimirmi.localradio.presentation.stations;

import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

import io.github.vladimirmi.localradio.domain.interactors.PlayerControlsInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 26.05.2018.
 */
@SuppressWarnings("WeakerAccess")
public abstract class BaseStationsPresenter extends BasePresenter<StationsView> {

    protected final StationsInteractor stationsInteractor;
    protected final PlayerControlsInteractor controlsInteractor;

    protected BaseStationsPresenter(StationsInteractor stationsInteractor,
                                    PlayerControlsInteractor controlsInteractor) {
        this.stationsInteractor = stationsInteractor;
        this.controlsInteractor = controlsInteractor;
    }

    @Override
    protected void onAttach(StationsView view) {
        viewSubs.add(getStations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<List<Station>>(view) {
                    @Override
                    public void onNext(List<Station> stations) {
                        handleStations(stations);
                    }
                }));

        viewSubs.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Station>(view) {
                    @Override
                    public void onNext(Station station) {
                        view.selectStation(station);
                    }
                }));

        viewSubs.add(controlsInteractor.getPlaybackStateObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<PlaybackStateCompat>(view) {
                    @Override
                    public void onNext(PlaybackStateCompat state) {
                        view.setSelectedPlaying(state.getState() == PlaybackStateCompat.STATE_PLAYING);
                    }
                }));
    }

    protected abstract Observable<List<Station>> getStations();

    protected abstract void handleStations(List<Station> stations);

    public void selectStation(Station station) {
        stationsInteractor.setCurrentStation(station);
    }
}
