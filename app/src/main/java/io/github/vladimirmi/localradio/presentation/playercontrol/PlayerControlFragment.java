package io.github.vladimirmi.localradio.presentation.playercontrol;

import android.view.View;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.presentation.core.BaseFragment;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public class PlayerControlFragment extends BaseFragment<PlayerControlPresenter> {

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

    }
}
