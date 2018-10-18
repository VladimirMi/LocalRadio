package io.github.vladimirmi.localradio.presentation.stations;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import io.github.vladimirmi.localradio.presentation.stations.favorites.FavoriteFragment;
import io.github.vladimirmi.localradio.presentation.stations.stations.StationsFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class StationsPagerAdapter extends FragmentPagerAdapter {

    public StationsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FavoriteFragment();
            case 1:
                return new StationsFragment();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
