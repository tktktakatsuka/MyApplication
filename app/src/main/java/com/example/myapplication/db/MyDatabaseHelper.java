package com.example.myapplication.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    // データベースの名前とバージョンを指定
    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 1;

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // テーブル作成用のSQL文
    private static final String RACECARDTABLE_CREATE =
            "CREATE TABLE raceCard (" +
                    "raceNumber TEXT ," +
                    "kaisaibi TEXT ," +
                    "horseNumber TEXT ," +
                    "horseName TEXT, " +
                    "jockey TEXT," +
                    "winOdds TEXT);" ;

    // テーブル作成用のSQL文
    private static final String RACERESULTABLE_CREATE =
            "CREATE TABLE raceResult (" +
                    "kaisaibi TEXT ," +
                    "kaisaijo TEXT ," +
                    "RaceNo TEXT ," +
                    "tyaku TEXT ," +
                    "waku TEXT ," +
                    "horseNumber TEXT ," +
                    "horseName TEXT, " +
                    "age TEXT," +
                    "weight TEXT," +
                    "jockey TEXT," +
                    "popular TEXT," +
                    "winOdds TEXT," +
                    "time TEXT," +
                    "tyakusa TEXT," +
                    "tuukazyun TEXT," +
                    "nobori TEXT," +
                    "tyoukyousi TEXT," +
                    "horseWeight TEXT"+ ")";
//                    "PRIMARY KEY (kaisaibi, kaisaijo, RaceNo, horseName));" ;

    @Override
    public void onCreate(SQLiteDatabase db) {
        // データベースが初めて作成されるときに呼び出されます
        db.execSQL(RACECARDTABLE_CREATE);
        db.execSQL(RACERESULTABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // データベースのアップグレードが必要なときに呼び出されます
        db.execSQL("DROP TABLE IF EXISTS raceCard");
        db.execSQL("DROP TABLE IF EXISTS raceResult");
        onCreate(db);
    }


}
