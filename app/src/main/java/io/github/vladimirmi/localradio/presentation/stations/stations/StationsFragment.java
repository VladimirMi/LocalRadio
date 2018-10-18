package io.github.vladimirmi.localradio.presentation.stations.stations;

import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.presentation.main.MainView;
import io.github.vladimirmi.localradio.presentation.stations.base.BaseStationsFragment;
import io.github.vladimirmi.localradio.utils.UiUtils;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsFragment extends BaseStationsFragment<StationsPresenter>
        implements SearchView.OnQueryTextListener {

    @BindView(R.id.goToSearchBt) Button goToSearchBt;
    private SearchView searchView;

    @Override
    protected StationsPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(StationsPresenter.class);
    }

    @Override
    protected int getPlaceholderIdText() {
        return R.string.placeholder_search_result;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stations, menu);

        searchView = (SearchView) menu.findItem(R.id.action_filter).getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getString(R.string.filter));
        String filter = presenter.getFilter();
        if (!filter.isEmpty()) {
            searchView.setQuery(filter, false);
            searchView.setIconified(false);
            searchView.clearFocus();
        }
    }

    @Override
    protected void setupView(View view) {
        super.setupView(view);
        //noinspection ConstantConditions
        goToSearchBt.setOnClickListener((v) -> ((MainView) getActivity()).showSearch());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        ImageView v = searchView.findViewById(androidx.appcompat.R.id.search_button);
        v.setImageResource(R.drawable.ic_filter);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean handleBackPress() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && getView() != null) {
            //noinspection ConstantConditions
            UiUtils.hideSoftKeyBoard(getContext(), getView().getWindowToken());
        }
    }

    private boolean isFirst = true;

    @Override
    public void setStations(List<Station> stations) {
        super.setStations(stations);
        if (isFirst && stations.size() > 0) {
            stationList.scheduleLayoutAnimation();
            isFirst = false;
        }
    }

    @Override
    public void onStationClick(Station station) {
        presenter.selectStation(station);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        presenter.filterStations(newText);
        return true;
    }

    @Override
    public void showPlaceholder() {
        super.showPlaceholder();
        goToSearchBt.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidePlaceholder() {
        super.hidePlaceholder();
        goToSearchBt.setVisibility(View.GONE);
    }
}

