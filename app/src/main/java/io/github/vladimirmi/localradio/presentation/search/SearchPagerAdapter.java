package io.github.vladimirmi.localradio.presentation.search;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import io.github.vladimirmi.localradio.presentation.search.manual.SearchManualFragment;

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
                return new SearchManualFragment();
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
