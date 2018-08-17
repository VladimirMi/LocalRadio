package io.github.vladimirmi.localradio.map;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;

/**
 * Created by Vladimir Mikhalev 27.07.2018.
 */
public class CustomAlgorithm extends NonHierarchicalDistanceBasedAlgorithm<LocationClusterItem> {

    private Set<LocationClusterItem> selectedItems = Collections.emptySet();

    public void setSelectedItems(Set<LocationClusterItem> selectedItems) {
        this.selectedItems = selectedItems;
    }

    @Override
    public Set<? extends Cluster<LocationClusterItem>> getClusters(double zoom) {

        Collection<LocationClusterItem> items = getItems();
        List<LocationClusterItem> unselectedItems = new ArrayList<>();
        for (LocationClusterItem item : items) {
            if (!selectedItems.contains(item)) unselectedItems.add(item);
        }
        Set<Cluster<LocationClusterItem>> result = new HashSet<>(getInternalClusters(selectedItems, zoom));
        result.addAll(getInternalClusters(unselectedItems, zoom));
        return result;
    }

    private Set<? extends Cluster<LocationClusterItem>> getInternalClusters(
            Collection<LocationClusterItem> items, double zoom) {

        final NonHierarchicalDistanceBasedAlgorithm<LocationClusterItem> algo
                = new NonHierarchicalDistanceBasedAlgorithm<>();

        algo.addItems(items);
        return algo.getClusters(zoom);
    }

}
