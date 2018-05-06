package io.github.vladimirmi.localradio.presentation.playercontrol;

import android.graphics.PorterDuff;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;
import io.github.vladimirmi.localradio.utils.UiUtils;
import io.github.vladimirmi.playerbutton.PlayerButton;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public class PlayerControlFragment extends BaseFragment<PlayerControlPresenter> implements PlayerControlView {

    @BindView(R.id.iconIv) ImageView iconIv;
    @BindView(R.id.previousBt) Button previousBt;
    @BindView(R.id.playPauseBt) PlayerButton playPauseBt;
    @BindView(R.id.loadingPb) ProgressBar loadingPb;
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

    private String stationImageUrl = "";

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

        playPauseBt.setManualMode(true);
        loadingPb.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(R.color.grey_50), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void setStation(Station station) {
        if (!stationImageUrl.equals(station.getImageUrl())) {
            stationImageUrl = station.getImageUrl();
            UiUtils.loadImageInto(iconIv, station);
        }

        favoriteBt.setBackgroundResource(station.isFavorite() ? R.drawable.ic_star : R.drawable.ic_star_empty);

        setTextOrHideIfEmpty(titleTv, station.getName());
        setTextOrHideIfEmpty(sloganTv, station.getSlogan());
        setTextOrHideIfEmpty(descriptionTv, station.getDescription());
        setTextOrHideIfEmpty(genreTv, station.getGenre());
        setTextOrHideIfEmpty(websiteTv, station.getWebsiteUrl());
        setTextOrHideIfEmpty(emailTv, station.getEmail());
        setTextOrHideIfEmpty(phoneTv, station.getPhone());

        // TODO: 5/5/18 remove string
        if ("{unlisted}".equals(station.getCity())) {
            locationTv.setText(station.getCountryCode());
        } else {
            setTextOrHideIfEmpty(locationTv, String.format("%s, %s", station.getCity(), station.getCountryCode()));
        }
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
    public void showLoading() {
        loadingPb.setVisibility(View.VISIBLE);
    }

    @Override
    public void showPlaying() {
        playPauseBt.setPlaying(true);
        loadingPb.setVisibility(View.GONE);
    }

    @Override
    public void showStopped() {
        playPauseBt.setPlaying(false);
        loadingPb.setVisibility(View.GONE);
    }
}
