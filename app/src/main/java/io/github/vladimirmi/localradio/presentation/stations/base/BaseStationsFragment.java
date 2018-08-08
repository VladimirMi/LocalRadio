package io.github.vladimirmi.localradio.presentation.stations.base;

import android.graphics.PorterDuff;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;

/**
 * Created by Vladimir Mikhalev 26.05.2018.
 */
public abstract class BaseStationsFragment<P extends BasePresenter> extends BaseFragment<P>
        implements StationsView, StationsAdapter.onStationListener {

    protected @BindView(R.id.stationList) RecyclerView stationList;
    protected @BindView(R.id.placeholder) TextView placeholder;
    protected @BindView(R.id.loadingPb) ProgressBar loadingPb;
    protected @BindView(R.id.goToSearchBt) Button goToSearchBt;

    protected StationsAdapter stationsAdapter;
    protected LinearLayoutManager layoutManager;

    @Override
    protected int getLayout() {
        return R.layout.fragment_stations;
    }

    @Override
    protected void setupView(View view) {
        layoutManager = new LinearLayoutManager(getContext());
        stationList.setLayoutManager(layoutManager);
        stationsAdapter = new StationsAdapter(this);
        stationList.setAdapter(stationsAdapter);

        loadingPb.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        placeholder.setText(getPlaceholderIdText());
    }

    protected abstract int getPlaceholderIdText();

    //region =============== StationsView ==============

    @Override
    public void setStations(List<Station> stations) {
        stationsAdapter.setData(stations);
        scrollToSelectedStation();
    }

    @Override
    public void setFavorites(Set<Integer> ids) {
        stationsAdapter.setFavorites(ids);
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
        placeholder.setVisibility(View.GONE);
        goToSearchBt.setVisibility(View.GONE);
    }

    @Override
    public void setSearching(boolean isSearching) {
        loadingPb.setVisibility(isSearching ? View.VISIBLE : View.GONE);
    }

    //endregion

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
