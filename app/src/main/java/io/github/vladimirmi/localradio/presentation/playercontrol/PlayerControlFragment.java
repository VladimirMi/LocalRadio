package io.github.vladimirmi.localradio.presentation.playercontrol;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public class PlayerControlFragment extends BaseFragment<PlayerControlPresenter> implements PlayerControlView {

    @BindView(R.id.iconIv) ImageView iconIv;
    @BindView(R.id.previousBt) Button previousBt;
    @BindView(R.id.playPauseBt) Button playPauseBt;
    @BindView(R.id.nextBt) Button nextBt;
    @BindView(R.id.favoriteBt) Button favoriteBt;
    @BindView(R.id.metadataTv) TextView metadataTv;
    @BindView(R.id.titleTv) TextView titleTv;
    @BindView(R.id.sloganTv) TextView sloganTv;
    @BindView(R.id.descriptionTv) TextView descriptionTv;
    @BindView(R.id.genreTv) TextView genreTv;
    @BindView(R.id.locationTv) TextView locationTv;
    @BindView(R.id.websiteTv) TextView websiteTv;
    @BindView(R.id.emailTv) TextView emailTv;
    @BindView(R.id.phoneTv) TextView phoneTv;


    private String iconUrl;

    @Override
    protected int getLayout() {
        return R.layout.fragment_player_controls;
    }

    @Override
    protected PlayerControlPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(PlayerControlPresenter.class);
    }

    @Override
    protected void setupView(View view) {
        metadataTv.setSelected(true);
        playPauseBt.setOnClickListener(v -> presenter.playPause());
        previousBt.setOnClickListener(v -> presenter.skipToPrevious());
        nextBt.setOnClickListener(v -> presenter.skipToNext());
        favoriteBt.setOnClickListener(v -> presenter.switchFavorite());
    }

    @Override
    public void setStation(Station station) {
        metadataTv.setText(station.getName());
        if (iconUrl == null || !iconUrl.equals(station.getImageUrl())) {
            iconUrl = station.getImageUrl();

            Glide.with(getContext())
                    .load(iconUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .error(R.drawable.ic_radio)
                    .into(iconIv);
        }

        favoriteBt.setBackgroundResource(station.isFavorite() ? R.drawable.ic_star : R.drawable.ic_star_empty);

        setTextOrHideIfEmpty(titleTv, station.getName());
        setTextOrHideIfEmpty(sloganTv, station.getSlogan());
        setTextOrHideIfEmpty(descriptionTv, station.getDescription());
        setTextOrHideIfEmpty(genreTv, station.getGenre());
        setTextOrHideIfEmpty(locationTv, String.format("%s, %s", station.getCity(), station.getCountryCode()));
        setTextOrHideIfEmpty(websiteTv, station.getWebsiteUrl());
        setTextOrHideIfEmpty(emailTv, station.getEmail());
        setTextOrHideIfEmpty(phoneTv, station.getPhone());
    }

    private void setTextOrHideIfEmpty(TextView tv, String text) {
        if (text == null || text.isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

    @Override
    public void setMetadata(String string) {
        metadataTv.setText(string);
    }

    @Override
    public void setMetadata(int stringId) {
        metadataTv.setText(stringId);
    }

    @Override
    public void showPlaying() {
        playPauseBt.setBackgroundResource(R.drawable.ic_stop);
        if (metadataTv.getText().toString().equals(getString(R.string.metadata_buffering))) {
            metadataTv.setText("");
        }
    }

    @Override
    public void showStopped() {
        playPauseBt.setBackgroundResource(R.drawable.ic_play);
    }

}
