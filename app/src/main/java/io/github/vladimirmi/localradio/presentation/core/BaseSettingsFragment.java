package io.github.vladimirmi.localradio.presentation.core;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.ResolvableApiException;
import com.tbruyelle.rxpermissions2.Permission;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import io.github.vladimirmi.localradio.R;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 31.10.2018.
 */
abstract public class BaseSettingsFragment extends PreferenceFragmentCompat implements BaseView {

    private FragmentBaseViewDelegate baseViewDelegate;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        baseViewDelegate = new FragmentBaseViewDelegate(this);
    }

    // TODO: 31.10.18 workaround https://issuetracker.google.com/issues/111662669
    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen) {
            @SuppressLint("RestrictedApi")
            @Override
            public void onBindViewHolder(PreferenceViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                Preference preference = getItem(position);
                if (preference instanceof PreferenceCategory)
                    setPaddingToLayoutChildren(holder.itemView);
                else {
                    View iconFrame = holder.itemView.findViewById(R.id.icon_frame);
                    if (iconFrame != null) {
                        iconFrame.setVisibility(preference.getIcon() == null ? View.GONE : View.VISIBLE);
                    }
                }
            }
        };
    }

    private void setPaddingToLayoutChildren(View view) {
        if (!(view instanceof ViewGroup))
            return;
        ViewGroup viewGroup = (ViewGroup) view;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            setPaddingToLayoutChildren(viewGroup.getChildAt(i));
            viewGroup.setPaddingRelative(0, viewGroup.getPaddingTop(),
                    viewGroup.getPaddingEnd(), viewGroup.getPaddingBottom());
        }
    }

    //region =============== BaseView ==============

    @Override
    public boolean handleBackPress() {
        return baseViewDelegate.handleBackPress();
    }

    @Override
    public Observable<Permission> resolvePermissions(String... permissions) {
        return baseViewDelegate.resolvePermissions(permissions);
    }

    @Override
    public void showMessage(String message) {
        baseViewDelegate.showMessage(message);
    }

    @Override
    public void showMessage(int messageId) {
        baseViewDelegate.showMessage(messageId);
    }

    @Override
    public void resolveApiException(ResolvableApiException resolvable) {
        baseViewDelegate.resolveApiException(resolvable);
    }

    //endregion

    private boolean startIntent(Intent intent) {
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
            return true;
        }
        return false;
    }
}
