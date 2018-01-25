package io.goulao.morse.view;

import android.content.Context;

/**
 * Created by NB21761 on 24/01/2018.
 */

public interface BaseView {
    Context context();

    boolean isVisible();

    void showLoading();

    void hideLoading();

    void showError(String message);

    void hideError();

    void onlineMode();

    void offlineMode();
}
