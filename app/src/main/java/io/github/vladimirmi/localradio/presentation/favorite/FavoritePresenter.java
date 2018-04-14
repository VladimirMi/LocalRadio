package io.github.vladimirmi.localradio.presentation.favorite;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.domain.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoritePresenter extends BasePresenter<FavoriteView> {

    private final StationsInteractor stationsInteractor;
    private final FavoriteInteractor favoriteInteractor;

    @Inject
    public FavoritePresenter(StationsInteractor stationsInteractor,
                             FavoriteInteractor favoriteInteractor) {
        this.stationsInteractor = stationsInteractor;
        this.favoriteInteractor = favoriteInteractor;
    }

    @Override
    protected void onAttach(FavoriteView view) {
        compDisp.add(stationsInteractor.getCurrentStationObs()
                .doOnNext(station -> Timber.e("station: " + station))
                .buffer(2, 1)
                .doOnNext(station -> Timber.e("buffer: " + station))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObservableObserver<List<Station>>(view) {
                    @Override
                    public void onNext(List<Station> stations) {
                        view.selectStation(stations);
                    }
                }));
    }

    public void listChanged(List<Station> list) {
        favoriteInteractor.initFavorites(list);
    }

    public void selectStation(Station station) {
        compDisp.add(stationsInteractor.setCurrentStation(station)
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }
}
