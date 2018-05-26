package io.github.vladimirmi.localradio.domain.interactors;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.repository.FavoriteRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoriteInteractor {

    private final StationsRepository stationsRepository;
    private final FavoriteRepository favoriteRepository;
    private final PlayerControlInteractor controlInteractor;

    @Inject
    public FavoriteInteractor(StationsRepository stationsRepository,
                              FavoriteRepository favoriteRepository,
                              PlayerControlInteractor controlInteractor) {
        this.stationsRepository = stationsRepository;
        this.favoriteRepository = favoriteRepository;
        this.controlInteractor = controlInteractor;
    }


    public Observable<List<Station>> getFavoriteStations() {
        return favoriteRepository.getFavoriteStationsObs();
    }

    public Completable switchCurrentFavorite() {
        Completable swithFavorite;
        Station current = stationsRepository.getCurrentStation();
        if (favoriteRepository.getFavoriteStations().contains(current)) {
//          changeCurrentStationOnNextFavorite();
            swithFavorite = favoriteRepository.removeFavorite(current);
        } else {
            swithFavorite = favoriteRepository.addFavorite(current);
        }

        return swithFavorite.subscribeOn(Schedulers.io());
    }

    public Observable<Boolean> isCurrentStationFavorite() {
        return favoriteRepository.getFavoriteStationsObs()
                .map(stations -> stations.contains(stationsRepository.getCurrentStation()));
    }

    public void previousStation() {
        List<Station> source = favoriteRepository.getFavoriteStations();
        int indexOfCurrent = getIndexOfCurrent();
        if (indexOfCurrent == -1) return;

        int indexOfPrevious = (indexOfCurrent + source.size() - 1) % source.size();
        stationsRepository.setCurrentStation(source.get(indexOfPrevious));
    }

    public void nextStation() {
        List<Station> source = favoriteRepository.getFavoriteStations();
        int indexOfCurrent = getIndexOfCurrent();
        if (indexOfCurrent == -1) return;

        int indexOfNext = (indexOfCurrent + 1) % source.size();
        stationsRepository.setCurrentStation(source.get(indexOfNext));
    }


    private void setCurrentStationIfFavorite() {
        Station currentFavoriteStation = favoriteRepository.findCurrentFavoriteStation();
        if (currentFavoriteStation != null) {
            stationsRepository.setCurrentStation(currentFavoriteStation);
        }
    }

//    private boolean updateStationsIfFavorite(List<Station> stations) {
//        boolean updated = false;
//        for (int i = 0; i < stations.size(); i++) {
//            Station station = stations.get(i);
//            boolean isFavorite = false;
//            for (Station favoriteStation : favoriteRepository.getFavoriteStations()) {
//                if (station.id == favoriteStation.id) {
//                    isFavorite = true;
//                    break;
//                }
//            }
//            if (station.isFavorite() != isFavorite) {
//                stations.set(i, station.setFavoriteAndCopy(isFavorite));
//                updated = true;
//            }
//        }
//        return updated;
//    }
//
//    private void changeCurrentStationOnNextFavorite() {
//        if (!getMainInteractor().isFavoritePage() || controlInteractor.isPlaying()) {
//            return;
//        }
//        if (favoriteRepository.getFavoriteStations().size() == 1) {
//            if (stationsRepository.getStations().isEmpty()) {
//                stationsRepository.setCurrentStation(Station.nullStation());
//            }
//            return;
//        }
//        int indexOfCurrent = getIndexOfCurrent();
//        if (indexOfCurrent + 1 == favoriteRepository.getFavoriteStations().size()) {
//            previousStation();
//        } else {
//            nextStation();
//        }
//    }

    private int getIndexOfCurrent() {
        List<Station> stations = favoriteRepository.getFavoriteStations();
        Station currentStation = stationsRepository.getCurrentStation();

        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).id == currentStation.id) {
                return i;
            }
        }
        return -1;
    }
}
