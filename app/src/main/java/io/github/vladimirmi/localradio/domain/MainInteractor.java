package io.github.vladimirmi.localradio.domain;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.preferences.Preferences;

/**
 * Created by Vladimir Mikhalev 28.04.2018.
 */
public class MainInteractor {

    private final Preferences preferences;

    @Inject
    public MainInteractor(Preferences preferences) {
        this.preferences = preferences;
    }

    public int getPagePosition() {
        return preferences.pagePosition.get();
    }

    public void savePagePosition(int position) {
        preferences.pagePosition.put(position);
    }

    public boolean isFavoritePage() {
        return getPagePosition() == 0;
    }
}
