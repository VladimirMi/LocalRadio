package io.github.vladimirmi.localradio.presentation.main;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.domain.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.SearchInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private final PlayerControlInteractor controlInteractor;
    private final StationsInteractor stationsInteractor;
    private final SearchInteractor searchInteractor;
    //// TODO: 4/26/18 move preferences through interactor
    private final Preferences preferences;

    @Inject
    MainPresenter(PlayerControlInteractor controlInteractor,
                  StationsInteractor stationsInteractor,
                  SearchInteractor searchInteractor, Preferences preferences) {
        this.controlInteractor = controlInteractor;
        this.stationsInteractor = stationsInteractor;
        this.searchInteractor = searchInteractor;
        this.preferences = preferences;
    }

    @Override
    protected void onFirstAttach(@Nullable MainView view, CompositeDisposable disposables) {
        initPage(preferences.page.get());

        disposables.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Station>(view) {
                    @Override
                    public void onNext(Station station) {
                        handleCurrentStation(station);
                    }
                }));

        // TODO: 4/26/18 show progress bar
        if (searchInteractor.isCanSearch()) {
            disposables.add(searchInteractor.searchStations()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new RxUtils.ErrorCompletableObserver(view) {
                        @Override
                        public void onComplete() {
                            // TODO: 4/26/18 hide progress bar
                        }
                    }));
        } else {
            selectPage(MainActivity.PAGE_SEARCH);
        }
    }

    @Override
    protected void onAttach(@NonNull MainView view) {
        controlInteractor.connect();
    }

    @Override
    protected void onDetach() {
        controlInteractor.disconnect();
    }

    public void selectPage(int position) {
        preferences.page.put(position);
        initPage(position);
    }

    private void handleCurrentStation(Station station) {
        if (view == null) return;
        if (station.isNullStation()) {
            view.hideControls();
        } else {
            view.showControls();
        }
    }

    private void initPage(int position) {
        switch (position) {
            case MainActivity.PAGE_FAVORITE:
                view.showFavorite();
                break;
            case MainActivity.PAGE_STATIONS:
                view.showStations();
                break;
            case MainActivity.PAGE_SEARCH:
                view.showSearch();
                break;
        }
    }
}
