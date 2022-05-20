package com.example.configurator_pc.ui.community;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/* renamed from: com.example.configurator_pc.ui.community.CommunityViewModel */
public class CommunityViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public CommunityViewModel() {
        MutableLiveData<String> mutableLiveData = new MutableLiveData<>();
        this.mText = mutableLiveData;
        mutableLiveData.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return this.mText;
    }
}
