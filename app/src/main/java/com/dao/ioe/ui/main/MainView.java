package com.dao.ioe.ui.main;

import android.content.Context;

public interface MainView {

    public Context getContext();

    public void showError(String msg);
}
