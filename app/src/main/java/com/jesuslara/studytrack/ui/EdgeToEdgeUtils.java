package com.jesuslara.studytrack.ui;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public final class EdgeToEdgeUtils {

    private EdgeToEdgeUtils() {
    }

    public static void enable(Activity activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
    }

    public static void applyInsetsAsPadding(View view,
                                            boolean applyTop,
                                            boolean applyBottom,
                                            boolean applyStart,
                                            boolean applyEnd) {
        final int initialStart = view.getPaddingStart();
        final int initialTop = view.getPaddingTop();
        final int initialEnd = view.getPaddingEnd();
        final int initialBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (target, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int start = initialStart + (applyStart ? systemBars.left : 0);
            int top = initialTop + (applyTop ? systemBars.top : 0);
            int end = initialEnd + (applyEnd ? systemBars.right : 0);
            int bottom = initialBottom + (applyBottom ? systemBars.bottom : 0);

            target.setPaddingRelative(start, top, end, bottom);
            return insets;
        });

        ViewCompat.requestApplyInsets(view);
    }
}
