package io.github.vladimirmi.localradio.presentation.stations;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsFragment extends BaseFragment<StationsPresenter>
        implements StationsView, StationsAdapter.onStationListener, SearchView.OnQueryTextListener {

    private StationsAdapter stationsAdapter;
    private SearchView searchView;

    @Override
    protected int getLayout() {
        return R.layout.fragment_stations;
    }

    @Override
    protected StationsPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(StationsPresenter.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        RecyclerView stationList = (RecyclerView) view;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        stationList.setLayoutManager(layoutManager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(stationList.getContext(),
                layoutManager.getOrientation());
        stationList.addItemDecoration(itemDecoration);
        stationsAdapter = new StationsAdapter(this);
        stationList.setAdapter(stationsAdapter);
    }

    @Override
    public void setStations(List<Station> stations) {
        stationsAdapter.submitList(stations);
    }

    @Override
    public void selectStation(Station station) {
        int stationPosition = stationsAdapter.select(station);
        if (stationPosition >= 0) {
            ((RecyclerView) getView()).scrollToPosition(stationPosition);
        }
    }

    @Override
    public void setSelectedPlaying(boolean playing) {
        stationsAdapter.setPlaying(playing);
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
}
