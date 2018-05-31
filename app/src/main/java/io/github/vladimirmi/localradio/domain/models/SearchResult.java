package io.github.vladimirmi.localradio.domain.models;

import static io.github.vladimirmi.localradio.domain.models.SearchResult.State.AUTO_DONE;
import static io.github.vladimirmi.localradio.domain.models.SearchResult.State.LOADING;
import static io.github.vladimirmi.localradio.domain.models.SearchResult.State.MANUAL_DONE;
import static io.github.vladimirmi.localradio.domain.models.SearchResult.State.NOT_DONE;

/**
 * Created by Vladimir Mikhalev 31.05.2018.
 */

public class SearchResult {

    public final State state;
    public final int result;
    public final String message;

    private SearchResult(State state, int result, String message) {
        this.state = state;
        this.result = result;
        this.message = message;
    }

    public static SearchResult notDone() {
        return new SearchResult(NOT_DONE, 0, "");
    }

    public static SearchResult done() {
        return new SearchResult(MANUAL_DONE, 0, "");
    }

    public static SearchResult loading() {
        return new SearchResult(LOADING, 0, "");
    }

    public static SearchResult doneAuto(int result, String message) {
        return new SearchResult(AUTO_DONE, result, message);
    }

    public static SearchResult doneManual(int result, String message) {
        return new SearchResult(MANUAL_DONE, result, message);
    }

    public boolean isSearchDone() {
        return state == AUTO_DONE || state == MANUAL_DONE;
    }

    public enum State {NOT_DONE, LOADING, AUTO_DONE, MANUAL_DONE}
}
