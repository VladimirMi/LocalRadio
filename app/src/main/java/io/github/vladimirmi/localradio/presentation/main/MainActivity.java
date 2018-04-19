package io.github.vladimirmi.localradio.presentation.main;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseActivity;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.playerControlsFr) View playerControlsFr;

    private boolean controlsVisible;

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected MainPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(MainPresenter.class);
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
    }

    @Override
    public void showFavorite() {
        viewPager.setCurrentItem(0);
    }

    @Override
    public void showStations() {
        viewPager.setCurrentItem(1);
        if (controlsVisible) playerControlsFr.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSearch() {
        viewPager.setCurrentItem(2);
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
        if (viewPager.getCurrentItem() != 2) {
            playerControlsFr.setVisibility(View.VISIBLE);
        }
    }
}
