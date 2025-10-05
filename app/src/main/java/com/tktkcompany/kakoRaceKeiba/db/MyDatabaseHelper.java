package com.tktkcompany.kakoRaceKeiba.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    // データベースの名前とバージョンを指定
    private static final String DATABASE_NAME = "mydb.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MEMO = "Memo";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    // テーブル作成用のSQL文
    private static final String MEMO_CREATE =
            "CREATE TABLE IF NOT EXISTS  memo (" +
                    "horseName TEXT ," +
                    "title TEXT ," +
                    "kaisaibi TEXT ," +
                    "comment TEXT ," +
                    "babaCondition TEXT ," +
                    "distance TEXT ," +
                    "kaisaijo TEXT);";

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
                    "horseWeight TEXT," +
                    "raceTitle TEXT," +
                    "hassouTime TEXT," +
                    "PRIMARY KEY (kaisaiBi, horseName))";

    // テーブル作成用のSQL文
    private static final String EXECUTER_CREATE =
            "CREATE TABLE EXECUTER (" +
                    "flg1 TEXT )";

    @Override
    public void onCreate(SQLiteDatabase db) {
        // データベースが初めて作成されるときに呼び出されます
        db.execSQL(MEMO_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // データベースのアップグレードが必要なときに呼び出されます
        onCreate(db);
        // 必要に応じてテーブル再作成
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMO);
        onCreate(db);
    }


}
