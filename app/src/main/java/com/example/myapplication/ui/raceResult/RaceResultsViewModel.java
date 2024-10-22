package com.example.myapplication.ui.raceResult;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RaceResultsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public RaceResultsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is raceResults fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}