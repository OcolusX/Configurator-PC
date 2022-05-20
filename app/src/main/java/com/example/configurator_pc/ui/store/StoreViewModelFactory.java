package com.example.configurator_pc.ui.store;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class StoreViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public StoreViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> aClass) throws IllegalArgumentException {
        if (aClass == StoreViewModel.class) {
            return (T) new StoreViewModel(this.application);
        }
        throw new IllegalArgumentException("Argument class is not StoreViewModel");
    }
}
