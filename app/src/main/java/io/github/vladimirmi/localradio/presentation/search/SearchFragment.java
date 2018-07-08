package io.github.vladimirmi.localradio.presentation.search;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;

/**
 * Created by Vladimir Mikhalev 01.07.2018.
 */
public class SearchFragment extends BaseFragment<SearchPresenter> implements SearchView {

    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.loadingPb) ProgressBar loadingPb;
    @BindView(R.id.searchBt) FloatingActionButton searchBt;

    @Override
    protected int getLayout() {
        return R.layout.fragment_search;
    }

    @Override
    protected SearchPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(SearchPresenter.class);
    }

    @Override
    protected void setupView(View view) {
        String[] pagerTitles = getResources().getStringArray(R.array.search_pager);
        SearchPagerAdapter adapter = new SearchPagerAdapter(getChildFragmentManager(), pagerTitles);
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                presenter.setSearchMode(position);
            }
        });
    }

    //region =============== SearchView ==============

    @Override
    public void setSearchMode(int mode) {
        viewPager.setCurrentItem(mode);
    }

    //endregion
}
