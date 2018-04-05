package io.github.vladimirmi.localradio.presentation.search;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.CustomArrayAdapter;
import io.github.vladimirmi.localradio.CustomAutoCompleteView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.Country;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;

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
    }

    @Override
    public void setCountries(List<Country> countries) {
        // TODO: 4/5/18 separate to methods
        CustomArrayAdapter<Country> countryAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, countries);
        Country anyCountry = Country.any();
        String anyCity = anyCountry.getCities().get(0);
        countryAdapter.setDefaultValue(anyCountry);

        countryEt.setAdapter(countryAdapter);

        CustomArrayAdapter<String> cityAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, citiesFromCountries(countries));
        cityAdapter.setDefaultValue(anyCity);

        cityEt.setAdapter(cityAdapter);

        countryAdapter.setOnFilteringListener(filteredData -> {
            cityAdapter.setData(citiesFromCountries(filteredData));
        });

        countryEt.setOnItemClickListener((parent, view, position, id) -> {
            Country selected = (Country) parent.getItemAtPosition(position);
            List<String> cities;
            if (selected.equals(anyCountry)) {
                cities = citiesFromCountries(countries);
            } else {
                cities = selected.getCities();
                if (cities.size() > 1 && !cities.contains(anyCity) || cities.size() == 0) {
                    cities.add(0, anyCity);
                }
            }
            cityAdapter.setData(cities);
        });

        cityEt.setOnItemClickListener((parent, view, position, id) -> {
            String city = (String) parent.getItemAtPosition(position);
            if (!city.equals(anyCity) || countryEt.getText().toString().isEmpty()) {
                countryEt.setText(findCountryForCity(city, countries).getName());
            }
        });

        cityEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                cityEt.dismissDropDown();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cityEt.getWindowToken(), 0);
            }
            return true;
        });
    }

    private List<String> citiesFromCountries(List<Country> countries) {
        List<String> cities = new ArrayList<>();

        for (Country country : countries) {
            cities.addAll(country.getCities());
        }
        return cities;
    }

    private Country findCountryForCity(String city, List<Country> allCountries) {
        for (Country country : allCountries) {
            if (country.getCities().contains(city)) {
                return country;
            }
        }
        return null;
    }
}
