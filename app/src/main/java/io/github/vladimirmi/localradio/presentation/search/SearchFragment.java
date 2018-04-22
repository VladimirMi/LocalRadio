package io.github.vladimirmi.localradio.presentation.search;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;
import io.github.vladimirmi.localradio.utils.CustomArrayAdapter;
import io.github.vladimirmi.localradio.utils.CustomAutoCompleteView;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchFragment extends BaseFragment<SearchPresenter> implements SearchView {

    @BindView(R.id.autodetectCb) CheckedTextView autodetectCb;
    @BindView(R.id.countryEt) CustomAutoCompleteView countryEt;
    @BindView(R.id.cityLabelTv) TextView cityLabelTv;
    @BindView(R.id.cityEt) CustomAutoCompleteView cityEt;
    @BindView(R.id.searchBt) Button searchBt;
    @BindView(R.id.refreshBt) Button refreshBt;
    @BindView(R.id.newSearchBt) Button newSearchBt;
    @BindView(R.id.searchResultTv) TextView searchResultTv;

    @Override
    protected int getLayout() {
        return R.layout.fragment_search;
    }

    @Override
    protected SearchPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(SearchPresenter.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void setupView(View view) {
        autodetectCb.setOnClickListener(v -> presenter.setAutodetect(!autodetectCb.isChecked()));

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

        searchBt.setOnClickListener(v -> {
            if (countryEt.getText().toString().isEmpty()) countryEt.setText(" ");
            countryEt.performValidation();
            if (cityEt.getText().toString().isEmpty()) cityEt.setText(" ");
            cityEt.performValidation();
            presenter.search(countryEt.getText().toString(),
                    cityEt.getText().toString());
        });

        refreshBt.setOnClickListener(v -> presenter.refreshSearch());
        newSearchBt.setOnClickListener(v -> presenter.newSearch());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setCountries(List<Country> countries) {
        CustomArrayAdapter<Country> countryAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, countries);
        countryAdapter.setDefaultValue(Country.any(getContext()));

        countryEt.setAdapter(countryAdapter);
        countryEt.setValidator(new CustomAutoCompleteView.CustomValidator<>(countries));

        countryAdapter.setOnFilteringListener(filteredData -> presenter.selectCountries(filteredData));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setCities(List<String> cities) {
        CustomArrayAdapter<String> cityAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, cities);
        cityAdapter.setDefaultValue(Country.any(getContext()).getCities().get(0));

        cityEt.setAdapter(cityAdapter);
        cityEt.setValidator(new CustomAutoCompleteView.CustomValidator<>(cities));
    }

    @Override
    public void setCountryName(String name) {
        countryEt.setText(name);
        countryEt.setSelection(name.length());
    }

    @Override
    public void setCity(String city) {
        cityEt.setText(city);
        cityEt.setSelection(city.length());
    }

    //workaround on not correct initialized checkbox view on a hidden fragment
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isFragmentVisible = isVisibleToUser;
        if (getView() != null && isVisibleToUser) {
            setAutodetect(isAutodetect);
        }
    }

    private boolean isFragmentVisible;
    private boolean isAutodetect;

    @Override
    public void setAutodetect(boolean enabled) {
        isAutodetect = enabled;
        if (!isFragmentVisible) return;
        autodetectCb.setChecked(enabled);
        enableView(countryEt, !enabled);
        enableView(cityEt, !enabled);
        setVisible(cityLabelTv, !enabled);
        setVisible(cityEt, !enabled);
        setVisible(searchBt, !enabled);
        setVisible(newSearchBt, !enabled);
        setVisible(refreshBt, enabled);
    }

    @Override
    public void setNewSearch(boolean enabled) {
        enableView(countryEt, enabled);
        enableView(cityEt, enabled);
        setVisible(searchBt, enabled);
        setVisible(newSearchBt, !enabled);
        setVisible(refreshBt, !enabled);
    }

    @Override
    public void setSearchResult(int foundStations) {
        if (foundStations == -1) {
            searchResultTv.setText("");
        } else {
            //todo plurals
            searchResultTv.setText(String.format("Found %d stations", foundStations));
        }
    }

    private void enableView(TextView view, boolean enable) {
        view.setEnabled(enable);
        view.setFocusable(enable);
        view.setFocusableInTouchMode(enable);
    }

    private void setVisible(View view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }
}
