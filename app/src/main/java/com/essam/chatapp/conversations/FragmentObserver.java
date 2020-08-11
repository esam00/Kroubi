package com.essam.chatapp.conversations;

import java.util.Observable;

public class FragmentObserver extends Observable {
    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }
}
