package io.github.vladimirmi.localradio.presentation.stations;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;

/**
 * Created by Vladimir Mikhalev 30.06.2018.
 */
public class StationsPagerFragment extends BaseFragment<StationsPagerPresenter> implements StationsPagerView {

    public static final int PAGE_FAVORITE = 0;
    public static final int PAGE_STATIONS = 1;

    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.viewPager) ViewPager viewPager;

    @Override
    protected int getLayout() {
        return R.layout.fragment_stations_pager;
    }

    @Override
    protected StationsPagerPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(StationsPagerPresenter.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void setupView(View view) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        StationsPagerAdapter pagerAdapter = new StationsPagerAdapter(getChildFragmentManager());

        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                presenter.selectPage(tab.getPosition());
            }
        });
    }

    @Override
    public void showFavorite() {
        viewPager.setCurrentItem(PAGE_FAVORITE);
    }

    @Override
    public void showStations() {
        viewPager.setCurrentItem(PAGE_STATIONS);
    }
}
