package io.github.vladimirmi.localradio.presentation.main;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.transition.Slide;
import android.support.transition.TransitionManager;
import android.support.transition.Visibility;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.about.AboutActivity;
import io.github.vladimirmi.localradio.presentation.core.BaseActivity;
import io.github.vladimirmi.localradio.presentation.search.SearchFragment;
import io.github.vladimirmi.localradio.presentation.stations.StationsPagerFragment;
import timber.log.Timber;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {

    @BindView(R.id.toolbar) Toolbar toolbar;
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
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                recreate();
            }
        }
        super.onCreate(savedInstanceState);
        showStations();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO: 6/2/18 make with popup
        if (menu instanceof MenuBuilder) {
            try {
                Field f = menu.getClass().getDeclaredField("mOptionalIconsVisible");
                f.setAccessible(true);
                f.setBoolean(menu, true);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            showAbout();
            return true;

        } else if (item.getItemId() == R.id.action_search) {
            showSearch();
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

        bottomSheetBehavior = BottomSheetBehavior.from(playerControlsFr);
    }

    @Override
    public void showStations() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, new StationsPagerFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showSearch() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, new SearchFragment())
                .addToBackStack(null)
                .commit();
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
        // TODO: 6/30/18 remake
        Slide slide = new Slide();
        slide.setSlideEdge(horizontal ? Gravity.START : Gravity.BOTTOM);
        slide.setDuration(200);
        slide.addTarget(playerControlsFr);
        slide.setInterpolator(new FastOutSlowInInterpolator());
        return slide;
    }
}
