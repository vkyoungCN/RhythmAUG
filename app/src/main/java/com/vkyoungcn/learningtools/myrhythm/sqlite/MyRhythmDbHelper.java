package com.vkyoungcn.learningtools.myrhythm.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vkyoungcn.learningtools.myrhythm.models.Group;
import com.vkyoungcn.learningtools.myrhythm.models.RhythmBasedCompound;
import com.vkyoungcn.learningtools.myrhythm.models.Lyric;
import com.vkyoungcn.learningtools.myrhythm.models.PitchSequence;
import com.vkyoungcn.learningtools.myrhythm.models.Rhythm;
import com.vkyoungcn.learningtools.myrhythm.helper.RhythmHelper;

import junit.framework.Test;

import java.util.ArrayList;

public class MyRhythmDbHelper extends SQLiteOpenHelper {
//    private static final String TAG = "MyRhythmDbHelper";

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
                    MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE + " INTEGER, "+

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

    //创建标签表
    public static final String SQL_CREATE_SINGLE_TAG =
            "CREATE TABLE " + MyRhythmContract.SingleTag.TABLE_NAME + " (" +
                    MyRhythmContract.SingleTag._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MyRhythmContract.SingleTag.COLUMN_TITLE + " TEXT, "+
                    MyRhythmContract.SingleTag.COLUMN_DESCRIPTION + " TEXT)";



    //创建分组表
    public static final String SQL_CREATE_GROUPS =
            "CREATE TABLE " + MyRhythmContract.Group.TABLE_NAME + " (" +
                    MyRhythmContract.Group._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MyRhythmContract.Group.COLUMN_TITLE + " TEXT, "+
                    MyRhythmContract.Group.COLUMN_DESCRIPTION + " TEXT, "+
//                    MyRhythmContract.Group. + " TEXT, "+

                    MyRhythmContract.Group.COLUMN_STAR + " INTEGER, "+
                    MyRhythmContract.Group.COLUMN_SELF_DESIGN + " BOOLEAN, "+
                    MyRhythmContract.Group.COLUMN_KEEP_TOP + " BOOLEAN, "+

                    MyRhythmContract.Group.COLUMN_CREATE_TIME + " INTEGER, "+
                    MyRhythmContract.Group.COLUMN_LAST_MODIFY_TIME + " INTEGER)";


    public static final String SQL_CREATE_GROUP_CROSS =
            "CREATE TABLE " + MyRhythmContract.GroupCrossModels.TABLE_NAME + " (" +
                    MyRhythmContract.GroupCrossModels._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MyRhythmContract.GroupCrossModels.COLUMN_GID + " INTEGER, "+
                    MyRhythmContract.GroupCrossModels.COLUMN_MID + " INTEGER, "+
                    MyRhythmContract.GroupCrossModels.COLUMN_MODEL_TYPE + " INTEGER)";

    /* 删表语句*/
    private static final String SQL_DROP_RHYTHM =
            "DROP TABLE IF EXISTS " +  MyRhythmContract.Rhythm.TABLE_NAME;
    private static final String SQL_DROP_LYRIC =
            "DROP TABLE IF EXISTS " +  MyRhythmContract.Lyric.TABLE_NAME;
    private static final String SQL_DROP_PITCHES =
            "DROP TABLE IF EXISTS " +  MyRhythmContract.Pitches.TABLE_NAME;
    private static final String SQL_DROP_ACTION_RECORDS =
            "DROP TABLE IF EXISTS " +  MyRhythmContract.ActionRecord.TABLE_NAME;
    private static final String SQL_DROP_GROUP =
            "DROP TABLE IF EXISTS " +  MyRhythmContract.Group.TABLE_NAME;
    private static final String SQL_DROP_GROUP_CROSS =
                "DROP TABLE IF EXISTS " +  MyRhythmContract.GroupCrossModels.TABLE_NAME;


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

        //填充几个数据
//        ArrayList<Rhythm> rhythmDefaultData = new ArrayList<>(2);
        TestDataHelper tdh = new TestDataHelper();
        createRhythmWithDb(db, tdh.populateRhythm(2));
//        createRhythmWithDb(db,tdh.populateRhythm(2));
//        createRhythmWithDb(db,tdh.populateRhythm(3));
//        createRhythmWithDb(db,tdh.populateRhythm(4));

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
        values.put(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE, rhythm.getRhythmType());
        values.put(MyRhythmContract.Rhythm.COLUMN_C0DES, rhythm.getCodeSerialString());//以字串形式存放
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


    /* 如果不涉及歌词*/
    public int updateRhythm(Rhythm rhythm){
        int affectedRows = 0;

        getReadableDatabaseIfClosedOrNull();

        ContentValues values = new ContentValues();

        values.put(MyRhythmContract.Rhythm.COLUMN_TITLE, rhythm.getTitle());
        values.put(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION, rhythm.getDescription());
        values.put(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE, rhythm.getRhythmType());
        values.put(MyRhythmContract.Rhythm.COLUMN_C0DES, rhythm.getCodeSerialString());//以字串形式存放
        values.put(MyRhythmContract.Rhythm.COLUMN_STAR,rhythm.getStars());

        values.put(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN,rhythm.isSelfDesign());
        values.put(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP,rhythm.isKeepTop());
        values.put(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME,rhythm.getCreateTime());
        values.put(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME,rhythm.getLastModifyTime());
        values.put(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID,rhythm.getPrimaryLyricId());
        values.put(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID,rhythm.getSecondLyricId());
        values.put(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID,rhythm.getPitchesId());

        affectedRows = mSQLiteDatabase.update(MyRhythmContract.Rhythm.TABLE_NAME,values,
                MyRhythmContract.Rhythm._ID+" = ? ",new String[]{String.valueOf(rhythm.getId())} );
        closeDB();
        return affectedRows;
    }


    /* 仅更新group表的非交叉信息（只涉及一张表）*/
    public int updateGroupPure(Group group){
        int affectedRows = 0;

        getReadableDatabaseIfClosedOrNull();

        ContentValues values = new ContentValues();

        values.put(MyRhythmContract.Rhythm.COLUMN_TITLE, group.getTitle());
        values.put(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION, group.getDescription());

        affectedRows = mSQLiteDatabase.update(MyRhythmContract.Group.TABLE_NAME,values,
                MyRhythmContract.Group._ID+" = ? ",new String[]{String.valueOf(group.getId())} );
        closeDB();
        return affectedRows;
    }



    /* 如果传递c_Rh类则需要对两表操作*/
    /*public long createRhythm(RhythmBasedCompound compoundRhythm){
        long l;
        getWritableDatabaseIfClosedOrNull();

        mSQLiteDatabase.beginTransaction();

        //先生成对应的Lyric数据
        Lyric lyric_p = new Lyric();
        lyric_p.setCodeSerialString(compoundRhythm.getPrimaryLyricSerial());



        ContentValues values_rh = new ContentValues();

        values_rh.put(MyRhythmContract.Rhythm.COLUMN_TITLE, compoundRhythm.getTitle());
        values_rh.put(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION, compoundRhythm.getDescription());
        values_rh.put(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE, compoundRhythm.getRhythmType());
        values_rh.put(MyRhythmContract.Rhythm.COLUMN_C0DES, compoundRhythm.getCodeSerialString());//以字串形式存放
        values_rh.put(MyRhythmContract.Rhythm.COLUMN_STAR,compoundRhythm.getStars());

        values_rh.put(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN,compoundRhythm.isSelfDesign());
        values_rh.put(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP,compoundRhythm.isKeepTop());
        values_rh.put(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME,compoundRhythm.getCreateTime());
        values_rh.put(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME,compoundRhythm.getLastModifyTime());
        values_rh.put(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID,compoundRhythm.getPrimaryLyricId());
        values_rh.put(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID,compoundRhythm.getSecondLyricId());
        values_rh.put(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID,compoundRhythm.getPitchesId());

        l = mSQLiteDatabase.insert(MyRhythmContract.Rhythm.TABLE_NAME, null, values_rh);

        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();


        closeDB();

        return l;
    }
*/
    public long createRhythmWithDb(SQLiteDatabase db,Rhythm rhythm){
        long l;
        ContentValues values = new ContentValues();

        values.put(MyRhythmContract.Rhythm.COLUMN_TITLE, rhythm.getTitle());
        values.put(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION, rhythm.getDescription());
        values.put(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE, rhythm.getRhythmType());
//        Log.i(TAG, "createRhythmWithDb: rh.codes="+rhythm.getCodeSerialByte().toString());
//        Log.i(TAG, "createRhythmWithDb: rh.codesStr="+rhythm.getCodeSerialString());
        values.put(MyRhythmContract.Rhythm.COLUMN_C0DES, rhythm.getCodeSerialString());//以字串形式存放
        values.put(MyRhythmContract.Rhythm.COLUMN_STAR,rhythm.getStars());

        values.put(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN,rhythm.isSelfDesign());
        values.put(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP,rhythm.isKeepTop());
        values.put(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME,rhythm.getCreateTime());
        values.put(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME,rhythm.getLastModifyTime());
        values.put(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID,rhythm.getPrimaryLyricId());
        values.put(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID,rhythm.getSecondLyricId());
        values.put(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID,rhythm.getPitchesId());

        l = db.insert(MyRhythmContract.Rhythm.TABLE_NAME, null, values);
        closeDB();

        return l;
    }


    /* 用于在事务内的lyric生成方法*/
    public void createLyric_inTRS(Lyric lyric){
        ContentValues values = new ContentValues();

        values.put(MyRhythmContract.Lyric.COLUMN_TITLE, lyric.getTitle());
        values.put(MyRhythmContract.Lyric.COLUMN_DESCRIPTION, lyric.getDescription());
        values.put(MyRhythmContract.Lyric.COLUMN_WORDS, lyric.getCodeSerialString());//以字串形式存放
        values.put(MyRhythmContract.Lyric.COLUMN_STAR,lyric.getStars());

        values.put(MyRhythmContract.Lyric.COLUMN_SELF_DESIGN,lyric.isSelfDesign());
        values.put(MyRhythmContract.Lyric.COLUMN_KEEP_TOP,lyric.isKeepTop());
        values.put(MyRhythmContract.Lyric.COLUMN_CREATE_TIME,lyric.getCreateTime());
        values.put(MyRhythmContract.Lyric.COLUMN_LAST_MODIFY_TIME,lyric.getLastModifyTime());

    }

    public long createLyric(Lyric lyric){
        long l;
        getWritableDatabaseIfClosedOrNull();
        ContentValues values = new ContentValues();

        values.put(MyRhythmContract.Lyric.COLUMN_TITLE, lyric.getTitle());
        values.put(MyRhythmContract.Lyric.COLUMN_DESCRIPTION, lyric.getDescription());
        values.put(MyRhythmContract.Lyric.COLUMN_WORDS, lyric.getCodeSerialString());//以字串形式存放
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
        values.put(MyRhythmContract.Pitches.COLUMN_PITCHES, pitchSequence.getCodeSerialString());//以字串形式存放
        values.put(MyRhythmContract.Pitches.COLUMN_STAR,pitchSequence.getStars());

        values.put(MyRhythmContract.Pitches.COLUMN_SELF_DESIGN,pitchSequence.isSelfDesign());
        values.put(MyRhythmContract.Pitches.COLUMN_KEEP_TOP,pitchSequence.isKeepTop());
        values.put(MyRhythmContract.Pitches.COLUMN_CREATE_TIME,pitchSequence.getCreateTime());
        values.put(MyRhythmContract.Pitches.COLUMN_LAST_MODIFY_TIME,pitchSequence.getLastModifyTime());

        l = mSQLiteDatabase.insert(MyRhythmContract.Pitches.TABLE_NAME, null, values);
        closeDB();

        return l;
    }


    public long createGroupPure(Group group){
        long l;
        getWritableDatabaseIfClosedOrNull();
        ContentValues values = new ContentValues();

        values.put(MyRhythmContract.Group.COLUMN_TITLE, group.getTitle());
        values.put(MyRhythmContract.Group.COLUMN_DESCRIPTION, group.getDescription());
        values.put(MyRhythmContract.Group.COLUMN_STAR,group.getStars());

        values.put(MyRhythmContract.Group.COLUMN_SELF_DESIGN,group.isSelfDesign());
        values.put(MyRhythmContract.Group.COLUMN_KEEP_TOP,group.isKeepTop());
        values.put(MyRhythmContract.Group.COLUMN_CREATE_TIME,group.getCreateTime());
        values.put(MyRhythmContract.Group.COLUMN_LAST_MODIFY_TIME,group.getLastModifyTime());

        l = mSQLiteDatabase.insert(MyRhythmContract.Group.TABLE_NAME, null, values);
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

    public void removeModelCrossGroup(int modelId, int groupId){
        String deleteSingleRhythmSql = "DELETE FROM "+ MyRhythmContract.GroupCrossModels.TABLE_NAME+" WHERE "+
                MyRhythmContract.GroupCrossModels.COLUMN_MID+" = "+modelId+" AND "+
                MyRhythmContract.GroupCrossModels.COLUMN_GID+" = "+groupId;
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

    public void deleteGroupById(int groupId){
        String deleteSql = "DELETE FROM "+ MyRhythmContract.Group.TABLE_NAME+" WHERE "+
                MyRhythmContract.Group._ID+" = "+groupId;
        //考虑到cascade的设定，就不对交叉表做手动处理了。

        mSQLiteDatabase.execSQL(deleteSql);
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
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE, rhythm.getRhythmType());
        contentValues.put(MyRhythmContract.Rhythm.COLUMN_C0DES, rhythm.getCodeSerialString());//以字串形式存放
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

    public int updateLyricById(Lyric lyric){
        int affectedRows = 0;
        getReadableDatabaseIfClosedOrNull();

        ContentValues values = new ContentValues();

        values.put(MyRhythmContract.Lyric.COLUMN_TITLE, lyric.getTitle());
        values.put(MyRhythmContract.Lyric.COLUMN_DESCRIPTION, lyric.getDescription());
        values.put(MyRhythmContract.Lyric.COLUMN_WORDS, lyric.getCodeSerialString());//以字串形式存放
        values.put(MyRhythmContract.Lyric.COLUMN_STAR,lyric.getStars());

        values.put(MyRhythmContract.Lyric.COLUMN_SELF_DESIGN,lyric.isSelfDesign());
        values.put(MyRhythmContract.Lyric.COLUMN_KEEP_TOP,lyric.isKeepTop());
//        values.put(MyRhythmContract.Lyric.COLUMN_CREATE_TIME,lyric.getCreateTime());
        values.put(MyRhythmContract.Lyric.COLUMN_LAST_MODIFY_TIME,lyric.getLastModifyTime());

        affectedRows = mSQLiteDatabase.update(MyRhythmContract.Lyric.TABLE_NAME,values,
                MyRhythmContract.Lyric._ID+" = ? ",new String[]{String.valueOf(lyric.getId())} );
        closeDB();
        return affectedRows;
    }


    public int updatePitchesById(int pitchesId,PitchSequence pitchSequence){
        int affectedRows = 0;
        getReadableDatabaseIfClosedOrNull();

        ContentValues values = new ContentValues();
        values.put(MyRhythmContract.Pitches.COLUMN_TITLE, pitchSequence.getTitle());
        values.put(MyRhythmContract.Pitches.COLUMN_DESCRIPTION, pitchSequence.getDescription());
        values.put(MyRhythmContract.Pitches.COLUMN_PITCHES, pitchSequence.getCodeSerialString());//以字串形式存放
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


    /* 获取纯rh表的节奏记录*/
    public ArrayList<Rhythm> getAllRhythms(){
        ArrayList<Rhythm> rhythms = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+ MyRhythmContract.Rhythm.TABLE_NAME;

        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do {
                Rhythm rhythm_1 = new Rhythm();
                rhythm_1.setId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm._ID)));
                rhythm_1.setCreateTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME)));
                rhythm_1.setLastModifyTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME)));
                rhythm_1.setDescription(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION)));
                rhythm_1.setSelfDesign(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN))==1);
                rhythm_1.setKeepTop(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP))==1);
                rhythm_1.setRhythmType(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE)));
                rhythm_1.setStars(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_STAR)));
                rhythm_1.setTitle(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_TITLE)));
                rhythm_1.setCodeSerialString(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_C0DES)));

                rhythm_1.setPrimaryLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID)));
                rhythm_1.setSecondLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID)));
                rhythm_1.setPitchesId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID)));
                rhythms.add(rhythm_1);
            }while (cursor.moveToNext());
        }else{
            return null;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();

        return rhythms;
    }


    public ArrayList<RhythmBasedCompound> getAllCompoundRhythms(){
        ArrayList<RhythmBasedCompound> rhythms = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+ MyRhythmContract.Rhythm.TABLE_NAME;

        getReadableDatabaseIfClosedOrNull();
        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do {
                RhythmBasedCompound rhythmBasedCompound = new RhythmBasedCompound();

                rhythmBasedCompound.setId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm._ID)));
                rhythmBasedCompound.setCreateTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME)));
                rhythmBasedCompound.setLastModifyTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME)));
                rhythmBasedCompound.setDescription(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION)));
                rhythmBasedCompound.setSelfDesign(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN))==1);
                rhythmBasedCompound.setKeepTop(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP))==1);
                rhythmBasedCompound.setRhythmType(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE)));
                rhythmBasedCompound.setStars(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_STAR)));
                rhythmBasedCompound.setTitle(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_TITLE)));
                rhythmBasedCompound.setCodeSerialString(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_C0DES)));

                rhythmBasedCompound.setPrimaryLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID)));
                rhythmBasedCompound.setSecondLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID)));
                rhythmBasedCompound.setPitchesId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID)));

                rhythmBasedCompound.setPrimaryLyricSerial(getLyricStringById_TRS(rhythmBasedCompound.getPrimaryLyricId()));
                rhythmBasedCompound.setSecondLyricSerial(getLyricStringById_TRS(rhythmBasedCompound.getSecondLyricId()));
                //暂时未存入音高序列

                rhythms.add(rhythmBasedCompound);
            }while (cursor.moveToNext());
        }else{
            return null;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();

        closeDB();

        return rhythms;
    }


    /* 获取某分组所含的节奏记录（混合式，完整数据）*/
    public ArrayList<RhythmBasedCompound> getRhythmBasedCompoundsByGid(int groupId){
        ArrayList<RhythmBasedCompound> rhythms = new ArrayList<>();

        String selectRhid = "SELECT "+MyRhythmContract.GroupCrossModels.COLUMN_MID+" FROM "+
                MyRhythmContract.GroupCrossModels.TABLE_NAME +
                " WHERE "+MyRhythmContract.GroupCrossModels.COLUMN_GID +" = "+groupId +
                " AND "+MyRhythmContract.GroupCrossModels.COLUMN_MODEL_TYPE +" = "+
                MyRhythmContract.GroupCrossModels.MODEL_TYPE_RH;

        getReadableDatabaseIfClosedOrNull();
        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectRhid,null);

        if(cursor.moveToFirst()){
            do {
                rhythms.add(getCompoundRhythmById_TRS(cursor.getInt(0)));
            }while (cursor.moveToNext());
        }else{
            return null;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();

        closeDB();

        return rhythms;
    }

    public ArrayList<Lyric> getFreeLyricsByGid(int groupId){
        ArrayList<Lyric> lyris = new ArrayList<>();

        String selectLYid = "SELECT "+MyRhythmContract.GroupCrossModels.COLUMN_MID+" FROM "+
                MyRhythmContract.GroupCrossModels.TABLE_NAME +
                " WHERE "+MyRhythmContract.GroupCrossModels.COLUMN_GID +" = "+groupId +
                " AND "+MyRhythmContract.GroupCrossModels.COLUMN_MODEL_TYPE +" = "+
                MyRhythmContract.GroupCrossModels.MODEL_TYPE_LY;

        getReadableDatabaseIfClosedOrNull();
        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectLYid,null);

        if(cursor.moveToFirst()){
            do {
                lyris.add(getFreeLyricsById_TRS(cursor.getInt(0)));
            }while (cursor.moveToNext());
        }else{
            return null;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();

        closeDB();

        return lyris;
    }


    public ArrayList<Lyric> getAllLyrics(){
        ArrayList<Lyric> lyrics = new ArrayList<>();

        String selectLYid = "SELECT * FROM "+MyRhythmContract.Lyric.TABLE_NAME ;

        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectLYid,null);
        if(cursor.moveToFirst()){
            do {
                Lyric lyric = new Lyric();
                lyric.setId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Lyric._ID)));
                lyric.setCreateTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_CREATE_TIME)));
                lyric.setLastModifyTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_LAST_MODIFY_TIME)));
                lyric.setDescription(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_DESCRIPTION)));
                lyric.setSelfDesign(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_SELF_DESIGN))==1);
                lyric.setKeepTop(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_KEEP_TOP))==1);
//                group.setRhythmType(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Group.COLUMN_RHYTHM_TYPE)));
                lyric.setStars(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_STAR)));
                lyric.setTitle(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_TITLE)));
                lyric.setCodeSerialString(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_WORDS)));

                lyrics.add(lyric);
            }while (cursor.moveToNext());
        }else{
            return null;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        closeDB();
        return lyrics;
    }


    /*public ArrayList<String> getFreePitchesByGid(int groupId){
        ArrayList<String> pitchesCodes = new ArrayList<>();

        String selectLYid = "SELECT "+MyRhythmContract.GroupCrossModels.COLUMN_MID+" FROM "+
                MyRhythmContract.GroupCrossModels.TABLE_NAME +
                " WHERE "+MyRhythmContract.GroupCrossModels.COLUMN_GID +" = "+groupId +
                " AND "+MyRhythmContract.GroupCrossModels.COLUMN_MODEL_TYPE +" = "+
                MyRhythmContract.GroupCrossModels.MODEL_TYPE_PT;

        getReadableDatabaseIfClosedOrNull();
        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectLYid,null);

        if(cursor.moveToFirst()){
            do {
//                pitchesCodes.add(getLyricStringById_TRS(cursor.getInt(0)));
            }while (cursor.moveToNext());
        }else{
            return null;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();

        closeDB();

        return pitchesCodes;
    }*/


    public ArrayList<RhythmBasedCompound> getCompoundRhythmsModifiedLaterThan(long time){
        ArrayList<RhythmBasedCompound> compounds = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+ MyRhythmContract.Rhythm.TABLE_NAME+
                " WHERE "+MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME+" > "+time;

        getReadableDatabaseIfClosedOrNull();
        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do {
                RhythmBasedCompound rhythmBasedCompound = new RhythmBasedCompound();
                rhythmBasedCompound.setId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm._ID)));
                rhythmBasedCompound.setCreateTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME)));
                rhythmBasedCompound.setLastModifyTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME)));
                rhythmBasedCompound.setDescription(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION)));
                rhythmBasedCompound.setSelfDesign(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN))==1);
                rhythmBasedCompound.setKeepTop(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP))==1);
                rhythmBasedCompound.setRhythmType(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE)));
                rhythmBasedCompound.setStars(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_STAR)));
                rhythmBasedCompound.setTitle(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_TITLE)));
                rhythmBasedCompound.setCodeSerialString(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_C0DES)));

                rhythmBasedCompound.setPrimaryLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID)));
                rhythmBasedCompound.setSecondLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID)));
                rhythmBasedCompound.setPitchesId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID)));

                rhythmBasedCompound.setPrimaryLyricSerial(getLyricStringById_TRS(rhythmBasedCompound.getPrimaryLyricId()));
                rhythmBasedCompound.setSecondLyricSerial(getLyricStringById_TRS(rhythmBasedCompound.getSecondLyricId()));

                compounds.add(rhythmBasedCompound);
            }while (cursor.moveToNext());
        }else{
            return null;
        }

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();

        closeDB();
        return compounds;
    }

    //获取修改时间在某时间内，或者虽不在指定时间内，但标有置顶的节奏项目
    public ArrayList<RhythmBasedCompound> getTopKeepCompoundRhythmsOrModifiedLaterThan(long time){
        ArrayList<RhythmBasedCompound> compounds = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+ MyRhythmContract.Rhythm.TABLE_NAME+
                " WHERE "+MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME+" > "+time
                +" OR "+MyRhythmContract.Rhythm.COLUMN_KEEP_TOP+" = 1 ";

        getReadableDatabaseIfClosedOrNull();
        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do {
                RhythmBasedCompound rhythmBasedCompound = new RhythmBasedCompound();
                rhythmBasedCompound.setId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm._ID)));
                rhythmBasedCompound.setCreateTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME)));
                rhythmBasedCompound.setLastModifyTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME)));
                rhythmBasedCompound.setDescription(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION)));
                rhythmBasedCompound.setSelfDesign(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN))==1);
                rhythmBasedCompound.setKeepTop(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP))==1);
                rhythmBasedCompound.setRhythmType(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE)));
                rhythmBasedCompound.setStars(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_STAR)));
                rhythmBasedCompound.setTitle(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_TITLE)));
                rhythmBasedCompound.setCodeSerialString(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_C0DES)));

                rhythmBasedCompound.setPrimaryLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID)));
                rhythmBasedCompound.setSecondLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID)));
                rhythmBasedCompound.setPitchesId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID)));

                rhythmBasedCompound.setPrimaryLyricSerial(getLyricStringById_TRS(rhythmBasedCompound.getPrimaryLyricId()));
                rhythmBasedCompound.setSecondLyricSerial(getLyricStringById_TRS(rhythmBasedCompound.getSecondLyricId()));

                compounds.add(rhythmBasedCompound);
            }while (cursor.moveToNext());
        }else{
            return null;
        }

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();

        closeDB();
        return compounds;
    }

    public RhythmBasedCompound getCompoundRhythmById(int rhythmId){
        RhythmBasedCompound cRhythm = new RhythmBasedCompound();
        String selectQuery = "SELECT * FROM "+ MyRhythmContract.Rhythm.TABLE_NAME+
                " WHERE "+ MyRhythmContract.Rhythm._ID+" = "+rhythmId;

        getReadableDatabaseIfClosedOrNull();
        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            cRhythm.setId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm._ID)));
            cRhythm.setCreateTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME)));
            cRhythm.setLastModifyTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME)));
            cRhythm.setDescription(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION)));
            cRhythm.setSelfDesign(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN))==1);
            cRhythm.setKeepTop(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP))==1);
            cRhythm.setRhythmType(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE)));
            cRhythm.setStars(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_STAR)));
            cRhythm.setTitle(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_TITLE)));
            cRhythm.setCodeSerialString(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_C0DES)));

            cRhythm.setPrimaryLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID)));
            cRhythm.setSecondLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID)));
            cRhythm.setPitchesId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID)));
        }
        String primaryLyricSerial = getLyricStringById_TRS(cRhythm.getPrimaryLyricId());
        cRhythm.setPrimaryLyricSerial(primaryLyricSerial);

        String secondLyricSerial = getLyricStringById_TRS(cRhythm.getSecondLyricId());
        cRhythm.setSecondLyricSerial(secondLyricSerial);


        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();

        closeDB();
        return cRhythm;
    }


    public RhythmBasedCompound getCompoundRhythmById_TRS(int rhythmId){
        RhythmBasedCompound cRhythm = new RhythmBasedCompound();
        String selectQuery = "SELECT * FROM "+ MyRhythmContract.Rhythm.TABLE_NAME+
                " WHERE "+ MyRhythmContract.Rhythm._ID+" = "+rhythmId;

//        getReadableDatabaseIfClosedOrNull();
//        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            cRhythm.setId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm._ID)));
            cRhythm.setCreateTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_CREATE_TIME)));
            cRhythm.setLastModifyTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_LAST_MODIFY_TIME)));
            cRhythm.setDescription(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_DESCRIPTION)));
            cRhythm.setSelfDesign(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SELF_DESIGN))==1);
            cRhythm.setKeepTop(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_KEEP_TOP))==1);
            cRhythm.setRhythmType(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_RHYTHM_TYPE)));
            cRhythm.setStars(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_STAR)));
            cRhythm.setTitle(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_TITLE)));
            cRhythm.setCodeSerialString(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_C0DES)));

            cRhythm.setPrimaryLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PRIMARY_LYRIC_ID)));
            cRhythm.setSecondLyricId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_SECOND_LYRIC_ID)));
            cRhythm.setPitchesId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_PITCH_SEQUENCE_ID)));
        }
        String primaryLyricSerial = getLyricStringById_TRS(cRhythm.getPrimaryLyricId());
        cRhythm.setPrimaryLyricSerial(primaryLyricSerial);

        String secondLyricSerial = getLyricStringById_TRS(cRhythm.getSecondLyricId());
        cRhythm.setSecondLyricSerial(secondLyricSerial);


       /* try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();

        closeDB();*/
        return cRhythm;
    }

    public Lyric getFreeLyricsById_TRS(int lyricId){
        Lyric lyric = new Lyric();
        String selectQuery = "SELECT * FROM "+ MyRhythmContract.Lyric.TABLE_NAME+
                " WHERE "+ MyRhythmContract.Lyric._ID+" = "+lyricId;

//        getReadableDatabaseIfClosedOrNull();
//        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            lyric.setId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Lyric._ID)));
            lyric.setCreateTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_CREATE_TIME)));
            lyric.setLastModifyTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_LAST_MODIFY_TIME)));
            lyric.setDescription(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_DESCRIPTION)));
            lyric.setSelfDesign(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_SELF_DESIGN))==1);
            lyric.setKeepTop(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_KEEP_TOP))==1);
            lyric.setStars(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_STAR)));
            lyric.setTitle(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Lyric.COLUMN_TITLE)));
        }

       /* try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();

        closeDB();*/
        return lyric;
    }

    //在事务中使用的版本，不启用db开闭
    public String getLyricStringById_TRS(int lyricId){
        String selectQuery = "SELECT "+MyRhythmContract.Lyric.COLUMN_WORDS+" FROM "+ MyRhythmContract.Lyric.TABLE_NAME+
                " WHERE "+ MyRhythmContract.Lyric._ID+" = "+lyricId;

//        getReadableDatabaseIfClosedOrNull();
//        mSQLiteDatabase.beginTransaction();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);
        String lyricStr = "";
        if(cursor.moveToFirst()){
            lyricStr = cursor.getString(cursor.getColumnIndex(MyRhythmContract.Rhythm.COLUMN_C0DES));
        }

        /*try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
//        mSQLiteDatabase.setTransactionSuccessful();
//        mSQLiteDatabase.endTransaction();

//        closeDB();
        return lyricStr;
    }



    public int getAmountOfRhythms(){
        int totalNum = 0;
        String selectQuery = "SELECT COUNT(*) FROM "+ MyRhythmContract.Rhythm.TABLE_NAME;

        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            totalNum = cursor.getInt(0);
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();

        return totalNum;
    }


    /* 获取纯rh表的节奏记录*/
    public ArrayList<Group> getAllGroups(){
        ArrayList<Group> groups = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+ MyRhythmContract.Group.TABLE_NAME;

        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do {
                Group group = new Group();
                group.setId(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Group._ID)));
                group.setCreateTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Group.COLUMN_CREATE_TIME)));
                group.setLastModifyTime(cursor.getLong(cursor.getColumnIndex(MyRhythmContract.Group.COLUMN_LAST_MODIFY_TIME)));
                group.setDescription(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Group.COLUMN_DESCRIPTION)));
                group.setSelfDesign(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Group.COLUMN_SELF_DESIGN))==1);
                group.setKeepTop(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Group.COLUMN_KEEP_TOP))==1);
//                group.setRhythmType(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Group.COLUMN_RHYTHM_TYPE)));
                group.setStars(cursor.getInt(cursor.getColumnIndex(MyRhythmContract.Group.COLUMN_STAR)));
                group.setTitle(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Group.COLUMN_TITLE)));
//                group.setCodeSerialString(cursor.getString(cursor.getColumnIndex(MyRhythmContract.Group.COLUMN_C0DES)));

                groups.add(group);
            }while (cursor.moveToNext());
        }else{
            return null;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();

        return groups;
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
