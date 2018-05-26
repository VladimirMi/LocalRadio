package io.github.vladimirmi.localradio.presentation.stations;

import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

import io.github.vladimirmi.localradio.domain.interactors.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 26.05.2018.
 */
public abstract class BaseStationsPresenter extends BasePresenter<StationsView> {

    protected final StationsInteractor stationsInteractor;
    protected final PlayerControlInteractor controlInteractor;

    protected BaseStationsPresenter(StationsInteractor stationsInteractor,
                                    PlayerControlInteractor controlInteractor) {
        this.stationsInteractor = stationsInteractor;
        this.controlInteractor = controlInteractor;
    }

    @Override
    protected void onAttach(StationsView view, boolean isFirstAttach) {
        disposables.add(getStations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<List<Station>>(view) {
                    @Override
                    public void onNext(List<Station> stations) {
                        view.setStations(stations);
                        decideShowPlaceholder(stations);
                    }
                }));

        disposables.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Station>(view) {
                    @Override
                    public void onNext(Station station) {
                        view.selectStation(station);
                    }
                }));

        disposables.add(controlInteractor.getPlaybackStateObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<PlaybackStateCompat>(view) {
                    @Override
                    public void onNext(PlaybackStateCompat state) {
                        view.setSelectedPlaying(state.getState() == PlaybackStateCompat.STATE_PLAYING);
                    }
                }));
    }

    protected abstract Observable<List<Station>> getStations();

    public void selectStation(Station station) {
        stationsInteractor.setCurrentStation(station);
    }

    private void decideShowPlaceholder(List<Station> stations) {
        if (stations.size() == 0) {
            view.showPlaceholder();
        } else {
            view.hidePlaceholder();
        }
    }
}
