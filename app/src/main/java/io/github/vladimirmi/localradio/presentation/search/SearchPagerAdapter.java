package io.github.vladimirmi.localradio.presentation.search;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import io.github.vladimirmi.localradio.presentation.search.manual.SearchManualFragment;
import io.github.vladimirmi.localradio.presentation.search.map.SearchMapFragment;

/**
 * Created by Vladimir Mikhalev 01.07.2018.
 */
@SuppressWarnings("WeakerAccess")
public class SearchPagerAdapter extends FragmentPagerAdapter {

    private final String[] pagerTitles;

    public SearchPagerAdapter(FragmentManager fm, String[] pagerTitles) {
        super(fm);
        this.pagerTitles = pagerTitles;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new SearchMapFragment();
            case 1:
                return new SearchManualFragment();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public int getCount() {
        return pagerTitles.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pagerTitles[position];
    }
}
