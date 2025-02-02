package com.tktkcompany.kakoRaceKeiba.dto;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<List<String>> sharedData = new MutableLiveData<>();

    public void setSharedData(List<String> data) {
        sharedData.setValue(data);
    }

    public LiveData<List<String>> getSharedData() {
        return sharedData;
    }
}

