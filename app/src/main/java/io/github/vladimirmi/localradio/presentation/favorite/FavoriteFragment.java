package io.github.vladimirmi.localradio.presentation.favorite;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.db.StationContract;
import io.github.vladimirmi.localradio.data.db.ValuesMapper;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;
import io.github.vladimirmi.localradio.presentation.stations.StationsAdapter;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoriteFragment extends BaseFragment<FavoritePresenter>
        implements FavoriteView, LoaderManager.LoaderCallbacks<Cursor>, StationsAdapter.onStationListener {

    public static final int LOADER_ID = 0;
    @BindView(R.id.stationList) RecyclerView stationList;
    @BindView(R.id.placeholder) TextView placeholder;

    private StationsAdapter stationsAdapter;

    @Override
    protected int getLayout() {
        return R.layout.fragment_stations;
    }

    @Override
    protected FavoritePresenter providePresenter() {
        return Scopes.getAppScope().getInstance(FavoritePresenter.class);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //noinspection ConstantConditions
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void setupView(View view) {
        placeholder.setText(R.string.favorites_empty);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        stationList.setLayoutManager(layoutManager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(stationList.getContext(),
                layoutManager.getOrientation());
        stationList.addItemDecoration(itemDecoration);
        stationsAdapter = new StationsAdapter(this);
        stationList.setAdapter(stationsAdapter);
    }

    @Override
    public void selectStation(Station station) {
        stationsAdapter.select(station);
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
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //noinspection ConstantConditions
        return new CursorLoader(getContext(), StationContract.StationEntry.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        List<Station> list = ValuesMapper.getList(data, ValuesMapper::cursorToStation);
        stationsAdapter.submitList(list);
        presenter.listChanged(list);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        stationsAdapter.submitList(Collections.emptyList());
    }

    @Override
    public void onStationClick(Station station) {
        presenter.selectStation(station);
    }
}
