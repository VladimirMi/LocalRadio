package io.github.vladimirmi.localradio.domain.models;

import static io.github.vladimirmi.localradio.domain.models.SearchResult.State.DONE;
import static io.github.vladimirmi.localradio.domain.models.SearchResult.State.LOADING;
import static io.github.vladimirmi.localradio.domain.models.SearchResult.State.NOT_DONE;

/**
 * Created by Vladimir Mikhalev 31.05.2018.
 */

public class SearchResult {

    public enum State {NOT_DONE, LOADING, DONE}

    public final State state;
    public final int result;

    private SearchResult(State state, int result) {
        this.state = state;
        this.result = result;
    }

    public static SearchResult notDone() {
        return new SearchResult(NOT_DONE, 0);
    }

    public static SearchResult done(int stations) {
        return new SearchResult(DONE, stations);
    }

    public static SearchResult loading() {
        return new SearchResult(LOADING, 0);
    }

    public boolean isSearchDone() {
        return state == DONE;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "state=" + state +
                ", result=" + result +
                '}';
    }
}
