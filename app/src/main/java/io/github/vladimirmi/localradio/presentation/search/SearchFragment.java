package io.github.vladimirmi.localradio.presentation.search;

import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.LocationsModule;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;
import io.github.vladimirmi.localradio.presentation.main.MainView;
import toothpick.Scope;
import toothpick.Toothpick;

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
        Scope scope = Scopes.getLocationsScope();
        scope.installModules(new LocationsModule());
        return scope.getInstance(SearchPresenter.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
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

        loadingPb.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public boolean handleBackPress() {
        if (!super.handleBackPress()) {
            Toothpick.closeScope(Scopes.LOCATIONS_SCOPE);
            //noinspection ConstantConditions
            ((MainView) getActivity()).showStations();
        }
        return true;
    }

    //region =============== SearchView ==============

    @Override
    public void setSearchMode(int mode) {
        viewPager.setCurrentItem(mode);
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            resultBt.setVisibility(View.GONE);
            loadingPb.setVisibility(View.VISIBLE);
        } else {
            new Handler().postDelayed(() -> {
                loadingPb.setVisibility(View.GONE);
            }, 1000);
        }
    }

    @Override
    public void setSearchResult(int stations) {
        String s = getResources().getQuantityString(R.plurals.search_result, stations, stations);
        new Handler().postDelayed(() -> {
            resultBt.setVisibility(View.VISIBLE);
            resultBt.setText(s);
        }, 1000);
    }

    //endregion
}
