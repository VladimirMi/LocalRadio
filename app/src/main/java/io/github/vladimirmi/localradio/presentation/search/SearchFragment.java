package io.github.vladimirmi.localradio.presentation.search;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
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
    @BindView(R.id.cityEt) CustomAutoCompleteView cityEt;
    @BindView(R.id.countryTil) TextInputLayout countryTil;
    @BindView(R.id.cityTil) TextInputLayout cityTil;
    @BindView(R.id.searchBt) Button searchBt;
    @BindView(R.id.refreshBt) Button refreshBt;
    @BindView(R.id.newSearchBt) Button newSearchBt;
    @BindView(R.id.searchResultTv) TextView searchResultTv;
    @BindView(R.id.loadingPb) ProgressBar loadingPb;

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
        autodetectCb.requestFocus();
        autodetectCb.setOnClickListener(v -> presenter.enableAutodetect(!autodetectCb.isChecked()));
        countryEt.setOnCompletionListener(text -> presenter.selectCountry(text));
        cityEt.setOnCompletionListener(text -> presenter.selectCity(text));

        cityEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                cityEt.dismissDropDown();
                InputMethodManager imm = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cityEt.getWindowToken(), 0);
            }
            return true;
        });

        searchBt.setOnClickListener(v -> presenter.search(countryEt.getText().toString(),
                cityEt.getText().toString()));

        refreshBt.setOnClickListener(v -> presenter.refreshSearch());
        newSearchBt.setOnClickListener(v -> presenter.newSearch());

        loadingPb.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void setCountrySuggestions(List<String> countries) {
        CustomArrayAdapter<String> countryAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, countries);
        countryAdapter.setDefaultValue(Country.any().getName());

        countryEt.setAdapter(countryAdapter);
        countryEt.setValidator(new CustomAutoCompleteView.CustomValidator<>(countries));
    }

    @Override
    public void setCitySuggestions(List<String> cities) {
        CustomArrayAdapter<String> cityAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, cities);
        cityAdapter.setDefaultValue(Country.any().getCities().get(0));

        cityEt.setAdapter(cityAdapter);
        cityEt.setValidator(new CustomAutoCompleteView.CustomValidator<>(cities));
    }

    @Override
    public void setCountryName(String name) {
        setTextNoAnimate(countryTil, name);
        countryEt.setSelection(name.length());
    }

    @Override
    public void setCity(String city) {
        setTextNoAnimate(cityTil, city);
        cityEt.setSelection(city.length());
    }

    //workaround on not correct initialized checkbox view on a hidden fragment
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null && isVisibleToUser) {
            initAutoDetectCheckBox();
        }
    }

    private boolean isAutodetect;
    private boolean autodetectInitialized;

    private void initAutoDetectCheckBox() {
        if (!autodetectInitialized) {
            autodetectCb.setChecked(!isAutodetect);
            autodetectCb.setChecked(isAutodetect);
            autodetectInitialized = true;
        }
    }

    @Override
    public void setAutodetect(boolean enabled) {
        isAutodetect = enabled;
        autodetectCb.setChecked(isAutodetect);
    }

    @Override
    public void setSearchDone(boolean done) {
        enableTextView(countryEt, !done);
        enableTextView(cityEt, !done);
        setVisible(searchBt, !done);
        setVisible(refreshBt, done);
    }

    @Override
    public void showNewSearchBtn(boolean visible) {
        setVisible(newSearchBt, visible);
    }

    @Override
    public void showCity(boolean visible) {
        setVisible(cityTil, visible);
    }

    @Override
    public void setSearchResult(int foundStations) {
        String text = getResources().getQuantityString(R.plurals.search_result, foundStations, foundStations);
        searchResultTv.setText(text);
    }

    @Override
    public void resetSearchResult() {
        searchResultTv.setText("");
    }

    @Override
    public void setSearching(boolean enabled) {
        setVisible(loadingPb, enabled);
    }

    @Override
    public void enableAutodetect(boolean enabled) {
        autodetectCb.setEnabled(enabled);
    }

    @Override
    public void enableButtons(boolean enabled) {
        refreshBt.setEnabled(enabled);
        newSearchBt.setEnabled(enabled);
    }

    private void enableTextView(TextView view, boolean enable) {
        view.setEnabled(enable);
        view.setFocusable(enable);
        view.setFocusableInTouchMode(enable);
    }

    private void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setTextNoAnimate(TextInputLayout til, String text) {
        til.setHintAnimationEnabled(false);
        //noinspection ConstantConditions
        til.getEditText().setText(text);
        til.setHintAnimationEnabled(true);
    }
}
