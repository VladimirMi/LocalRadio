package io.github.vladimirmi.localradio.presentation.stations;

import java.util.List;

import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public interface StationsView extends BaseView {

    void setStations(List<Station> stations);

    void selectStation(Station station);

    void setSelectedPlaying(boolean playing);

    void showPlaceholder();

    void hidePlaceholder();

    void setSearching(boolean isSearching);
}
