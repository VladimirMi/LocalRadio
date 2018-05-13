package io.github.vladimirmi.localradio.presentation.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.transition.Slide;
import android.support.transition.TransitionManager;
import android.support.transition.Visibility;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.about.AboutActivity;
import io.github.vladimirmi.localradio.presentation.core.BaseActivity;
import io.github.vladimirmi.localradio.utils.NonSwipeableViewPager;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {

    public static final int PAGE_FAVORITE = 0;
    public static final int PAGE_STATIONS = 1;
    public static final int PAGE_SEARCH = 2;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.playerControlsFr) View playerControlsFr;

    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected MainPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(MainPresenter.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            showAbout();
            return true;

        } else if (item.getItemId() == R.id.action_exit) {
            exit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupView() {
        setSupportActionBar(toolbar);

        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager.setOffscreenPageLimit(2);
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
    public void showSearch() {
        viewPager.setCurrentItem(PAGE_SEARCH);
    }

    private void showAbout() {
        Intent showAbout = new Intent(this, AboutActivity.class);
        startActivity(showAbout);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void exit() {
        presenter.exit();
        finish();
    }

    @Override
    public void showControls(boolean horizontal) {
        Slide slide = createSlideTransition(horizontal);
        slide.setMode(Visibility.MODE_IN);
        TransitionManager.beginDelayedTransition(((ViewGroup) contentView), slide);
        playerControlsFr.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideControls(boolean horizontal) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Slide slide = createSlideTransition(horizontal);
        slide.setMode(Visibility.MODE_OUT);
        TransitionManager.beginDelayedTransition(((ViewGroup) contentView), slide);
        playerControlsFr.setVisibility(View.GONE);
    }

    @NonNull
    private Slide createSlideTransition(boolean horizontal) {
        Slide slide = new Slide();
        slide.setSlideEdge(horizontal ? Gravity.START : Gravity.BOTTOM);
        slide.setDuration(NonSwipeableViewPager.ANIMATION_DURATION);
        slide.addTarget(playerControlsFr);
        slide.setInterpolator(new FastOutSlowInInterpolator());
        return slide;
    }
}
