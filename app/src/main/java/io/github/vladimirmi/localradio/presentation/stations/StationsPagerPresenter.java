package io.github.vladimirmi.localradio.presentation.stations;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.MainInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 30.06.2018.
 */
@SuppressWarnings("WeakerAccess")
public class StationsPagerPresenter extends BasePresenter<StationsPagerView> {

    private final MainInteractor mainInteractor;
    private final StationsInteractor stationsInteractor;

    @Inject
    public StationsPagerPresenter(MainInteractor mainInteractor, StationsInteractor stationsInteractor) {
        this.mainInteractor = mainInteractor;
        this.stationsInteractor = stationsInteractor;
    }

    @Override
    protected void onFirstAttach(StationsPagerView view, CompositeDisposable disposables) {
        initPage(mainInteractor.getPagePosition());
    }

    @Override
    protected void onAttach(StationsPagerView view) {
        viewSubs.add(stationsInteractor.getCurrentStationObs()
                .map(station -> station.isNullObject)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean isNull) {
                        if (isNull) view.hideControls(false);
                        else view.showControls();
                    }
                }));
    }

    public void selectPage(int position) {
        mainInteractor.savePagePosition(position);
        if (hasView()) initPage(position);
    }

    private void initPage(int position) {
        switch (position) {
            case StationsPagerFragment.PAGE_FAVORITE:
                view.showFavorite();
                break;
            case StationsPagerFragment.PAGE_STATIONS:
                view.showStations();
                break;
        }
    }
}
