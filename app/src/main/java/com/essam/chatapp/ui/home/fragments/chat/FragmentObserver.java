package com.essam.chatapp.ui.home.fragments.chat;

import java.util.Observable;

public class FragmentObserver extends Observable {
    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }
}
