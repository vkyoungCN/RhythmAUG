package com.vkyoungcn.learningtools.myrhythm.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vkyoungcn.learningtools.myrhythm.models.Lyric;
import com.vkyoungcn.learningtools.myrhythm.models.PitchSequence;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;

public class MyRhythmDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MyRhythm.db";

    private volatile static MyRhythmDbHelper sMyRhythmDbHelper = null;

    //类内所有的DB操作均采用以下引用进行。通过同一的获取和关闭方法进行获取、关闭。
    private SQLiteDatabase mSQLiteDatabase;


    /* 建表语句*/
    //创建节奏表
    public static final String SQL_CREATE_RHYTHM =
            "CREATE TABLE " + MyRhythmContract.Rhythm.TABLE_NAME + " (" +
                    MyRhythmContract.Rhythm._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MyRhythmContract.Rhythm.COLUMN_TITLE + " TEXT, "+
                    MyRhythmContract.Rhythm.COLUMN_DESCRIPTION + " TEXT, "+
                    MyRhythmContract.Rhythm.COLUMN_C0DES + " TEXT, "+
                    MyRhythmContract.Rhythm.COLUMN_BEAT_TYPE + " INTEGER, "+

                    MyRhythmContract.Rhythm.COLUMN_STAR + " INTEGER, "+
                    MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN + " BOOLEAN, "+
                    MyRhythmContract.Rhythm.COLUMN_KEEP_TOP + " BOOLEAN, "+

                    MyRhythmContract.Rhythm.COLUMN_CREATE_TIME + " INTEGER, "+
                    MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME + " INTEGER, "+
                    MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID + " INTEGER, "+
                    MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID + " INTEGER, "+
                    MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID + " INTEGER)";

    //创建歌词表
    public static final String SQL_CREATE_LYRIC =
            "CREATE TABLE " + MyRhythmContract.Lyric.TABLE_NAME + " (" +
                    MyRhythmContract.Lyric._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MyRhythmContract.Lyric.COLUMN_TITLE + " TEXT, "+
                    MyRhythmContract.Lyric.COLUMN_DESCRIPTION + " TEXT, "+
                    MyRhythmContract.Lyric.COLUMN_WORDS + " TEXT, "+

                    MyRhythmContract.Lyric.COLUMN_STAR + " INTEGER, "+
                    MyRhythmContract.Lyric.COLUMN_SELF_DESIGN + " BOOLEAN, "+
                    MyRhythmContract.Lyric.COLUMN_KEEP_TOP + " BOOLEAN, "+

                    MyRhythmContract.Lyric.COLUMN_CREATE_TIME + " INTEGER, "+
                    MyRhythmContract.Lyric.COLUMN_LAST_MODIFY_TIME + " INTEGER)";

    //创建音高表
    public static final String SQL_CREATE_PITCHES =
            "CREATE TABLE " + MyRhythmContract.Pitches.TABLE_NAME + " (" +
                    MyRhythmContract.Pitches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MyRhythmContract.Pitches.COLUMN_TITLE + " TEXT, "+
                    MyRhythmContract.Pitches.COLUMN_DESCRIPTION + " TEXT, "+
                    MyRhythmContract.Pitches.COLUMN_PITCHES + " TEXT, "+

                    MyRhythmContract.Pitches.COLUMN_STAR + " INTEGER, "+
                    MyRhythmContract.Pitches.COLUMN_SELF_DESIGN + " BOOLEAN, "+
                    MyRhythmContract.Pitches.COLUMN_KEEP_TOP + " BOOLEAN, "+

                    MyRhythmContract.Pitches.COLUMN_CREATE_TIME + " INTEGER, "+
                    MyRhythmContract.Pitches.COLUMN_LAST_MODIFY_TIME + " INTEGER)";

    //创建操作记录表
    public static final String SQL_CREATE_ACTION_RECORDS =
            "CREATE TABLE " + MyRhythmContract.ActionRecord.TABLE_NAME + " (" +
                    MyRhythmContract.ActionRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MyRhythmContract.ActionRecord.COLUMN_ACTION_TIME + " INTEGER, "+
                    MyRhythmContract.ActionRecord.COLUMN_ACTION_TYPE + " INTEGER)";

    /* 删表语句*/
    private static final String SQL_DROP_RHYTHM =
            "DROP TABLE IF EXISTS " +  MyRhythmContract.Rhythm.TABLE_NAME;
    private static final String SQL_DROP_LYRIC =
            "DROP TABLE IF EXISTS " +  MyRhythmContract.Lyric.TABLE_NAME;
    private static final String SQL_DROP_PITCHES =
            "DROP TABLE IF EXISTS " +  MyRhythmContract.Pitches.TABLE_NAME;
    private static final String SQL_DROP_ACTION_RECORDS =
            "DROP TABLE IF EXISTS " +  MyRhythmContract.ActionRecord.TABLE_NAME;



    //构造器
    private MyRhythmDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        getWritableDatabaseIfClosedOrNull();
    }


    //DCL模式单例获取方法【双检查锁定？】
    public static MyRhythmDbHelper getInstance(Context context){
        if(sMyRhythmDbHelper == null){
            synchronized (MyRhythmDbHelper.class){
                if(sMyRhythmDbHelper == null){
                    sMyRhythmDbHelper = new MyRhythmDbHelper(context);
                }
            }
        }
        return sMyRhythmDbHelper;
    }


    /* 必要的覆写方法 */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //根据API，本方法在初次使用数据库时（比如初次请求生成mSQLiteDatabase）自动调用
        //建表
        db.execSQL(SQL_CREATE_RHYTHM);
        db.execSQL(SQL_CREATE_LYRIC);
        db.execSQL(SQL_CREATE_PITCHES);
        db.execSQL(SQL_CREATE_ACTION_RECORDS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // 使用for实现跨版本升级数据库【暂时用不到升级逻辑】
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {
               /* case 4:
                示范
                    db.execSQL("DROP TABLE IF EXISTS temp_old_group");
                    db.execSQL("DROP TABLE IF EXISTS "+YoMemoryContract.GroupCrossItem.TABLE_NAME + DEFAULT_ITEM_SUFFIX);
                    break;*/

                default:
                    break;
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        for (int i = oldVersion; i > newVersion; i--) {
            switch (i) {
                //没写。。
                default:
                    break;
            }
        }

    }


    /*CRUD部分*/
    public long createRhythm(Rhythm rhythm){
        long l;
        getWritableDatabaseIfClosedOrNull();
        ContentValues values = new ContentValues();

        values.put(MyRhythmContract.Rhythm.COLUMN_TITLE, rhythm.getTitle());
        values.put(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION, rhythm.getDescription());
        values.put(MyRhythmContract.Rhythm.COLUMN_BEAT_TYPE, rhythm.getRhythmType());
        values.put(MyRhythmContract.Rhythm.COLUMN_C0DES, rhythm.getStrRhythmCodeSerial());//以字串形式存放
        values.put(MyRhythmContract.Rhythm.COLUMN_STAR,rhythm.getStars());

        values.put(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN,rhythm.isSelfDesign());
        values.put(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP,rhythm.isKeepTop());
        values.put(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME,rhythm.getCreateTime());
        values.put(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME,rhythm.getLastModifyTime());
        values.put(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID,rhythm.getPrimaryLyricId());
        values.put(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID,rhythm.getSecondLyricId());
        values.put(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID,rhythm.getPitchesId());

        l = mSQLiteDatabase.insert(MyRhythmContract.Rhythm.TABLE_NAME, null, values);
        closeDB();

        return l;
    }


    public long createLyric(Lyric lyric){
        long l;
        getWritableDatabaseIfClosedOrNull();
        ContentValues values = new ContentValues();

        values.put(MyRhythmContract.Lyric.COLUMN_TITLE, lyric.getTitle());
        values.put(MyRhythmContract.Lyric.COLUMN_DESCRIPTION, lyric.getDescription());
        values.put(MyRhythmContract.Lyric.COLUMN_WORDS, lyric.getLyricSerial());//以字串形式存放
        values.put(MyRhythmContract.Lyric.COLUMN_STAR,lyric.getStars());

        values.put(MyRhythmContract.Lyric.COLUMN_SELF_DESIGN,lyric.isSelfDesign());
        values.put(MyRhythmContract.Lyric.COLUMN_KEEP_TOP,lyric.isKeepTop());
        values.put(MyRhythmContract.Lyric.COLUMN_CREATE_TIME,lyric.getCreateTime());
        values.put(MyRhythmContract.Lyric.COLUMN_LAST_MODIFY_TIME,lyric.getLastModifyTime());

        l = mSQLiteDatabase.insert(MyRhythmContract.Lyric.TABLE_NAME, null, values);
        closeDB();

        return l;
    }

    public long createPitch(PitchSequence pitchSequence){
        long l;
        getWritableDatabaseIfClosedOrNull();
        ContentValues values = new ContentValues();
        values.put(MyRhythmContract.Pitches.COLUMN_TITLE, pitchSequence.getTitle());
        values.put(MyRhythmContract.Pitches.COLUMN_DESCRIPTION, pitchSequence.getDescription());
        values.put(MyRhythmContract.Pitches.COLUMN_PITCHES, pitchSequence.getStrRhythmCodeSerial());//以字串形式存放
        values.put(MyRhythmContract.Pitches.COLUMN_STAR,pitchSequence.getStars());

        values.put(MyRhythmContract.Pitches.COLUMN_SELF_DESIGN,pitchSequence.isSelfDesign());
        values.put(MyRhythmContract.Pitches.COLUMN_KEEP_TOP,pitchSequence.isKeepTop());
        values.put(MyRhythmContract.Pitches.COLUMN_CREATE_TIME,pitchSequence.getCreateTime());
        values.put(MyRhythmContract.Pitches.COLUMN_LAST_MODIFY_TIME,pitchSequence.getLastModifyTime());

        l = mSQLiteDatabase.insert(MyRhythmContract.Pitches.TABLE_NAME, null, values);
        closeDB();

        return l;
    }


    /*
     * 要删除一个节奏，需要①节奏删除……没有其他操作
     * */
    public void deleteRhythmById(int rhythmId){
        String deleteSingleRhythmSql = "DELETE FROM "+ MyRhythmContract.Rhythm.TABLE_NAME+" WHERE "+
                MyRhythmContract.Rhythm._ID+" = "+rhythmId;
        mSQLiteDatabase.execSQL(deleteSingleRhythmSql);
        getWritableDatabaseIfClosedOrNull();
        closeDB();

    }

    public void deleteLyricById(int lyricId){
        String deleteSingleLyricSql = "DELETE FROM "+ MyRhythmContract.Lyric.TABLE_NAME+" WHERE "+
                MyRhythmContract.Lyric._ID+" = "+lyricId;
        mSQLiteDatabase.execSQL(deleteSingleLyricSql);
        getWritableDatabaseIfClosedOrNull();
        closeDB();

    }
    public void deletePitchesById(int pitchesId){
        String deletePitchesSql = "DELETE FROM "+ MyRhythmContract.Pitches.TABLE_NAME+" WHERE "+
                MyRhythmContract.Pitches._ID+" = "+pitchesId;
        mSQLiteDatabase.execSQL(deletePitchesSql);
        getWritableDatabaseIfClosedOrNull();
        closeDB();

    }

    /* 修改*/
    public int updateRhythmById(int rhythmId,Rhythm rhythm){
        int affectedRows = 0;
        getReadableDatabaseIfClosedOrNull();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_TITLE, rhythm.getTitle());
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION, rhythm.getDescription());
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_BEAT_TYPE, rhythm.getRhythmType());
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_C0DES, rhythm.getStrRhythmCodeSerial());//以字串形式存放
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_STAR,rhythm.getStars());

        contentValues.put(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN,rhythm.isSelfDesign());
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP,rhythm.isKeepTop());
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME,rhythm.getCreateTime());
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME,rhythm.getLastModifyTime());
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID,rhythm.getPrimaryLyricId());
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID,rhythm.getSecondLyricId());
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID,rhythm.getPitchesId());

        affectedRows = mSQLiteDatabase.update(MyRhythmContract.Rhythm.TABLE_NAME,contentValues,
                MyRhythmContract.Rhythm._ID+" = ? ",new String[]{String.valueOf(rhythmId)} );
        closeDB();
        return affectedRows;
    }

    public int updateLyricById(int lyricId,Lyric lyric){
        int affectedRows = 0;
        getReadableDatabaseIfClosedOrNull();

        ContentValues values = new ContentValues();

        values.put(MyRhythmContract.Lyric.COLUMN_TITLE, lyric.getTitle());
        values.put(MyRhythmContract.Lyric.COLUMN_DESCRIPTION, lyric.getDescription());
        values.put(MyRhythmContract.Lyric.COLUMN_WORDS, lyric.getLyricSerial());//以字串形式存放
        values.put(MyRhythmContract.Lyric.COLUMN_STAR,lyric.getStars());

        values.put(MyRhythmContract.Lyric.COLUMN_SELF_DESIGN,lyric.isSelfDesign());
        values.put(MyRhythmContract.Lyric.COLUMN_KEEP_TOP,lyric.isKeepTop());
        values.put(MyRhythmContract.Lyric.COLUMN_CREATE_TIME,lyric.getCreateTime());
        values.put(MyRhythmContract.Lyric.COLUMN_LAST_MODIFY_TIME,lyric.getLastModifyTime());

        affectedRows = mSQLiteDatabase.update(MyRhythmContract.Lyric.TABLE_NAME,values,
                MyRhythmContract.Lyric._ID+" = ? ",new String[]{String.valueOf(lyricId)} );
        closeDB();
        return affectedRows;
    }


    public int updatePitchesById(int pitchesId,PitchSequence pitchSequence){
        int affectedRows = 0;
        getReadableDatabaseIfClosedOrNull();

        ContentValues values = new ContentValues();
        values.put(MyRhythmContract.Pitches.COLUMN_TITLE, pitchSequence.getTitle());
        values.put(MyRhythmContract.Pitches.COLUMN_DESCRIPTION, pitchSequence.getDescription());
        values.put(MyRhythmContract.Pitches.COLUMN_PITCHES, pitchSequence.getStrRhythmCodeSerial());//以字串形式存放
        values.put(MyRhythmContract.Pitches.COLUMN_STAR,pitchSequence.getStars());

        values.put(MyRhythmContract.Pitches.COLUMN_SELF_DESIGN,pitchSequence.isSelfDesign());
        values.put(MyRhythmContract.Pitches.COLUMN_KEEP_TOP,pitchSequence.isKeepTop());
        values.put(MyRhythmContract.Pitches.COLUMN_CREATE_TIME,pitchSequence.getCreateTime());
        values.put(MyRhythmContract.Pitches.COLUMN_LAST_MODIFY_TIME,pitchSequence.getLastModifyTime());


        affectedRows = mSQLiteDatabase.update(MyRhythmContract.Pitches.TABLE_NAME,values,
                MyRhythmContract.Pitches._ID+" = ? ",new String[]{String.valueOf(pitchesId)} );
        closeDB();
        return affectedRows;
    }


    private void getWritableDatabaseIfClosedOrNull(){
        if(mSQLiteDatabase==null || !mSQLiteDatabase.isOpen()) {
            mSQLiteDatabase = this.getWritableDatabase();
        }/*else if (mSQLiteDatabase.isReadOnly()){
            //只读的不行，先关，再开成可写的。
            try{
                mSQLiteDatabase.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
            mSQLiteDatabase = this.getWritableDatabase();
        }*/
    }

    private void getReadableDatabaseIfClosedOrNull(){
        if(mSQLiteDatabase==null || !mSQLiteDatabase.isOpen()) {
            mSQLiteDatabase = this.getReadableDatabase();
            //如果是可写DB，也能用，不再开关切换。
        }
    }

    //关数据库
    private void closeDB(){
        if(mSQLiteDatabase != null && mSQLiteDatabase.isOpen()){
            try{
                mSQLiteDatabase.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        } // end if
    }

}
