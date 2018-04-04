package io.github.vladimirmi.localradio.presentation.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public abstract class BaseActivity<P extends BasePresenter> extends AppCompatActivity {

    protected P mPresenter;

    protected abstract int getLayout();

    protected abstract P providePresenter();

    protected abstract void setupView();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        mPresenter = providePresenter();
        ButterKnife.bind(this);
        setupView();
    }

    @Override
    public void onResume() {
        super.onResume();
        //noinspection unchecked
        mPresenter.attachView((BaseView) this);
    }

    @Override
    public void onPause() {
        mPresenter.detachView();
        super.onPause();
    }
}
