package io.github.vladimirmi.localradio.presentation.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.Visibility;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

    @BindView(R.id.root) CoordinatorLayout root;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.playerControlsFr) View playerControlsFr;

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private boolean forbidShowControls = true;

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

        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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
//        enableToolbarScroll(true);
        forbidShowControls = false;
        showControls();
        toolbar.setTitle(R.string.app_name);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.contentContainer, new StationsPagerFragment())
                .commit();
    }

    @Override
    public void showSearch() {
//        enableToolbarScroll(false);
        forbidShowControls = true;
        hideControls();
        toolbar.setTitle(R.string.search);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.contentContainer, new SearchFragment())
                .commit();
    }

    @Override
    public void showControls() {
        if (forbidShowControls) return;
        Slide slide = createSlideTransition();
        slide.setMode(Visibility.MODE_IN);
        //noinspection ConstantConditions
        TransitionManager.beginDelayedTransition(root, slide);
        playerControlsFr.setVisibility(View.VISIBLE);
        playerControlsFr.postDelayed(() -> {
            playerControlsFr.setPadding(0, 0, 0, 0);
        }, 300);
    }

    @Override
    public void hideControls() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Slide slide = createSlideTransition();
        slide.setMode(Visibility.MODE_OUT);
        //noinspection ConstantConditions
        TransitionManager.beginDelayedTransition(root, slide);
        playerControlsFr.setVisibility(View.GONE);
    }

    @NonNull
    private Slide createSlideTransition() {
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.BOTTOM);
        slide.setDuration(300);
        slide.addTarget(playerControlsFr);
        slide.setInterpolator(new FastOutSlowInInterpolator());
        return slide;
    }

    private void showAbout() {
        hideControls();
        Intent showAbout = new Intent(this, AboutActivity.class);
        startActivity(showAbout);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void exit() {
        presenter.exit();
        finish();
    }

    private void enableToolbarScroll(boolean enable) {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(enable ? AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP : 0);
    }
}
