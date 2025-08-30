package com.tktkcompany.kakoRaceKeiba.dto;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class SharedViewModel extends ViewModel {
    // 既存のデータ
    private final MutableLiveData<List<String>> sharedData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> joNames = new MutableLiveData<>();

    // ★★★ 1. UIDを保持するためのLiveDataを追加 (型をStringに) ★★★
    private final MutableLiveData<String> uid = new MutableLiveData<>();

    // --- 既存のメソッド ---
    public void setSharedData(List<String> data) {
        sharedData.setValue(data);
    }
    public LiveData<List<String>> getSharedData() {
        return sharedData;
    }
    public void setJoNames(List<String> joNamesList) {
        joNames.setValue(joNamesList);
    }
    public LiveData<List<String>> getJoNames() {
        return joNames;
    }

    // ★★★ 2. UIDを操作するためのセッターとゲッターを追加 ★★★
    public void setUid(String userId) {
        uid.setValue(userId);
    }
    public LiveData<String> getUid() {
        return uid;
    }
}