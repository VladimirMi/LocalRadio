package io.github.vladimirmi.localradio.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.view.ClusterRenderer;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;

/**
 * Created by Vladimir Mikhalev 24.07.2018.
 */
public class CustomClusterManager extends ClusterManager<LocationClusterItem> {

    private final CustomAlgorithm algorithm;
    private final ReadWriteLock algorithmLock = new ReentrantReadWriteLock();
    private final CustomClusterRenderer renderer;

    private final GoogleMap map;
    private final ReadWriteLock clusterTaskLock = new ReentrantReadWriteLock();
    private ClusterTask clusterTask;

    public CustomClusterManager(Context context, GoogleMap map) {
        super(context, map);

        this.map = map;

        algorithm = new CustomAlgorithm();
        clusterTask = new ClusterTask();
        renderer = new CustomClusterRenderer(context, map, this);
        setRenderer(renderer);
    }

    @Override
    public void setAlgorithm(Algorithm<LocationClusterItem> algorithm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClusterRenderer<LocationClusterItem> getRenderer() {
        return renderer;
    }

    @Override
    public Algorithm<LocationClusterItem> getAlgorithm() {
        return algorithm;
    }

    @Override
    public void clearItems() {
        algorithmLock.writeLock().lock();
        try {
            algorithm.clearItems();
        } finally {
            algorithmLock.writeLock().unlock();
        }
    }

    @Override
    public void addItems(Collection<LocationClusterItem> items) {
        algorithmLock.writeLock().lock();
        try {
            algorithm.addItems(items);
        } finally {
            algorithmLock.writeLock().unlock();
        }
    }

    @Override
    public void addItem(LocationClusterItem myItem) {
        algorithmLock.writeLock().lock();
        try {
            algorithm.addItem(myItem);
        } finally {
            algorithmLock.writeLock().unlock();
        }
    }

    @Override
    public void removeItem(LocationClusterItem item) {
        algorithmLock.writeLock().lock();
        try {
            algorithm.removeItem(item);
        } finally {
            algorithmLock.writeLock().unlock();
        }
    }

    @Override
    public void cluster() {
        clusterTaskLock.writeLock().lock();
        try {
            clusterTask.cancel(true);
            clusterTask = new ClusterTask();
            clusterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, map.getCameraPosition().zoom);
        } finally {
            clusterTaskLock.writeLock().unlock();
        }
    }

    public void selectClusters(Set<LocationClusterItem> clusterItems) {
        renderer.selectItems(clusterItems);
        algorithm.setSelectedItems(clusterItems);
    }

    @SuppressLint("StaticFieldLeak")
    private class ClusterTask extends AsyncTask<Float, Void, Set<? extends Cluster<LocationClusterItem>>> {

        @Override
        protected Set<? extends Cluster<LocationClusterItem>> doInBackground(Float... zoom) {
            algorithmLock.readLock().lock();
            try {
                return algorithm.getClusters(zoom[0]);
            } finally {
                algorithmLock.readLock().unlock();
            }
        }

        @Override
        protected void onPostExecute(Set<? extends Cluster<LocationClusterItem>> clusters) {
            renderer.onClustersChanged(clusters);
        }
    }
}
