package com.prts.arkmusic.ui.ep;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EpViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EpViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is ep fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}