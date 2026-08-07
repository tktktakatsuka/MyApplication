package com.tktkcompany.kakoRaceKeiba.db;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private static final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    /**
     * データを作成・追加する
     * @param path Firebase Realtime Databaseのパス
     * @param data 格納するデータ (Map形式)
     */
    public static void createData(String path, Map<String, Object> data) {
        mDatabase.child(path).push().setValue(data)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Data successfully created at: " + path);
                })
                .addOnFailureListener(e -> {
                    System.err.println("Failed to create data: " + e.getMessage());
                });
    }

    /**
     * データを読み取る
     * @param path Firebase Realtime Databaseのパス
     * @param listener データ取得時のコールバック
     */
    public static void readData(String path, ValueEventListener listener) {
        mDatabase.child(path).addListenerForSingleValueEvent(listener);
    }

    /**
     * データを更新する
     * @param path Firebase Realtime Databaseのパス
     * @param updates 更新するデータ (Map形式)
     */
    public static void updateData(String path, Map<String, Object> updates) {
        mDatabase.child(path).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Data successfully updated at: " + path);
                })
                .addOnFailureListener(e -> {
                    System.err.println("Failed to update data: " + e.getMessage());
                });
    }

    /**
     * データを削除する
     * @param path Firebase Realtime Databaseのパス
     */
    public static void deleteData(String path) {
        mDatabase.child(path).removeValue()
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Data successfully deleted at: " + path);
                })
                .addOnFailureListener(e -> {
                    System.err.println("Failed to delete data: " + e.getMessage());
                });
    }

    /**
     * データの取得
     * @param path Firebase Realtime Databaseのパス
     * @param childKey 検索条件のキー
     * @param value 検索する値
     * @param listener データ取得時のコールバック
     */
    public static void queryData(String path, String childKey, String value, ValueEventListener listener) {
        Query query = mDatabase.child(path).orderByChild(childKey);
        query.addListenerForSingleValueEvent(listener);
    }

    /**
     * 条件付きデータの取得
     * @param path Firebase Realtime Databaseのパス
     * @param childKey 検索条件のキー
     * @param value 検索する値
     * @param listener データ取得時のコールバック
     */
    public static void queryDataAddWhere(String path, String childKey, boolean value, ValueEventListener listener) {
        Query query = mDatabase.child(path).orderByChild(childKey).equalTo(value);
        query.addListenerForSingleValueEvent(listener);
    }

    /**
     * 指定したキーが値と一致するデータを取得する
     */
    public static void queryDataEqualTo(String path, String childKey, String value, ValueEventListener listener) {
        Query query = mDatabase.child(path).orderByChild(childKey).equalTo(value);
        query.addListenerForSingleValueEvent(listener);
    }



    public static void raceResultInsertDatatoFirebase(String kaisaibi, String kaisaijo, String raceNo, String tyaku,
                                               String waku, String horseNumber, String horseName, String age, String weight,
                                               String jockey, String popular, String winOdds, String time, String tyakusa,
                                               String tuukazyun, String nobori, String tyoukyousi, String horseWeight,String raceTitle, String hassouTime) {
        Map<String, Object> raseResult = new HashMap<>();
        raseResult.put("kaisaibi", kaisaibi);
        raseResult.put("kaisaijo", kaisaijo);
        raseResult.put("raceNo", raceNo);
        raseResult.put("tyaku", tyaku);
        raseResult.put("waku", waku);
        raseResult.put("horseNumber", horseNumber);
        raseResult.put("horseName", horseName);
        raseResult.put("age", age);
        raseResult.put("weight", weight);
        raseResult.put("jockey", jockey);
        raseResult.put("popular", popular);
        raseResult.put("winOdds", winOdds);
        raseResult.put("time", time);
        raseResult.put("tyakusa", tyakusa);
        raseResult.put("tuukazyun", tuukazyun);
        raseResult.put("nobori", nobori);
        raseResult.put("tyoukyousi", tyoukyousi);
        raseResult.put("horseWeight", horseWeight);
        raseResult.put("raceTitle", raceTitle);
        raseResult.put("hassouTime", hassouTime);
        FirebaseManager.createData("raceResult" + "/" + kaisaijo +"/" , raseResult);
    }
}
