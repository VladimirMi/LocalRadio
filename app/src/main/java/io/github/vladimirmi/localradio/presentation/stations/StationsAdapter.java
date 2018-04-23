package io.github.vladimirmi.localradio.presentation.stations;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsAdapter extends ListAdapter<Station, StationsAdapter.StationVH> {

    private static final String PAYLOAD_SELECTED_CHANGE = "PAYLOAD_SELECTED_CHANGE";
    private static final String PAYLOAD_FAVORITE_CHANGE = "PAYLOAD_FAVORITE_CHANGE";

    private final onStationListener listener;
    private List<Station> stations = Collections.emptyList();
    private int selectedPosition;
    private Station selectedStation;
    private boolean playing;

    private static final DiffUtil.ItemCallback CALLBACK = new DiffUtil.ItemCallback<Station>() {
        @Override
        public boolean areItemsTheSame(Station oldItem, Station newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Station oldItem, Station newItem) {
            return oldItem.isFavorite() == newItem.isFavorite();
        }

        @Override
        public Object getChangePayload(Station oldItem, Station newItem) {
            return PAYLOAD_FAVORITE_CHANGE;
        }
    };

    public StationsAdapter(onStationListener listener) {
        super(CALLBACK);
        this.listener = listener;
    }

    @Override
    public void submitList(List<Station> list) {
        stations = list;
        selectedPosition = stations.indexOf(selectedStation);
        super.submitList(list);
    }

    @NonNull
    @Override
    public StationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station, parent, false);
        return new StationVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationVH holder, int position, @NonNull List<Object> payloads) {
        if (payloads.contains(PAYLOAD_SELECTED_CHANGE)) {
            holder.select(position == selectedPosition, playing);

        } else if (payloads.contains(PAYLOAD_FAVORITE_CHANGE)) {
            holder.setFavorite(getItem(position));

        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StationVH holder, int position) {
        Station station = getItem(position);
        holder.bind(station);
        holder.setFavorite(station);
        holder.select(position == selectedPosition, playing);
        holder.itemView.setOnClickListener(view -> listener.onStationClick(getItem(position)));
    }

    public void select(Station station) {
        int newSelectedPos = stations.indexOf(station);
        int oldSelectedPos = stations.indexOf(selectedStation);

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

    @Override
    protected Station getItem(int position) {
        return stations.get(position);
    }

    static class StationVH extends RecyclerView.ViewHolder {

        @BindView(R.id.imageIv) ImageView imageIv;
        @BindView(R.id.titleTv) TextView titleTv;
        @BindView(R.id.bandTv) TextView bandTv;
        @BindView(R.id.favoriteIv) ImageView favoriteIv;
        @BindView(R.id.genresTv) TextView genresTv;

        StationVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Station station) {
            titleTv.setText(station.getCallsign());
            genresTv.setText(station.getGenre());
            String band;
            if (station.getBand().equals("net")) {
                band = station.getBand();
            } else {
                band = String.format("%s %s", station.getDial(), station.getBand());
            }
            bandTv.setText(band);

            Glide.with(itemView.getContext())
                    .load(station.getImageurl())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .error(R.drawable.ic_radio)
                    .into(imageIv);

        }

        void setFavorite(Station station) {
            favoriteIv.setVisibility(station.isFavorite() ? View.VISIBLE : View.GONE);
        }

        void select(boolean select, boolean playing) {
            final int color;
            if (select) {
                color = ContextCompat.getColor(itemView.getContext(),
                        playing ? R.color.playing : R.color.selected);
            } else {
                color = ContextCompat.getColor(itemView.getContext(), android.R.color.transparent);
            }
            itemView.setBackgroundColor(color);
        }
    }

    public interface onStationListener {

        void onStationClick(Station station);

    }
}
