package io.github.vladimirmi.localradio.presentation.stations;

import android.graphics.PorterDuff;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsFragment extends BaseFragment<StationsPresenter>
        implements StationsView, StationsAdapter.onStationListener, SearchView.OnQueryTextListener {

    @BindView(R.id.stationList) RecyclerView stationList;
    @BindView(R.id.placeholder) TextView placeholder;
    @BindView(R.id.loadingPb) ProgressBar loadingPb;

    private StationsAdapter stationsAdapter;
    private SearchView searchView;
    private LinearLayoutManager layoutManager;

    @Override
    protected int getLayout() {
        return R.layout.fragment_stations;
    }

    @Override
    protected StationsPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(StationsPresenter.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stations, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
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
    public void onPrepareOptionsMenu(Menu menu) {
        ImageView v = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        v.setImageResource(R.drawable.ic_filter);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void setupView(View view) {
        layoutManager = new LinearLayoutManager(getContext());
        stationList.setLayoutManager(layoutManager);
        stationsAdapter = new StationsAdapter(this);
        stationList.setAdapter(stationsAdapter);

        loadingPb.getIndeterminateDrawable().mutate().setColorFilter(getResources()
                .getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
    }

    //region =============== StationsView ==============

    @Override
    public void setStations(List<Station> stations) {
        stationsAdapter.submitList(stations);
        scrollToSelectedStation();
    }

    @Override
    public void selectStation(Station station) {
        stationsAdapter.select(station);
        scrollToSelectedStation();
    }

    @Override
    public void setSelectedPlaying(boolean playing) {
        stationsAdapter.setPlaying(playing);
    }

    @Override
    public void showPlaceholder() {
        placeholder.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidePlaceholder() {
        Timber.e("hidePlaceholder: ");
        placeholder.setVisibility(View.GONE);
    }

    @Override
    public void setSearching(boolean isSearching) {
        Timber.e("setSearching: " + isSearching);
        loadingPb.setVisibility(isSearching ? View.VISIBLE : View.GONE);
    }

    //endregion

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

    private void scrollToSelectedStation() {
        int stationPosition = stationsAdapter.getSelectedPosition();
        if (stationPosition < 0) return;

        int firstPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastPosition = layoutManager.findLastCompletelyVisibleItemPosition();

        if (firstPosition < stationPosition && stationPosition < lastPosition) {
            return;
        }

        if (firstPosition == -1 && lastPosition == -1 && getView() != null) {
            layoutManager.scrollToPositionWithOffset(stationPosition, stationList.getHeight() / 3);

        } else {
            layoutManager.scrollToPosition(stationPosition);
        }
    }
}
