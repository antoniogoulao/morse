package io.goulao.morse.presenter;

import io.goulao.morse.view.BaseView;

/**
 * Created by NB21761 on 24/01/2018.
 */

public interface Presenter {
    void initWithView(BaseView view);
    void resume();
    void pause();
    void destroy();
}
