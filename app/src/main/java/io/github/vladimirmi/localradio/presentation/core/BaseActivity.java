package io.github.vladimirmi.localradio.presentation.core;

import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.common.api.ResolvableApiException;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public abstract class BaseActivity<P extends BasePresenter> extends AppCompatActivity implements BaseView {

    private static final int REQUEST_CHECK_SETTINGS = 123;

    protected P presenter;
    protected @BindView(android.R.id.content) View contentView;

    protected abstract int getLayout();

    protected abstract P providePresenter();

    protected abstract void setupView();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        presenter = providePresenter();
        ButterKnife.bind(this);
        setupView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //noinspection unchecked
        presenter.attachView(this);
    }

    @Override
    protected void onStop() {
        presenter.detachView();
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            presenter.destroy();
            presenter = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!handleBackPress()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean handleBackPress() {
        boolean handled = false;
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof BaseView) {
                handled = ((BaseView) fragment).handleBackPress();
            }
        }
        return handled;
    }

    @Override
    public Observable<Boolean> resolvePermissions(String... permissions) {
        return new RxPermissions(this).request(permissions);
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(contentView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(int messageId) {
        Snackbar.make(contentView, messageId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void resolveApiException(ResolvableApiException resolvable) {
        try {
            resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
        }
    }
}
