/*
 * Copyright 2018 Pranav Pandey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pranavpandey.android.dynamic.support.activity;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.pranavpandey.android.dynamic.support.R;

/**
 * An activity extending {@link DynamicSystemActivity} to maintain state of
 * the widgets and fragments. It will be very useful while handling
 * orientation changes. It saves the current fragment state and reuses
 * it while a configuration change takes place.
 */
public abstract class DynamicStateActivity extends DynamicSystemActivity {

    /**
     * Minimum delay to restore the activity state.
     */
    protected static final int STATE_DELAY = 400;

    /**
     * Content fragment TAG key which will be used to find it
     * during the configuration changes.
     */
    protected static final String ADS_STATE_CONTENT_FRAGMENT_TAG =
            "ads_state_content_fragment_tag";

    /**
     * Status bar key to maintain its state.
     */
    protected static final String ADS_STATE_APP_BAR_COLLAPSED =
            "ads_state_app_bar_collapsed";

    /**
     * FAB key to maintain its state.
     */
    protected static final String ADS_STATE_FAB_VISIBLE = "ads_state_fab_visible";

    /**
     * Search key to maintain its state.
     */
    protected static final String ADS_STATE_SEARCH_VIEW_VISIBLE =
            "ads_state_search_view_visible";

    /**
     * FAB visibility constant for no change.
     */
    protected static final int ADS_VISIBILITY_FAB_NO_CHANGE = -1;

    /**
     * Content fragment used by this activity.
     */
    private Fragment mContentFragment;

    /**
     * Content fragment TAG which will be used to find it during
     * configuration changes.
     */
    private String mContentFragmentTag;

    /**
     * Current FAB visibility.
     */
    private int mFABVisibility;

    /**
     * {@code true} if the app bar is in collapsed state.
     */
    private boolean mAppBarCollapsed;

    /**
     * App bar off set change DynamicTutorialListener to identify whether it is in
     * the collapsed state or not.
     */
    protected AppBarLayout.OnOffsetChangedListener mAppBarStateListener =
    new AppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            mAppBarCollapsed = verticalOffset == 0;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                final int fragmentsCount =
                        getSupportFragmentManager().getBackStackEntryCount();
                if (fragmentsCount > 0) {
                    mContentFragmentTag = getSupportFragmentManager()
                            .getBackStackEntryAt(fragmentsCount - 1).getName();

                    if (mContentFragmentTag != null) {
                        mContentFragment = getSupportFragmentManager()
                                .findFragmentByTag(mContentFragmentTag);
                    }
                } else {
                    mContentFragment = getSupportFragmentManager()
                            .findFragmentById(R.id.ads_container);
                }
            }
        });

        if (savedInstanceState != null) {
            mFABVisibility = ADS_VISIBILITY_FAB_NO_CHANGE;
            mContentFragmentTag = savedInstanceState
                    .getString(ADS_STATE_CONTENT_FRAGMENT_TAG);
            mContentFragment = getSupportFragmentManager()
                    .findFragmentByTag(mContentFragmentTag);
        }
    }

    /**
     * @return The fragment container id so that the fragment can be
     * injected into that view.
     */
    protected abstract @IdRes int getFragmentContainerId();

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(
                ADS_STATE_CONTENT_FRAGMENT_TAG, mContentFragmentTag);
    }

    /**
     * Switch the content fragment use by this activity by using the supplied
     * fragment transaction.
     *
     * @param fragmentTransaction Customised fragment transaction to support
     *                            animations and more.
     * @param fragment Fragment to be used by this activity.
     * @param addToBackStack {@code true} to put previous fragment to back
     *                       stack.
     * @param tag Fragment tag to maintain the back stack.
     */
    protected void switchFragment(@NonNull FragmentTransaction fragmentTransaction,
                                  @NonNull Fragment fragment, boolean addToBackStack,
                                  @Nullable String tag) {
        tag = tag != null ? tag : fragment.getClass().getSimpleName();
        if (getSupportFragmentManager().findFragmentByTag(tag) != null) {
            fragment = getSupportFragmentManager().findFragmentByTag(tag);
        }

        fragmentTransaction.replace(getFragmentContainerId(), fragment, tag);
        if (addToBackStack && mContentFragment != null) {
            fragmentTransaction.addToBackStack(tag);
        } else {
            getSupportFragmentManager().popBackStack(
                    null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        commitFragmentTransaction(fragmentTransaction);
        setContentFragment(fragment, tag);
    }

    private void commitFragmentTransaction(@NonNull FragmentTransaction fragmentTransaction) {
        try {
            fragmentTransaction.commit();
        } catch (Exception e) {
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    /**
     * Switch the content fragment use by this activity.
     *
     * @param fragment Fragment to be used by this activity.
     * @param addToBackStack {@code true} to put previous fragment to back
     *                       stack.
     * @param tag Fragment tag to maintain the back stack.
     */
    protected void switchFragment(@NonNull Fragment fragment, boolean addToBackStack,
                                  @Nullable String tag) {
        FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();

        switchFragment(fragmentTransaction, fragment, addToBackStack, tag);
    }

    /**
     * Switch the content fragment use by this activity.
     *
     * @param fragment Fragment to be used by this activity.
     * @param addToBackStack {@code true} to put previous fragment to back stack.
     */
    protected void switchFragment(@NonNull Fragment fragment, boolean addToBackStack) {
        switchFragment(fragment, addToBackStack, null);
    }

    /**
     * Getter for {@link #mContentFragment}.
     */
    public @Nullable Fragment getContentFragment() {
        return mContentFragment;
    }

    /**
     * Set the current content fragment.
     *
     * @param fragment Content fragment.
     * @param tag Content fragment tag.
     */
    public void setContentFragment(Fragment fragment, @NonNull String tag) {
        this.mContentFragment = fragment;
        this.mContentFragmentTag = tag;
    }

    /**
     * Getter for {@link #mFABVisibility}.
     */
    public int getFABVisibility() {
        return mFABVisibility;
    }

    /**
     * Setter for {@link #mFABVisibility}.
     */
    public void setFABVisibility(int visibility) {
        this.mFABVisibility = visibility;
    }

    /**
     * Getter for {@link #mAppBarCollapsed}.
     */
    public boolean isAppBarCollapsed() {
        return mAppBarCollapsed;
    }
}
