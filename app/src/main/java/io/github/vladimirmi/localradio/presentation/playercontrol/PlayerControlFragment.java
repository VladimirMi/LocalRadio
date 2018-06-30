package io.github.vladimirmi.localradio.presentation.playercontrol;

import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.support.transition.ChangeBounds;
import android.support.transition.TransitionManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;
import io.github.vladimirmi.localradio.utils.AnimUtils;
import io.github.vladimirmi.localradio.utils.ImageUtils;
import io.github.vladimirmi.playerbutton.PlayerButton;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public class PlayerControlFragment extends BaseFragment<PlayerControlPresenter> implements PlayerControlView {

    private static final int NAV_BTN_VELOCITY = 200; // dp/sec
    private static final int FAV_VELOCITY = 4; // dp/sec

    @BindView(R.id.root) ConstraintLayout root;
    @BindView(R.id.iconIv) ImageView iconIv;
    @BindView(R.id.previousBt) Button previousBt;
    @BindView(R.id.playPauseBt) PlayerButton playPauseBt;
    @BindView(R.id.loadingPb) ProgressBar loadingPb;
    @BindView(R.id.nextBt) Button nextBt;
    @BindView(R.id.favoriteBt) Button favoriteBt;
    @BindView(R.id.metadataTv) TextView metadataTv;
    @BindView(R.id.titleTv) TextView titleTv;
    @BindView(R.id.bandTv) TextView bandTv;
    @BindView(R.id.sloganTv) TextView sloganTv;
    @BindView(R.id.descriptionTv) TextView descriptionTv;
    @BindView(R.id.genreTv) TextView genreTv;
    @BindView(R.id.locationTv) TextView locationTv;
    @BindView(R.id.websiteTv) TextView websiteTv;
    @BindView(R.id.emailTv) TextView emailTv;
    @BindView(R.id.phoneTv) TextView phoneTv;

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
        previousBt.setOnClickListener(v -> {
            AnimUtils.getBounceAnimation(previousBt, -NAV_BTN_VELOCITY).start();
            presenter.skipToPrevious();
        });
        nextBt.setOnClickListener(v -> {
            AnimUtils.getBounceAnimation(nextBt, NAV_BTN_VELOCITY).start();
            presenter.skipToNext();
        });
        favoriteBt.setOnClickListener(v -> {
            favAnimate();
            presenter.switchFavorite();
        });
        iconIv.setOnClickListener(v -> presenter.stationIconClick());

        playPauseBt.setManualMode(true);
        loadingPb.getIndeterminateDrawable().mutate().setColorFilter(getResources()
                .getColor(R.color.grey_50), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void setStation(Station station) {
        animateStationInfoLayout();

        ImageUtils.loadImageInto(iconIv, station);

        setTextOrHideIfEmpty(titleTv, station.name);
        setTextOrHideIfEmpty(bandTv, station.bandString);
        setTextOrHideIfEmpty(sloganTv, station.slogan);
        setTextOrHideIfEmpty(descriptionTv, station.description);
        setTextOrHideIfEmpty(genreTv, station.genre);
        setTextOrHideIfEmpty(websiteTv, station.websiteUrl);
        setTextOrHideIfEmpty(emailTv, station.email);
        setTextOrHideIfEmpty(phoneTv, station.phone);
        setTextOrHideIfEmpty(locationTv, station.locationString);

    }

    @Override
    public void setFavorite(boolean isFavorite) {
        favoriteBt.setBackgroundResource(isFavorite ? R.drawable.ic_star : R.drawable.ic_star_empty);
    }

    @Override
    public void setMetadata(String string) {
        metadataTv.setText(string);
    }

    @Override
    public void showLoading() {
        playPauseBt.setPlaying(true);
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

    private void favAnimate() {
        AnimUtils.getScaleXAnimation(favoriteBt, FAV_VELOCITY).start();
        AnimUtils.getScaleYAnimation(favoriteBt, FAV_VELOCITY).start();
    }

    private void animateStationInfoLayout() {
        ChangeBounds transition = new ChangeBounds();
        // TODO: 6/30/18 check duration
        transition.setDuration(200);
        transition.addTarget(root);
        transition.setInterpolator(new FastOutSlowInInterpolator());
        TransitionManager.beginDelayedTransition(root, transition);
    }

    private void setTextOrHideIfEmpty(TextView tv, String text) {
        if (text == null || text.isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }
}
