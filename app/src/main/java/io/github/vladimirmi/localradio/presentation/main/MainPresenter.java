package io.github.vladimirmi.localradio.presentation.main;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.domain.MainInteractor;
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
    private final MainInteractor mainInteractor;

    @Inject
    MainPresenter(PlayerControlInteractor controlInteractor,
                  StationsInteractor stationsInteractor,
                  SearchInteractor searchInteractor, MainInteractor mainInteractor) {
        this.controlInteractor = controlInteractor;
        this.stationsInteractor = stationsInteractor;
        this.searchInteractor = searchInteractor;
        this.mainInteractor = mainInteractor;
    }

    @Override
    protected void onFirstAttach(MainView view, CompositeDisposable disposables) {
        initPage(mainInteractor.getPagePosition());

        disposables.add(mainInteractor.initApp()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    if (!mainInteractor.isHaveStations()) selectPage(MainActivity.PAGE_SEARCH);
                })
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }

    @Override
    protected void onAttach(MainView view) {
        controlInteractor.connect();

        disposables.add(stationsInteractor.getCurrentStationObs()
                .map(Station::isNullStation)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean isNull) {
                        handleIsNullStation(isNull);
                    }
                }));

    }

    @Override
    protected void onDetach() {
        controlInteractor.disconnect();
    }

    public void selectPage(int position) {
        mainInteractor.savePagePosition(position);
        if (hasView()) initPage(position);
    }

    public void exit() {
        controlInteractor.stop();
    }

    private void handleIsNullStation(boolean isNull) {
        if (view == null) return;
        if (isNull) {
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
