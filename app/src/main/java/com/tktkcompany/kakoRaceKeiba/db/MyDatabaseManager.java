package com.tktkcompany.kakoRaceKeiba.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

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
    public void raceResultInsertData(String kaisaibi, String kaisaijo, String raceNo, String tyaku,
                                     String waku, String horseNumber, String horseName, String age, String weight,
                                     String jockey, String popular, String winOdds, String time, String tyakusa,
                                     String tuukazyun, String nobori, String tyoukyousi, String horseWeight,String raceTitle, String hassouTime) {
        ContentValues values = new ContentValues();
        values.put("kaisaibi", kaisaibi);
        values.put("kaisaijo", kaisaijo);
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
        values.put("raceTitle", raceTitle);
        values.put("hassouTime", hassouTime);
        // テーブル名は"my_table"、nullColumnHackはnull
        database.insert("raceResult", null, values);
    }

    // データをインサートするメソッド
    public void executerInsertData(String flg1) {
        // テーブル名は"my_table"、nullColumnHackはnull
        ContentValues values = new ContentValues();
        values.put("flg1", flg1);
        database.insert("EXECUTER", null, values);
    }

    // レース結果を取得するメソッド
    public List<String> getRaceResults(String kaisaibi, String raceNumber, String kaisaijo) {
        // SQL SELECT文
        String[] columns = {"kaisaibi", "kaisaijo", "RaceNo", "tyaku", "waku", "horseNumber", "horseName", "age", "winOdds", "time", "jockey", "popular","raceTitle","hassouTime"};

        String selection;
        String[] selectionArgs;
        // WHERE句の条件を設定
        if (!"".equals(kaisaibi)) {
            selection = "kaisaibi = ? AND RaceNo =? AND kaisaijo=?";
            selectionArgs = new String[]{kaisaibi, raceNumber, kaisaijo}; // ここで引数を渡します
        } else {
            selection = null;
            selectionArgs =null;
        }

        Cursor cursor = database.query("raceResult", columns, selection, selectionArgs, null, null, "tyaku ASC");
        List<String> list = new ArrayList<String>();

        // カーソルが空でないか確認
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String tyaku = cursor.getString(cursor.getColumnIndexOrThrow("tyaku"));
                String waku = cursor.getString(cursor.getColumnIndexOrThrow("waku"));
                String horseName = cursor.getString(cursor.getColumnIndexOrThrow("horseName"));
                String age = cursor.getString(cursor.getColumnIndexOrThrow("age"));
                String jockey = cursor.getString(cursor.getColumnIndexOrThrow("jockey"));
                String popular = cursor.getString(cursor.getColumnIndexOrThrow("popular"));
                String winOdds = cursor.getString(cursor.getColumnIndexOrThrow("winOdds"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String racetitle = cursor.getString(cursor.getColumnIndexOrThrow("raceTitle"));
                String hassouTime = cursor.getString(cursor.getColumnIndexOrThrow("hassouTime"));
                list.add(tyaku);
                list.add(waku);
                list.add(horseName);
                list.add(age);
                list.add(jockey);
                list.add(popular);
                list.add(winOdds);
                list.add(time);
                list.add(racetitle);
                list.add(hassouTime);

            } while (cursor.moveToNext());
        }
        // カーソルを閉じる
        if (cursor != null) {
            cursor.close();
        }

        return list;
    }



    // レース結果を取得するメソッド
    public String getExecuter() {
        // SQL SELECT文
        String[] columns = {"flg1"};

        String selection;
        String[] selectionArgs;
        // WHERE句の条件を設定
        Cursor cursor = database.query("EXECUTER", columns, null, null, null, null, null);
        String flg1="";

        // カーソルが空でないか確認
        if (cursor != null && cursor.moveToFirst()) {
                flg1 = cursor.getString(cursor.getColumnIndexOrThrow("flg1"));
        }
        // カーソルを閉じる
        if (cursor != null) {
            cursor.close();
        }

        return flg1;
    }
}
