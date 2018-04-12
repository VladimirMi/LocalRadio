package io.github.vladimirmi.localradio.presentation.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import io.github.vladimirmi.localradio.presentation.favorite.FavoriteFragment;
import io.github.vladimirmi.localradio.presentation.search.SearchFragment;
import io.github.vladimirmi.localradio.presentation.stations.StationsFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FavoriteFragment();
            case 1:
                return new StationsFragment();
            case 2:
                return new SearchFragment();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
