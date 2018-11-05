package io.github.vladimirmi.localradio.presentation.stations;

import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 30.06.2018.
 */
public interface StationsPagerView extends BaseView {

    void showFavorite();

    void showStations();

    void hideControls(boolean forbidShow);

    void showControls();
}
