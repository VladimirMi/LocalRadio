package io.github.vladimirmi.localradio.presentation.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.vladimirmi.localradio.BuildConfig;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.interactors.PlayerControlInteractor;
import toothpick.Toothpick;

public class AboutActivity extends AppCompatActivity {

    @Inject PlayerControlInteractor controlInteractor;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.versionTv) TextView versionTv;
    @BindView(R.id.rateBt) Button rateBt;
    @BindView(R.id.brandTv) TextView brandTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toothpick.inject(this, Scopes.getAppScope());
        ButterKnife.bind(this);

        setupToolbar();
        versionTv.setText(BuildConfig.VERSION_NAME);
        setupRateButton();
        setupBrandLink();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_exit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;

        } else if (item.getItemId() == R.id.action_exit) {
            controlInteractor.stop();
            finishAffinity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRateButton() {
        Intent playIntent = new Intent(Intent.ACTION_VIEW);

        rateBt.setOnClickListener(v -> {
            playIntent.setData(Uri.parse("market://details?id=" + getPackageName()));
            if (!startIntent(playIntent)) {
                playIntent.setData(Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName()));
                startIntent(playIntent);
            }
        });
    }

    private void setupBrandLink() {
        Uri uri = Uri.parse("http://dar.fm/");
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        brandTv.setOnClickListener(v -> startIntent(browserIntent));
    }

    private boolean startIntent(Intent intent) {
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            return true;
        }
        return false;
    }
}
