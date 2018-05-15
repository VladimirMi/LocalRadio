package io.github.vladimirmi.localradio.presentation.main;

import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public interface MainView extends BaseView {

    void showFavorite();

    void showStations();

    void showSearch();

    void showControls(boolean horizontal);

    void hideControls(boolean horizontal);
}
