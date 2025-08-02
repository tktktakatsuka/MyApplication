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


    private MutableLiveData<List<String>> joNames = new MutableLiveData<>();
    public void setJoNames(List<String> joNamesList) {
        joNames.setValue(joNamesList);
    }
    public LiveData<List<String>> getJoNames() {
        return joNames;
    }
}

