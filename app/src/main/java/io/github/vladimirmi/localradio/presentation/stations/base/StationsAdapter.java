package io.github.vladimirmi.localradio.presentation.stations.base;

import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.custom.FixedOutlineProvider;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.utils.ImageUtils;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsAdapter extends RecyclerView.Adapter<StationsAdapter.StationVH> {

    private static final String PAYLOAD_FAVORITE_CHANGE = "PAYLOAD_FAVORITE_CHANGE";
    private static final String PAYLOAD_SELECTED_CHANGE = "PAYLOAD_SELECTED_CHANGE";
    private static final String PAYLOAD_BACKGROUND_CHANGE = "PAYLOAD_BACKGROUND_CHANGE";

    private final onStationListener listener;
    private List<Station> stations = Collections.emptyList();
    private Station selectedStation = Station.nullObject();
    private int selectedPosition;
    private Set<Integer> favoriteIds = Collections.emptySet();
    private boolean playing;
    private boolean isFavorite;

    private static final ViewOutlineProvider defaultOutline =
            Build.VERSION.SDK_INT >= 21 ? ViewOutlineProvider.BACKGROUND : null;
    private static final ViewOutlineProvider fixedOutline =
            Build.VERSION.SDK_INT >= 21 ? new FixedOutlineProvider() : null;

    @SuppressWarnings("WeakerAccess")
    public StationsAdapter(onStationListener listener) {
        this.listener = listener;
    }

    public void setData(List<Station> list) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return stations.size();
            }

            @Override
            public int getNewListSize() {
                return list.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return stations.get(oldItemPosition).id == list.get(newItemPosition).id;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }
        });
        stations = list;
        selectedPosition = stations.indexOf(selectedStation);
        diffResult.dispatchUpdatesTo(this);

        notifyItemRangeChanged(0, stations.size(), PAYLOAD_BACKGROUND_CHANGE);
    }

    public void setFavorites(Set<Integer> ids) {
        favoriteIds = ids;
        notifyItemRangeChanged(0, stations.size(), PAYLOAD_FAVORITE_CHANGE);
    }

    @NonNull
    @Override
    public StationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station, parent, false);
        ViewCompat.setElevation(view, parent.getContext().getResources().getDimension(R.dimen.item_card_elevation));
        return new StationVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationVH holder, int position, @NonNull List<Object> payloads) {
        Station station = stations.get(position);

        if (payloads.contains(PAYLOAD_SELECTED_CHANGE)) {
            holder.select(station.id == selectedStation.id, playing);

        } else if (payloads.contains(PAYLOAD_FAVORITE_CHANGE)) {
            holder.setFavorite(favoriteIds.contains(station.id));

        } else if (payloads.contains(PAYLOAD_BACKGROUND_CHANGE)) {
            holder.setBackground(position, getItemCount());
            holder.select(station.id == selectedStation.id, playing);

        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StationVH holder, int position) {
        Station station = stations.get(position);
        holder.bind(station);
        holder.setBackground(position, getItemCount());
        holder.setFavorite(isFavorite || favoriteIds.contains(station.id));
        holder.select(station.id == selectedStation.id, playing);
        holder.itemView.setOnClickListener(view -> listener.onStationClick(station));
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    public void select(Station station) {
        int newSelectedPos = stations.indexOf(station);
        int oldSelectedPos = selectedPosition;

        selectedPosition = newSelectedPos;
        selectedStation = station;

        notifyItemChanged(oldSelectedPos, PAYLOAD_SELECTED_CHANGE);
        notifyItemChanged(newSelectedPos, PAYLOAD_SELECTED_CHANGE);
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        notifyItemChanged(selectedPosition, PAYLOAD_SELECTED_CHANGE);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }


    static class StationVH extends RecyclerView.ViewHolder {

        @BindView(R.id.iconIv) ImageView imageIv;
        @BindView(R.id.titleTv) TextView titleTv;
        @BindView(R.id.bandTv) TextView bandTv;
        @BindView(R.id.favoriteIv) ImageView favoriteIv;
        @BindView(R.id.genresTv) TextView genresTv;
        @BindView(R.id.dilimeter) View dilimeter;

        StationVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Station station) {
            titleTv.setText(station.name);
            genresTv.setText(station.genre);
            bandTv.setText(station.bandString);

            ImageUtils.loadImageInto(imageIv, station);
        }

        void setFavorite(boolean isFavorite) {
            if ((favoriteIv.getVisibility() == View.VISIBLE) == isFavorite) return;
            if (isFavorite) {
                showFavorite();
            } else {
                hideFavorite();
            }
        }

        void select(boolean select, boolean playing) {
            final int color;
            if (select) {
                color = ContextCompat.getColor(itemView.getContext(),
                        playing ? R.color.playing : R.color.selected);
            } else {
                color = ContextCompat.getColor(itemView.getContext(), R.color.grey_50);
            }
            itemView.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        void setBackground(int position, int itemCount) {
            int res;
            if (itemCount == 1) {
                res = R.drawable.item_single;
                dilimeter.setVisibility(View.GONE);
            } else if (position == 0) {
                res = R.drawable.item_top;
                dilimeter.setVisibility(View.VISIBLE);
            } else if (position == itemCount - 1) {
                res = R.drawable.item_bottom;
                dilimeter.setVisibility(View.GONE);
            } else {
                res = R.drawable.item_middle;
                dilimeter.setVisibility(View.VISIBLE);
            }
            itemView.setBackground(ContextCompat.getDrawable(itemView.getContext(), res));

            if (Build.VERSION.SDK_INT < 21) return;
            if (itemCount == 1 || position == 0 || position == itemCount - 1) {
                itemView.setOutlineProvider(defaultOutline);
            } else {
                itemView.setOutlineProvider(fixedOutline);
            }
        }

        private void hideFavorite() {
            favoriteIv.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setDuration(200)
                    .start();
            favoriteIv.postDelayed(() -> favoriteIv.setVisibility(View.GONE), 200);
        }

        private void showFavorite() {
            favoriteIv.setVisibility(View.VISIBLE);
            favoriteIv.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(new AnticipateOvershootInterpolator(3f))
                    .setDuration(200)
                    .start();
        }
    }

    public interface onStationListener {

        void onStationClick(Station station);

    }
}
