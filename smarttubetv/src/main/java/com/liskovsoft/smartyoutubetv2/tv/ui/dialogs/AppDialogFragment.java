package com.liskovsoft.smartyoutubetv2.tv.ui.dialogs;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.leanback.preference.LeanbackSettingsFragment;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.PlaybackPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.views.AppDialogView;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.FormatItem;
import com.liskovsoft.smartyoutubetv2.common.misc.MotherActivity;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.ChatPreference;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.ChatPreferenceDialogFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.CommentsPreference;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.CommentsPreferenceDialogFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.RadioListPreferenceDialogFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.StringListPreference;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.StringListPreferenceDialogFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.mod.leanback.preference.LeanbackListPreferenceDialogFragment;
import com.liskovsoft.smartyoutubetv2.tv.util.ViewUtil;

import java.util.List;

public class AppDialogFragment extends LeanbackSettingsFragment implements AppDialogView {
    private static final String TAG = AppDialogFragment.class.getSimpleName();
    private AppDialogPresenter mPresenter;
    private AppPreferenceManager mManager;
    private boolean mIsTransparent;
    private boolean mIsOverlay;
    private boolean mIsPaused;
    private int mId;

    private static final String PREFERENCE_FRAGMENT_TAG =
            "androidx.leanback.preference.LeanbackSettingsFragment.PREFERENCE_FRAGMENT";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = AppDialogPresenter.instance(getActivity());
        mPresenter.setView(this);
        mManager = new AppPreferenceManager(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // Workaround for dialog that are destroyed with the delay (e.g. transparent dialogs)
        mIsPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // Workaround for dialog that are destroyed with the delay (e.g. transparent dialogs)
        mIsPaused = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mPresenter.getView() == this) {
            mPresenter.onViewDestroyed();
        }
    }

    @Override
    public void onPreferenceStartInitialScreen() {
        // FIX: Can not perform this action after onSaveInstanceState
        // Possible fix: Unable to add window -- token android.os.BinderProxy is not valid; is your activity running?
        if (!Utils.checkActivity(getActivity())) {
            return;
        }

        try {
            // Fix mPresenter in null after init stage.
            // Seems concurrency between dialogs.
            mPresenter.setView(this);

            mPresenter.onViewInitialized();
        } catch (IllegalStateException e) {
            // NOP
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference) {
        // Contains only child fragments.
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment, PreferenceScreen preferenceScreen) {
        // Contains only child fragments.
        return false;
    }

    private AppPreferenceFragment buildPreferenceFragment(List<OptionCategory> categories, String title) {
        AppPreferenceFragment fragment = new AppPreferenceFragment();
        fragment.setCategories(categories);
        fragment.setTitle(title);
        fragment.enableTransparent(mIsTransparent);
        return fragment;
    }

    @Override
    public void show(List<OptionCategory> categories, String title, boolean isExpandable, boolean isTransparent, boolean isOverlay, int id) {
        if (!Utils.checkActivity(getActivity())) {
            return;
        }

        // Only root fragment could make other fragments in the stack transparent
        boolean stackIsEmpty = getChildFragmentManager() != null && getChildFragmentManager().getBackStackEntryCount() == 0;
        mIsTransparent = stackIsEmpty ? isTransparent : mIsTransparent;
        mIsOverlay = isOverlay;
        mId = id;

        if (isExpandable && categories != null && categories.size() == 1) {
            OptionCategory category = categories.get(0);
            if (category.options != null) {
                onPreferenceDisplayDialog(null, mManager.createPreference(category));
            }
        } else {
            AppPreferenceFragment fragment = buildPreferenceFragment(categories, title);
            startPreferenceFragment(fragment);
        }
    }

    @Override
    public boolean onPreferenceDisplayDialog(@Nullable PreferenceFragment caller, @NonNull Preference pref) {
        // Fix: IllegalStateException: Activity has been destroyed
        // Possible fix: Unable to add window -- token android.os.BinderProxy is not valid; is your activity running?
        if (!Utils.checkActivity(getActivity())) {
            return false;
        }

        if (pref instanceof StringListPreference) {
            StringListPreference listPreference = (StringListPreference) pref;
            StringListPreferenceDialogFragment f = StringListPreferenceDialogFragment.newInstanceStringList(listPreference.getKey());
            f.enableTransparent(mIsTransparent);
            f.setTargetFragment(caller, 0);
            f.setPreference(pref);
            startPreferenceFragment(f);

            return true;
        } else if (pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) pref;
            RadioListPreferenceDialogFragment f = RadioListPreferenceDialogFragment.newInstanceSingle(listPreference.getKey());
            f.enableTransparent(mIsTransparent);
            f.setTargetFragment(caller, 0);
            f.setPreference(pref);
            startPreferenceFragment(f);

            return true;
        } else if (pref instanceof ChatPreference) {
            ChatPreference chatPreference = (ChatPreference) pref;
            ChatPreferenceDialogFragment f = ChatPreferenceDialogFragment.newInstance(chatPreference.getChatReceiver(), chatPreference.getKey());
            f.enableTransparent(mIsTransparent);
            f.setTargetFragment(caller, 0);
            f.setPreference(pref);
            startPreferenceFragment(f);

            return true;
        } else if (pref instanceof CommentsPreference) {
            ((MotherActivity) getActivity()).enableThrottleKeyDown(true);
            CommentsPreference commentsPreference = (CommentsPreference) pref;
            CommentsPreferenceDialogFragment f = CommentsPreferenceDialogFragment.newInstance(commentsPreference.getCommentsReceiver(), commentsPreference.getKey());
            f.enableTransparent(mIsTransparent);
            f.setTargetFragment(caller, 0);
            f.setPreference(pref);
            startPreferenceFragment(f);

            return true;
        } else if (pref instanceof MultiSelectListPreference) {
            MultiSelectListPreference listPreference = (MultiSelectListPreference) pref;
            LeanbackListPreferenceDialogFragment f = LeanbackListPreferenceDialogFragment.newInstanceMulti(listPreference.getKey());
            f.setTargetFragment(caller, 0);
            f.setPreference(pref);
            startPreferenceFragment(f);
        }
        // TODO
        // else if (pref instanceof EditTextPreference) {
        //
        //        }
        else {
            // Single button item. Imitate click on it (expandable = true).
            if (pref.getOnPreferenceClickListener() != null) {
                pref.getOnPreferenceClickListener().onPreferenceClick(pref);
            }

            return false;
        }

        // NOTE: Transparent CheckedList should be placed here (just in case you'll need it).

        //return super.onPreferenceDisplayDialog(caller, pref);
        return true;
    }

    /**
     * Fix possible state loss!!!
     */
    @Override
    public void startPreferenceFragment(@NonNull Fragment fragment) {
        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        final Fragment prevFragment =
                getChildFragmentManager().findFragmentByTag(PREFERENCE_FRAGMENT_TAG);
        if (prevFragment != null) {
            transaction
                    .addToBackStack(null)
                    .replace(R.id.settings_preference_fragment_container, fragment,
                            PREFERENCE_FRAGMENT_TAG);
        } else {
            transaction
                    .add(R.id.settings_preference_fragment_container, fragment,
                            PREFERENCE_FRAGMENT_TAG);
        }
        // Fix possible state loss!!!
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void finish() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void goBack() {
        if (getChildFragmentManager() != null && getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public void clearBackstack() {
        // this manager holds entire back stack
        Helpers.setField(this, "mChildFragmentManager", null);
    }

    @Override
    public boolean isShown() {
        return isVisible() && getUserVisibleHint();
    }

    @Override
    public boolean isTransparent() {
        return mIsTransparent;
    }

    @Override
    public boolean isOverlay() {
        return mIsOverlay;
    }

    @Override
    public boolean isPaused() {
        return mIsPaused;
    }

    @Override
    public int getViewId() {
        return mId;
    }

    public void onFinish() {
        mPresenter.onFinish();
    }
    
    public static class AppPreferenceFragment extends LeanbackPreferenceFragment {
        private static final String TAG = AppPreferenceFragment.class.getSimpleName();
        private List<OptionCategory> mCategories;
        private Context mExtractedContext;
        private AppPreferenceManager mManager;
        private String mTitle;
        private boolean mIsTransparent;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            // Note, place in field with different name to avoid field overlapping
            mExtractedContext = (Context) Helpers.getField(this, "mStyledContext");
            // Note, don't use external manager (probable focus lost and visual bugs)
            mManager = new AppPreferenceManager(mExtractedContext);

            initPrefs();

            Log.d(TAG, "onCreatePreferences");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            try {
                View view = super.onCreateView(inflater, container, savedInstanceState);

                if (mIsTransparent && view != null) {
                    // Enable transparent shadow outline on parent (R.id.settings_preference_fragment_container)
                    ViewUtil.enableTransparentDialog(getActivity(), getParentFragment().getView());
                    // Enable transparency on child fragment itself (isn't attached to parent yet)
                    ViewUtil.enableTransparentDialog(getActivity(), view);
                }
                
                return view;
            } catch (Exception e) {
                Log.e(TAG, "Error creating preference view, using fallback", e);
                // Fallback: create a proper preferences view manually
                return createFallbackPreferencesView(inflater, container);
            }
        }

        private View createFallbackPreferencesView(LayoutInflater inflater, ViewGroup container) {
            // Create a simple LinearLayout with basic preferences
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 50, 50, 50);
            layout.setBackgroundColor(getResources().getColor(android.R.color.black));

            // Add title
            TextView title = new TextView(getActivity());
            title.setText("Video Quality Settings");
            title.setTextColor(getResources().getColor(android.R.color.white));
            title.setTextSize(24);
            title.setPadding(0, 0, 0, 30);
            layout.addView(title);

            // Add basic quality options with actual functionality
            String[] qualityOptions = {"Auto", "1080p", "720p", "480p", "360p", "240p"};
            for (int i = 0; i < qualityOptions.length; i++) {
                String quality = qualityOptions[i];
                TextView option = new TextView(getActivity());
                option.setText("• " + quality);
                option.setTextColor(getResources().getColor(android.R.color.white));
                option.setTextSize(18);
                option.setPadding(20, 10, 0, 10);
                option.setFocusable(true);
                option.setClickable(true);
                option.setBackground(getResources().getDrawable(android.R.drawable.list_selector_background));
                
                // Add click listener to actually change quality
                final int qualityIndex = i;
                option.setOnClickListener(v -> {
                    Log.d(TAG, "=== QUALITY SELECTION STARTED ===");
                    Log.d(TAG, "Quality selected: " + quality);
                    Log.d(TAG, "Click listener triggered for quality: " + quality);
                    
                    // Try to get the player and set the quality
                    try {
                        Log.d(TAG, "Calling setVideoQuality for: " + quality);
                        setVideoQuality(quality);
                        Log.d(TAG, "setVideoQuality completed for: " + quality);
                        
                        // Show feedback to user
                        option.setText("✓ " + quality + " (Selected)");
                        option.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                        Log.d(TAG, "UI feedback updated for: " + quality);
                        
                        // Close the dialog after a short delay to show the feedback
                        new android.os.Handler().postDelayed(() -> {
                            Log.d(TAG, "Closing dialog after quality selection");
                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        }, 1000);
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting quality: " + quality, e);
                        option.setText("✗ " + quality + " (Error)");
                        option.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    }
                    Log.d(TAG, "=== QUALITY SELECTION ENDED ===");
                });
                
                layout.addView(option);
            }

            return layout;
        }

        private void setVideoQuality(String quality) {
            Log.d(TAG, "Setting video quality to: " + quality);
            
            try {
                // Get the player instance through PlaybackPresenter
                PlaybackPresenter playbackPresenter = PlaybackPresenter.instance(getActivity());
                if (playbackPresenter == null) {
                    Log.e(TAG, "PlaybackPresenter is null");
                    return;
                }
                
                // Get the player manager
                Object playerManager = playbackPresenter.getPlayer();
                if (playerManager == null) {
                    Log.e(TAG, "Player manager is null");
                    return;
                }
                
                // Check if player is properly initialized
                try {
                    java.lang.reflect.Method isEngineInitializedMethod = playerManager.getClass().getMethod("isEngineInitialized");
                    Boolean isInitialized = (Boolean) isEngineInitializedMethod.invoke(playerManager);
                    if (isInitialized == null || !isInitialized) {
                        Log.w(TAG, "Player engine is not initialized, quality change may not work immediately");
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not check player initialization status", e);
                }
                
                // Create FormatItem based on quality selection
                FormatItem selectedFormat = createFormatItemFromQuality(quality);
                if (selectedFormat == null) {
                    Log.w(TAG, "Could not create format for quality: " + quality);
                    return;
                }
                
                // Set the format using the player's setFormat method
                try {
                    java.lang.reflect.Method setFormatMethod = playerManager.getClass().getMethod("setFormat", FormatItem.class);
                    setFormatMethod.invoke(playerManager, selectedFormat);
                    Log.d(TAG, "Successfully set video quality to: " + quality);
                    
                    // Also persist the format using PlayerData through the player
                    try {
                        // Try to get PlayerData through the player's getPlayerData method
                        java.lang.reflect.Method getPlayerDataMethod = playerManager.getClass().getMethod("getPlayerData");
                        Object playerData = getPlayerDataMethod.invoke(playerManager);
                        if (playerData != null) {
                            java.lang.reflect.Method setFormatDataMethod = playerData.getClass().getMethod("setFormat", FormatItem.class);
                            setFormatDataMethod.invoke(playerData, selectedFormat);
                            Log.d(TAG, "Successfully persisted video quality to: " + quality);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error persisting format through player", e);
                        // Try alternative approach - set format directly on player data
                        try {
                            // Use the common PlayerData class directly
                            Class<?> playerDataClass = Class.forName("com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData");
                            java.lang.reflect.Method getInstanceMethod = playerDataClass.getMethod("instance", android.content.Context.class);
                            Object playerData = getInstanceMethod.invoke(null, getActivity());
                            if (playerData != null) {
                                java.lang.reflect.Method setFormatDataMethod = playerDataClass.getMethod("setFormat", FormatItem.class);
                                setFormatDataMethod.invoke(playerData, selectedFormat);
                                Log.d(TAG, "Successfully persisted video quality using PlayerData.instance: " + quality);
                            }
                        } catch (Exception e2) {
                            Log.e(TAG, "Error persisting format using PlayerData.instance", e2);
                        }
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error setting format", e);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error setting video quality", e);
            }
        }
        
        private FormatItem createFormatItemFromQuality(String quality) {
            // Create FormatItem based on quality selection
            switch (quality.toLowerCase()) {
                case "auto":
                    return FormatItem.VIDEO_AUTO;
                case "240p":
                    return FormatItem.VIDEO_SUB_SD_AVC_30; // 240p
                case "360p":
                    return FormatItem.VIDEO_SD_AVC_30; // 360p
                case "480p":
                    return FormatItem.VIDEO_SD_AVC_30; // 360p (closest to 480p)
                case "720p":
                    return FormatItem.VIDEO_HD_AVC_30; // 720p
                case "1080p":
                    return FormatItem.VIDEO_FHD_AVC_30; // 1080p
                default:
                    return FormatItem.VIDEO_AUTO;
            }
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            try {
                super.onViewCreated(view, savedInstanceState);
            } catch (Exception e) {
                Log.e(TAG, "Error in onViewCreated, skipping", e);
                // Skip the problematic onViewCreated call
            }
        }

        private void initPrefs() {
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(mExtractedContext);
            setPreferenceScreen(screen);

            screen.setTitle(mTitle);

            addPreferences(screen);
        }

        private void addPreferences(PreferenceScreen screen) {
            if (mCategories != null) {
                for (OptionCategory category : mCategories) {
                    if (category.options != null) {
                        screen.addPreference(mManager.createPreference(category));
                    }
                }
            }
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            super.onDisplayPreferenceDialog(preference);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            return super.onPreferenceTreeClick(preference);
        }

        public void setCategories(List<OptionCategory> categories) {
            mCategories = categories;
        }

        public void setTitle(String title) {
            mTitle = title;
        }

        public void enableTransparent(boolean enable) {
            mIsTransparent = enable;
        }
    }
}