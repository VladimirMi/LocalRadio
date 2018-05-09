package io.github.vladimirmi.localradio.presentation.stations;

import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.domain.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.SearchInteractor;
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
    private final SearchInteractor searchInteractor;

    @Inject
    StationsPresenter(StationsInteractor stationsInteractor,
                      PlayerControlInteractor controlInteractor,
                      SearchInteractor searchInteractor) {
        this.stationsInteractor = stationsInteractor;
        this.controlInteractor = controlInteractor;
        this.searchInteractor = searchInteractor;
    }

    @Override
    protected void onAttach(StationsView view) {
        disposables.add(stationsInteractor.getStationsObs()
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

        disposables.add(searchInteractor.isSearching()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean isSearching) {
                        view.setSearching(isSearching);
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

    private void decideShowPlaceholder(List<Station> stations) {
        if (stations.size() == 0) {
            view.showPlaceholder();
        } else {
            view.hidePlaceholder();
        }
    }
}
