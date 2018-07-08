package io.github.vladimirmi.localradio.presentation.stations;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.transition.Slide;
import android.support.transition.TransitionManager;
import android.support.transition.Visibility;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

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
    @BindView(R.id.playerControlsFr) View playerControlsFr;

    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    protected int getLayout() {
        return R.layout.fragment_stations_pager;
    }

    @Override
    protected StationsPagerPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(StationsPagerPresenter.class);
    }

    @Override
    protected void setupView(View view) {
        StationsPagerAdapter pagerAdapter = new StationsPagerAdapter(getChildFragmentManager());

        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                presenter.selectPage(tab.getPosition());
            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(playerControlsFr);
    }

    @Override
    public void showFavorite() {
        viewPager.setCurrentItem(PAGE_FAVORITE);
    }

    @Override
    public void showStations() {
        viewPager.setCurrentItem(PAGE_STATIONS);
    }

    @Override
    public void showControls() {
        Slide slide = createSlideTransition();
        slide.setMode(Visibility.MODE_IN);
        //noinspection ConstantConditions
        TransitionManager.beginDelayedTransition((ViewGroup) getView(), slide);
        playerControlsFr.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideControls() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Slide slide = createSlideTransition();
        slide.setMode(Visibility.MODE_OUT);
        //noinspection ConstantConditions
        TransitionManager.beginDelayedTransition((ViewGroup) getView(), slide);
        playerControlsFr.setVisibility(View.GONE);
    }

    @NonNull
    private Slide createSlideTransition() {
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.BOTTOM);
        slide.setDuration(200);
        slide.addTarget(playerControlsFr);
        slide.setInterpolator(new FastOutSlowInInterpolator());
        return slide;
    }
}
