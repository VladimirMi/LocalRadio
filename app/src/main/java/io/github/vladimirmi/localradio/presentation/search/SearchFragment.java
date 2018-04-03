package io.github.vladimirmi.localradio.presentation.search;

import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchFragment extends BaseFragment<SearchPresenter> implements SearchView {

    @BindView(R.id.autodetectTv) TextView autodetectTv;
    @BindView(R.id.autodetectCb) CheckBox autodetectCb;
    @BindView(R.id.countryEt) AutoCompleteTextView countryEt;
    @BindView(R.id.cityEt) AutoCompleteTextView cityEt;

    @Override
    protected int getLayout() {
        return R.layout.fragment_search;
    }

    @Override
    protected SearchPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(SearchPresenter.class);
    }

    @Override
    protected void setupView(View view) {

    }
}
