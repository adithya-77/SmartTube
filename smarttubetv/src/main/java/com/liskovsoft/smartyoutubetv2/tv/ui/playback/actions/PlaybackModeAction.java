package com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.PlaybackControlsRow.MultiAction;

import com.liskovsoft.smartyoutubetv2.common.app.models.playback.manager.PlayerConstants;
import com.liskovsoft.smartyoutubetv2.tv.R;

/**
 * An action for displaying two repeat states: none and all.
 */
public class PlaybackModeAction extends MultiAction {
    private static final int INDEX_NONE = PlayerConstants.PLAYBACK_MODE_CLOSE;
    private static final int INDEX_ONE = PlayerConstants.PLAYBACK_MODE_ONE;
    private static final int INDEX_ALL = PlayerConstants.PLAYBACK_MODE_ALL;
    private static final int INDEX_PAUSE = PlayerConstants.PLAYBACK_MODE_PAUSE;
    private static final int INDEX_LIST = PlayerConstants.PLAYBACK_MODE_LIST;
    private static final int INDEX_SHUFFLE = PlayerConstants.PLAYBACK_MODE_SHUFFLE;
    private static final int INDEX_REVERSE_LIST = PlayerConstants.PLAYBACK_MODE_REVERSE_LIST;
    private static final int INDEX_LOOP_LIST = PlayerConstants.PLAYBACK_MODE_LOOP_LIST;
    private final Context mContext;

    /**
     * Constructor
     * @param context Context used for loading resources.
     */
    public PlaybackModeAction(Context context) {
        this(context, ActionHelpers.getIconHighlightColor(context));
    }

    /**
     * Constructor
     * @param context Context used for loading resources
     * @param selectionColor Color to display the repeat-all icon.
     */
    public PlaybackModeAction(Context context, int selectionColor) {
        super(R.id.action_repeat);

        mContext = context;
        Drawable[] drawables = new Drawable[7];
        Drawable repeatNoneDrawable = ContextCompat.getDrawable(context, R.drawable.action_mode_none);
        Drawable repeatOneDrawable = ContextCompat.getDrawable(context, R.drawable.action_mode_one);
        Drawable repeatAllDrawable = ContextCompat.getDrawable(context, R.drawable.action_mode_all);
        Drawable repeatPauseDrawable = ContextCompat.getDrawable(context, R.drawable.action_mode_pause);
        Drawable repeatListDrawable = ContextCompat.getDrawable(context, R.drawable.action_mode_list);
        Drawable repeatShuffleDrawable = ContextCompat.getDrawable(context, R.drawable.action_mode_shuffle);
        Drawable reverseListDrawable = ContextCompat.getDrawable(context, R.drawable.action_mode_reverse_list);
        
        // Handle both BitmapDrawable and VectorDrawable
        drawables[INDEX_NONE] = createDrawableWithColor(context, repeatNoneDrawable, selectionColor);
        drawables[INDEX_ONE] = createDrawableWithColor(context, repeatOneDrawable, selectionColor);
        drawables[INDEX_ALL] = createDrawableWithColor(context, repeatAllDrawable, selectionColor);
        drawables[INDEX_PAUSE] = createDrawableWithColor(context, repeatPauseDrawable, selectionColor);
        drawables[INDEX_LIST] = createDrawableWithColor(context, repeatListDrawable, selectionColor);
        drawables[INDEX_SHUFFLE] = createDrawableWithColor(context, repeatShuffleDrawable, selectionColor);
        drawables[INDEX_REVERSE_LIST] = createDrawableWithColor(context, reverseListDrawable, selectionColor);
        setDrawables(drawables);

        String[] labels = new String[drawables.length];
        // Note, labels denote the action taken when clicked
        labels[INDEX_NONE] = context.getString(R.string.repeat_mode_none);
        labels[INDEX_ONE] = context.getString(R.string.repeat_mode_one);
        labels[INDEX_ALL] = context.getString(R.string.repeat_mode_all);
        labels[INDEX_PAUSE] = context.getString(R.string.repeat_mode_pause);
        labels[INDEX_LIST] = context.getString(R.string.repeat_mode_pause_alt);
        labels[INDEX_SHUFFLE] = context.getString(R.string.repeat_mode_shuffle);
        labels[INDEX_REVERSE_LIST] = context.getString(R.string.repeat_mode_reverse_list);
        setLabels(labels);
    }

    //@Override
    //public void setLabels(String[] labels) {
    //    for (int i = 0; i < labels.length; i++) {
    //        if (labels[i] != null) {
    //            labels[i] = Utils.updateTooltip(mContext, labels[i]);
    //        }
    //    }
    //
    //    super.setLabels(labels);
    //}
    
    /**
     * Helper method to create drawable with color, handling both BitmapDrawable and VectorDrawable
     */
    private Drawable createDrawableWithColor(Context context, Drawable drawable, int color) {
        if (drawable == null) {
            return null;
        }
        
        if (drawable instanceof BitmapDrawable) {
            // Handle bitmap drawables
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            return ActionHelpers.createDrawable(context, bitmapDrawable, color);
        } else {
            // Handle vector drawables and other drawable types
            Drawable coloredDrawable = drawable.mutate();
            if (color != 0) {
                coloredDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            }
            return coloredDrawable;
        }
    }
}
