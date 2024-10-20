package com.example.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MyDatabaseManager {

    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public MyDatabaseManager(Context context) {
        dbHelper = new MyDatabaseHelper(context);
    }

    // データベースのオープン
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    // データベースのクローズ
    public void close() {
        dbHelper.close();
    }

    // データをインサートするメソッド
    public void insertData(String raceNumber,
                           String kaisaibi, String horseNumber, String horseName, String jockey, String winOdds) {
        ContentValues values = new ContentValues();
        values.put("raceNumber", raceNumber);
        values.put("kaisaibi", kaisaibi);
        values.put("horseNumber", horseNumber);
        values.put("horseName", horseName);
        values.put("jockey", jockey);
        values.put("winOdds", winOdds);

        // テーブル名は"my_table"、nullColumnHackはnull
        database.insert("raceCard", null, values);
    }

    // データをインサートするメソッド
    public void raceResultInsertData(String kaisaibi,String raceNo,String tyaku,
                                     String waku, String horseNumber, String horseName, String age, String weight,
                                     String jockey, String popular, String winOdds, String time, String tyakusa,
                                     String tuukazyun, String nobori, String tyoukyousi, String horseWeight) {
        ContentValues values = new ContentValues();
        values.put("kaisaibi", kaisaibi);
        values.put("raceNo", raceNo);
        values.put("tyaku", tyaku);
        values.put("waku", waku);
        values.put("horseNumber", horseNumber);
        values.put("horseName", horseName);
        values.put("age", age);
        values.put("weight", weight);
        values.put("jockey", jockey);
        values.put("popular", popular);
        values.put("winOdds", winOdds);
        values.put("time", time);
        values.put("tyakusa", tyakusa);
        values.put("tuukazyun", tuukazyun);
        values.put("nobori", nobori);
        values.put("tyoukyousi", tyoukyousi);
        values.put("horseWeight", horseWeight);


        // テーブル名は"my_table"、nullColumnHackはnull
        database.insert("raceResult", null, values);
    }
}
