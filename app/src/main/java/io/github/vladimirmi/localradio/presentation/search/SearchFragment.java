package io.github.vladimirmi.localradio.presentation.search;

import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;
import io.github.vladimirmi.localradio.presentation.main.MainView;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 01.07.2018.
 */
public class SearchFragment extends BaseFragment<SearchPresenter> implements SearchView {

    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.loadingPb) ProgressBar loadingPb;
    @BindView(R.id.searchBt) FloatingActionButton searchBt;
    @BindView(R.id.resultBt) MaterialButton resultBt;

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

        searchBt.setOnClickListener((v) -> presenter.search());

        resultBt.setOnClickListener((v) -> handleBackPress());
    }

    @Override
    public boolean handleBackPress() {
        //noinspection ConstantConditions
        ((MainView) getActivity()).showStations();
        return true;
    }

    //region =============== SearchView ==============

    @Override
    public void setSearchMode(int mode) {
        viewPager.setCurrentItem(mode);
    }

    @Override
    public void showLoading(boolean show) {
        Timber.e("showLoading: " + show);
        if (show) {
            resultBt.setVisibility(View.GONE);
            loadingPb.setVisibility(View.VISIBLE);
        } else {
            resultBt.setVisibility(View.VISIBLE);
            loadingPb.setVisibility(View.GONE);
        }
    }

    @Override
    public void setSearchResult(int stations) {
        String s = getResources().getQuantityString(R.plurals.search_result, stations, stations);
        resultBt.setText(s);
    }

    //endregion
}
