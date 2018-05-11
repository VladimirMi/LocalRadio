package io.github.vladimirmi.localradio.presentation.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseActivity;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {

    public static final int PAGE_FAVORITE = 0;
    public static final int PAGE_STATIONS = 1;
    public static final int PAGE_SEARCH = 2;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.playerControlsFr) View playerControlsFr;

    private boolean controlsVisible;
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
        tryShowControls();
    }

    @Override
    public void showStations() {
        viewPager.setCurrentItem(PAGE_STATIONS);
        tryShowControls();
    }

    @Override
    public void showSearch() {
        viewPager.setCurrentItem(PAGE_SEARCH);
        playerControlsFr.setVisibility(View.GONE);
    }

    @Override
    public void hideControls() {
        controlsVisible = false;
        playerControlsFr.setVisibility(View.GONE);
    }

    @Override
    public void showControls() {
        controlsVisible = true;
        if (viewPager.getCurrentItem() != PAGE_SEARCH) {
            tryShowControls();
        }
    }

    private void showAbout() {
        // TODO: 5/11/18 implement
    }

    private void exit() {
        presenter.exit();
        finish();
    }

    private void tryShowControls() {
        if (controlsVisible) {
            playerControlsFr.setVisibility(View.VISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }
}
