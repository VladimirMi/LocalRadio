package io.github.vladimirmi.localradio.presentation.playercontrol;

import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public interface PlayerControlView extends BaseView {

    void setStation(Station station);

    void setFavorite(boolean isFavorite);

    void setMetadata(String string);

    void showPlaying();

    void showStopped();

    void showLoading();
}
