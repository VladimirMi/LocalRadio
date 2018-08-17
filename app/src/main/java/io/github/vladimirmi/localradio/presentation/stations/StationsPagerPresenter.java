package io.github.vladimirmi.localradio.presentation.stations;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.MainInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 30.06.2018.
 */
@SuppressWarnings("WeakerAccess")
public class StationsPagerPresenter extends BasePresenter<StationsPagerView> {

    private final MainInteractor mainInteractor;

    @Inject
    public StationsPagerPresenter(MainInteractor mainInteractor) {
        this.mainInteractor = mainInteractor;
    }

    @Override
    protected void onFirstAttach(StationsPagerView view, CompositeDisposable disposables) {
        initPage(mainInteractor.getPagePosition());
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
