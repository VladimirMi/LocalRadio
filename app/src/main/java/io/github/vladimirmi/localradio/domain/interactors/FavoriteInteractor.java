package io.github.vladimirmi.localradio.domain.interactors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Observable<Set<Integer>> getFavoriteIds() {
        return getFavoriteStations()
                .map(stations -> {
                    Set<Integer> ids = new HashSet<>(stations.size());
                    for (Station station : stations) {
                        ids.add(station.id);
                    }
                    return ids;
                });
    }

    public Completable switchCurrentFavorite() {
        Completable switchFavorite;
        Station current = stationsRepository.getCurrentStation();
        if (favoriteRepository.getFavoriteStations().contains(current)) {
//          changeCurrentStationOnNextFavorite();
            switchFavorite = favoriteRepository.removeFavorite(current);
        } else {
            switchFavorite = favoriteRepository.addFavorite(current);
        }

        return switchFavorite.subscribeOn(Schedulers.io());
    }

    public Observable<Boolean> isCurrentStationFavorite() {
        return stationsRepository.getCurrentStationObs()
                .map(station -> favoriteRepository.getFavoriteStations().contains(station));
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
