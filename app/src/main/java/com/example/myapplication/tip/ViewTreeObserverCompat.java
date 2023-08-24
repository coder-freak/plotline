package com.example.myapplication.tip;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.ViewTreeObserver;

public final class ViewTreeObserverCompat {

    static class ViewTreeObserverCompatBaseImpl {

        public void removeOnGlobalLayoutListener(ViewTreeObserver viewTreeObserver, ViewTreeObserver.OnGlobalLayoutListener victim) {
            viewTreeObserver.removeGlobalOnLayoutListener(victim);
        }
    }

    static class ViewTreeObserverCompatApi16Impl extends ViewTreeObserverCompatBaseImpl {

        @Override
        public void removeOnGlobalLayoutListener(ViewTreeObserver viewTreeObserver, ViewTreeObserver.OnGlobalLayoutListener victim) {
            viewTreeObserver.removeOnGlobalLayoutListener(victim);
        }
    }

    static final ViewTreeObserverCompatBaseImpl IMPL;
    static {
        final int version = Build.VERSION.SDK_INT;
        if (version >= VERSION_CODES.JELLY_BEAN) {
            IMPL = new ViewTreeObserverCompatApi16Impl();
        } else {
            IMPL = new ViewTreeObserverCompatBaseImpl();
        }
    }

    private ViewTreeObserverCompat() {
    }

    public static void removeOnGlobalLayoutListener(ViewTreeObserver viewTreeObserver, ViewTreeObserver.OnGlobalLayoutListener victim) {
        IMPL.removeOnGlobalLayoutListener(viewTreeObserver, victim);
    }
}
