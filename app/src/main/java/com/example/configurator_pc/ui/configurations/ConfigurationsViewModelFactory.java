package com.example.configurator_pc.ui.configurations;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ConfigurationsViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public ConfigurationsViewModelFactory(Application application2) {
        this.application = application2;
    }

    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> aClass) throws IllegalArgumentException {
        if (aClass == ConfigurationsViewModel.class) {
            return (T) new ConfigurationsViewModel(this.application);
        }
        throw new IllegalArgumentException("Argument class is not ConfigurationsViewModel");
    }
}
