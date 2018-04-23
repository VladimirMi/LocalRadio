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
        layoutManager = new LinearLayoutManager(getContext());
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

        int firstPosition = layoutManager.findFirstVisibleItemPosition();
        int lastPosition = layoutManager.findLastVisibleItemPosition();
        int offset = (lastPosition - firstPosition) / 4;

        if (stationPosition > firstPosition + offset && stationPosition < lastPosition - offset) {
            return;
        }

        if (firstPosition == -1 && lastPosition == -1 && getView() != null) {
            //workaround on not scrolled when initialized
            getView().postDelayed(() -> layoutManager
                    .scrollToPositionWithOffset(stationPosition, getView().getHeight() / 2), 10);
        } else if (stationPosition + offset > lastPosition) {
            layoutManager.scrollToPosition(stationPosition + offset);
        } else {
            layoutManager.scrollToPosition(stationPosition - offset);
        }
    }
}
