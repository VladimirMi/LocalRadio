package io.github.vladimirmi.localradio.domain.models;

/**
 * Created by Vladimir Mikhalev 31.05.2018.
 */

public enum SearchState {
    NOT_DONE, LOADING, AUTO_DONE, MANUAL_DONE;

    public boolean isSearchDone() {
        return this == AUTO_DONE || this == MANUAL_DONE;
    }
}
