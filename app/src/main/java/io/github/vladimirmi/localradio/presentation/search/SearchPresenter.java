package io.github.vladimirmi.localradio.presentation.search;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.SearchInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchPresenter extends BasePresenter<SearchView> {

    SearchInteractor interactor;

    @Inject
    public SearchPresenter(SearchInteractor interactor) {
        this.interactor = interactor;
    }

    @Override
    protected void onAttach(SearchView view) {
        view.setCountries(interactor.getCountries());
    }
}
