package io.github.vladimirmi.localradio.presentation.favorite;

import android.view.Menu;
import android.view.MenuInflater;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.presentation.stations.BaseStationsFragment;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoriteFragment extends BaseStationsFragment<FavoritePresenter> {

    @Override
    protected FavoritePresenter providePresenter() {
        return Scopes.getAppScope().getInstance(FavoritePresenter.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_common, menu);
    }

    @Override
    protected int getPlaceholderIdText() {
        return R.string.placeholder_favorites;
    }

    @Override
    public void onStationClick(Station station) {
        presenter.selectStation(station);
    }
}
