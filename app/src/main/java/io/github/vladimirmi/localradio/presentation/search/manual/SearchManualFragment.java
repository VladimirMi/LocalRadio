package io.github.vladimirmi.localradio.presentation.search.manual;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.custom.CustomArrayAdapter;
import io.github.vladimirmi.localradio.custom.CustomAutoCompleteView;
import io.github.vladimirmi.localradio.custom.TextViewValidator;
import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;
import io.github.vladimirmi.localradio.utils.UiUtils;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchManualFragment extends BaseFragment<SearchManualPresenter> implements SearchManualView {

    @BindView(R.id.countryEt) CustomAutoCompleteView<LocationEntity> countryEt;
    @BindView(R.id.cityEt) CustomAutoCompleteView<LocationEntity> cityEt;
    @BindView(R.id.selectionResultTv) TextView selectionResultTv;

    @Override
    protected int getLayout() {
        return R.layout.fragment_search_manual;
    }

    @Override
    protected SearchManualPresenter providePresenter() {
        return Scopes.getLocationsScope().getInstance(SearchManualPresenter.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void setupView(View view) {
        countryEt.setOnCompletionListener(item -> {
            presenter.selectCountry(item);
        });
        cityEt.setOnCompletionListener(item -> {
            presenter.selectCity(item);
        });

        cityEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                cityEt.dismissDropDown();
                UiUtils.hideSoftKeyBoard(getContext(), cityEt.getWindowToken());
            }
            return true;
        });
    }



    @Override
    public void setCountrySuggestions(List<LocationEntity> countries) {
        CustomArrayAdapter<LocationEntity> countryAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, countries);

        countryEt.setAdapter(countryAdapter);
        countryEt.setValidator(new TextViewValidator<>(countries));
    }

    @Override
    public void setCitySuggestions(List<LocationEntity> cities) {
        CustomArrayAdapter<LocationEntity> cityAdapter = new CustomArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, cities);

        cityEt.setAdapter(cityAdapter);
        cityEt.setValidator(new TextViewValidator<>(cities));
    }

    @Override
    public void setCountry(String name) {
        countryEt.setText(name, false);
        countryEt.setSelection(name.length());
    }

    @Override
    public void setCity(String city) {
        cityEt.setText(city, false);
        cityEt.setSelection(city.length());
    }

    @Override
    public void setSelectionResult(int stations) {
        String s = getResources().getQuantityString(R.plurals.selection_result, stations, stations);
        selectionResultTv.setText(s);
    }

    //    @Override
//    public void enableLocationData(boolean enabled) {
//        isAutodetect = enabled;
//        autodetectCb.setChecked(isAutodetect);
//    }

//    @Override
//    public void setSearchDone(boolean done) {
//        enableTextView(countryEt, !done);
//        enableTextView(cityEt, !done);
//        setVisible(clearCountryBt, !done);
//        setVisible(clearCityBt, !done);
//        searchBt.setImageResource(done ? R.drawable.ic_repeat_search : R.drawable.ic_search);
//
//        isRefreshEnabled = done;
//        //noinspection ConstantConditions
//        getActivity().invalidateOptionsMenu();
//    }


    private void enableTextView(TextView view, boolean enable) {
        view.setEnabled(enable);
        view.setFocusable(enable);
        view.setFocusableInTouchMode(enable);
    }

    private void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
