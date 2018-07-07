package io.github.vladimirmi.localradio.presentation.search;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.presentation.core.BasePresenter;

/**
 * Created by Vladimir Mikhalev 01.07.2018.
 */
@SuppressWarnings("WeakerAccess")
public class SearchPresenter extends BasePresenter<SearchView> {

    public static final String MAP_MODE = "MAP_MODE";
    public static final String MANUAL_MODE = "MANUAL_MODE";

    @Inject
    public SearchPresenter() {
    }
}
