package io.github.vladimirmi.localradio.presentation.search;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
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
    @BindView(R.id.searchBt) FloatingActionButton searchBt;
    @BindView(R.id.searchResultTv) TextView searchResultTv;
    @BindView(R.id.loadingPb) ProgressBar loadingPb;
    @BindView(R.id.clearCountryBt) ImageButton clearCountryBt;
    @BindView(R.id.clearCityBt) ImageButton clearCityBt;

    private boolean isRefreshEnabled = false;

    @Override
    protected int getLayout() {
        return R.layout.fragment_search;
    }

    @Override
    protected SearchPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(SearchPresenter.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(isRefreshEnabled ? R.menu.menu_refresh : R.menu.menu_common, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            presenter.refreshSearch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void setupView(View view) {
        autodetectCb.setOnClickListener(v -> presenter.enableAutodetect(!autodetectCb.isChecked()));
        countryEt.setOnCompletionListener(text -> presenter.selectCountry(text));
        cityEt.setOnCompletionListener(text -> presenter.selectCity(text));
        clearCountryBt.setOnClickListener(v -> countryEt.setText(""));
        clearCityBt.setOnClickListener(v -> cityEt.setText(""));

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
            String countryName = countryEt.getText().toString();
            String city = cityEt.getText().toString();
            presenter.search(countryName, city);
        });

        loadingPb.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void setCountrySuggestions(List<String> countries) {
        CustomArrayAdapter<String> countryAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, countries);

        countryEt.setAdapter(countryAdapter);
        countryEt.setValidator(new CustomAutoCompleteView.CustomValidator<>(countries));
    }

    @Override
    public void setCitySuggestions(List<String> cities) {
        CustomArrayAdapter<String> cityAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, cities);

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
        setVisible(clearCountryBt, !done);
        setVisible(clearCityBt, !done);
        searchBt.setImageResource(done ? R.drawable.ic_repeat_search : R.drawable.ic_search);
    }

    @Override
    public void showSearchBtn(boolean visible) {
        setVisible(searchBt, visible);
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
    public void enableSearch(boolean enabled) {
        searchBt.setEnabled(enabled);
    }

    @Override
    public void enableRefresh(boolean enabled) {
        isRefreshEnabled = enabled;
        //noinspection ConstantConditions
        getActivity().invalidateOptionsMenu();
    }

    private void enableTextView(TextView view, boolean enable) {
        view.setEnabled(enable);
        view.setFocusable(enable);
        view.setFocusableInTouchMode(enable);
    }

    private void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
