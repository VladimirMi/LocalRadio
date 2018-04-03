package io.github.vladimirmi.localradio;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */
public class SearchFragment extends Fragment {

    @BindView(R.id.autodetectTv) TextView autodetectTv;
    @BindView(R.id.autodetectCb) CheckBox autodetectCb;
    @BindView(R.id.countryEt) AutoCompleteTextView countryEt;
    @BindView(R.id.cityEt) AutoCompleteTextView cityEt;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
