package io.github.vladimirmi.localradio.presentation.main;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.MainInteractor;
import io.github.vladimirmi.localradio.domain.interactors.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private final PlayerControlInteractor controlInteractor;
    private final StationsInteractor stationsInteractor;
    private final MainInteractor mainInteractor;

    @Inject
    MainPresenter(PlayerControlInteractor controlInteractor,
                  StationsInteractor stationsInteractor,
                  MainInteractor mainInteractor) {
        this.controlInteractor = controlInteractor;
        this.stationsInteractor = stationsInteractor;
        this.mainInteractor = mainInteractor;
    }

    @Override
    protected void onFirstAttach(MainView view) {
        controlInteractor.connect();
        initPage(mainInteractor.getPagePosition());

        disposables.add(mainInteractor.initApp()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    if (!mainInteractor.isHaveStations()) selectPage(MainActivity.PAGE_SEARCH);
                })
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }

    @Override
    protected void onAttach(MainView view, boolean isFirstAttach) {
        disposables.add(stationsInteractor.getCurrentStationObs()
                .map(station -> station.isNullObject)
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
    protected void onDestroy() {
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
            view.hideControls(false);
        } else if (!mainInteractor.isSearchPage()) {
            view.showControls(false);
        }
    }

    private void initPage(int position) {
        switch (position) {
            case MainActivity.PAGE_FAVORITE:
                view.showFavorite();
                if (!stationsInteractor.getCurrentStation().isNullObject) {
                    view.showControls(true);
                }
                break;
            case MainActivity.PAGE_STATIONS:
                view.showStations();
                if (!stationsInteractor.getCurrentStation().isNullObject) {
                    view.showControls(true);
                }
                break;
            case MainActivity.PAGE_SEARCH:
                view.showSearch();
                view.hideControls(true);
                break;
        }
    }
}
