package io.github.vladimirmi.localradio.presentation.search;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.Country;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;
import io.github.vladimirmi.localradio.utils.CustomArrayAdapter;
import io.github.vladimirmi.localradio.utils.CustomAutoCompleteView;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchFragment extends BaseFragment<SearchPresenter> implements SearchView {

    @BindView(R.id.autodetectTv) TextView autodetectTv;
    @BindView(R.id.autodetectCb) CheckBox autodetectCb;
    @BindView(R.id.countryEt) CustomAutoCompleteView countryEt;
    @BindView(R.id.cityEt) CustomAutoCompleteView cityEt;

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
        autodetectCb.setOnCheckedChangeListener((compoundButton, b) -> presenter.setAutodetect(b));

        countryEt.setOnItemClickListener((parent, v, position, id) -> {
            presenter.selectCountry((Country) parent.getItemAtPosition(position));
        });

        cityEt.setOnItemClickListener((parent, v, position, id) -> {
            presenter.selectCity((String) parent.getItemAtPosition(position));
        });

        cityEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                cityEt.dismissDropDown();
                InputMethodManager imm = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cityEt.getWindowToken(), 0);
            }
            return true;
        });
    }

    @Override
    public void setCountries(List<Country> countries) {
        CustomArrayAdapter<Country> countryAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, countries);
        countryAdapter.setDefaultValue(Country.any(getContext()));

        countryEt.setAdapter(countryAdapter);

        countryAdapter.setOnFilteringListener(filteredData -> presenter.selectCountries(filteredData));
    }

    @Override
    public void setCities(List<String> cities) {
        CustomArrayAdapter<String> cityAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, cities);
        cityAdapter.setDefaultValue(Country.any(getContext()).getCities().get(0));

        cityEt.setAdapter(cityAdapter);
    }

    @Override
    public void setCountry(String name) {
        countryEt.setText(name);
    }

    @Override
    public void setCity(List<String> cities) {
        if (!cities.contains(cityEt.getText().toString())) {
            cityEt.setText(cities.get(0));
        }
    }

    @Override
    public void setAutodetect(Boolean enabled) {
        autodetectCb.setChecked(enabled);
        enableView(countryEt, !enabled);
        enableView(cityEt, !enabled);
    }

    private void enableView(TextView view, boolean enable) {
        view.setEnabled(enable);
        view.setFocusable(enable);
        view.setFocusableInTouchMode(enable);
    }
}
