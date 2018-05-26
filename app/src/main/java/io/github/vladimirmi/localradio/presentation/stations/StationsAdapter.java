package io.github.vladimirmi.localradio.presentation.stations;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.utils.UiUtils;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsAdapter extends RecyclerView.Adapter<StationsAdapter.StationVH> {

    private static final String PAYLOAD_SELECTED_CHANGE = "PAYLOAD_SELECTED_CHANGE";

    private final onStationListener listener;
    private List<Station> stations = Collections.emptyList();
    private Station selectedStation = Station.nullObject();
    private int selectedPosition;
    private boolean playing;


    public StationsAdapter(onStationListener listener) {
        this.listener = listener;
    }

    public void setData(List<Station> list) {
        stations = list;
        selectedPosition = stations.indexOf(selectedStation);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station, parent, false);
        return new StationVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationVH holder, int position, @NonNull List<Object> payloads) {
        Station station = stations.get(position);
        holder.itemView.setOnClickListener(view -> listener.onStationClick(station));

        if (payloads.contains(PAYLOAD_SELECTED_CHANGE)) {
            holder.select(station.id == selectedStation.id, playing);

        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StationVH holder, int position) {
        Station station = stations.get(position);
        holder.bind(station);
        holder.setFavorite(station);
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


    static class StationVH extends RecyclerView.ViewHolder {

        @BindView(R.id.iconIv) ImageView imageIv;
        @BindView(R.id.titleTv) TextView titleTv;
        @BindView(R.id.bandTv) TextView bandTv;
        @BindView(R.id.favoriteIv) ImageView favoriteIv;
        @BindView(R.id.genresTv) TextView genresTv;

        StationVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Station station) {
            titleTv.setText(station.name);
            genresTv.setText(station.genre);
            bandTv.setText(station.bandString);

            UiUtils.loadImageInto(imageIv, station);
        }

        void setFavorite(Station station) {
//            favoriteIv.setVisibility(station.isFavorite() ? View.VISIBLE : View.GONE);
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
