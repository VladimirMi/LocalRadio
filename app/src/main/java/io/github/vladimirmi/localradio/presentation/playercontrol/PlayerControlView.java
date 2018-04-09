package io.github.vladimirmi.localradio.presentation.playercontrol;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public interface PlayerControlView extends BaseView {

    void setStation(Station station);

    void setMetadata(String string);

    void setMetadata(int stringId);

    void showPlaying();

    void showStopped();
}
