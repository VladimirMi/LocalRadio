package io.github.vladimirmi.localradio.presentation.main;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.MainInteractor;
import io.github.vladimirmi.localradio.domain.interactors.PlayerControlsInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private final PlayerControlsInteractor controlInteractor;
    private final StationsInteractor stationsInteractor;
    private final MainInteractor mainInteractor;

    @Inject
    MainPresenter(PlayerControlsInteractor controlInteractor,
                  StationsInteractor stationsInteractor,
                  MainInteractor mainInteractor) {
        this.controlInteractor = controlInteractor;
        this.stationsInteractor = stationsInteractor;
        this.mainInteractor = mainInteractor;
    }

    @Override
    protected void onFirstAttach(MainView view, CompositeDisposable dataSubs) {
        controlInteractor.connect();

        dataSubs.add(mainInteractor.initApp()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }

    @Override
    protected void onAttach(MainView view) {

        viewSubs.add(stationsInteractor.getCurrentStationObs()
                .map(station -> station.isNullObject)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean isNull) {
                        if (isNull) view.hideControls();
                    }
                }));
    }

    @Override
    protected void onDestroy() {
        controlInteractor.disconnect();
    }

    public void exit() {
        controlInteractor.stop();
    }
}
