package com.vkyoungcn.learningtools.myrhythm.sqlite;


import android.provider.BaseColumns;

/*
 * 作者：杨胜 @中国海洋大学
 * 别名：杨镇时
 * author：Victor Young@ Ocean University of China
 * email: yangsheng@ouc.edu.cn
 * 2018.08.15
 * */
public class MyRhythmContract {
//    四张表：节奏、歌词、音高序列、操作记录
//    以节奏为主，为了简化，暂未引入交叉表及其复杂的逻辑，而是由节奏持有两个主歌词id、一个音高序列id；
//    未被节奏持有的歌词、音高资源属于“自由资源”；也可以独立编辑、生成等


    //    防止类意外实例化，令构造器为private。
    private MyRhythmContract() {
    }

    //id列由BC类自动设为_ID=_id
    /* 节奏表*/
    public static class Rhythm implements BaseColumns {
        public static final String TABLE_NAME = "rhythm";
        public static final String COLUMN_TITLE ="title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_C0DES = "code_serial";
        public static final String COLUMN_RHYTHM_TYPE = "beat_type";
        public static final String COLUMN_STAR = "star";
        public static final String COLUMN_SELF_DESIGN = "self_design";
        public static final String COLUMN_KEEP_TOP = "keep_top";
        public static final String COLUMN_CREATE_TIME = "create_time";
        public static final String COLUMN_LAST_MODIFY_TIME = "last_modify_time";
        public static final String COLUMN_PRIMARY_LYRIC_ID = "primary_lyric_id";
        public static final String COLUMN_SECOND_LYRIC_ID = "second_lyric_id";
        public static final String COLUMN_PITCH_SEQUENCE_ID = "pitch_sequence_id";
    }


    /* 歌词表*/
    public static class Lyric implements BaseColumns {
        public static final String TABLE_NAME = "lyric";
        public static final String COLUMN_TITLE ="title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_WORDS = "word_serial";
        public static final String COLUMN_STAR = "star";
        public static final String COLUMN_SELF_DESIGN = "self_design";
        public static final String COLUMN_KEEP_TOP = "keep_top";
        public static final String COLUMN_CREATE_TIME = "create_time";
        public static final String COLUMN_LAST_MODIFY_TIME = "last_modify_time";
    }


    /* 音高序列表*/
    public static class Pitches implements BaseColumns {
        public static final String TABLE_NAME = "pitches";
        public static final String COLUMN_TITLE ="title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_PITCHES = "pitch_serial";
        public static final String COLUMN_STAR = "star";
        public static final String COLUMN_SELF_DESIGN = "self_design";
        public static final String COLUMN_KEEP_TOP = "keep_top";
        public static final String COLUMN_CREATE_TIME = "create_time";
        public static final String COLUMN_LAST_MODIFY_TIME = "last_modify_time";
    }



    /* 分组表*/
    public static class Group implements BaseColumns {
        public static final String TABLE_NAME = "groups";
        public static final String COLUMN_TITLE ="title";
        public static final String COLUMN_DESCRIPTION = "description";
//        public static final String COLUMN_CODES = "pitch_serial";
        public static final String COLUMN_STAR = "star";
        public static final String COLUMN_SELF_DESIGN = "self_design";
        public static final String COLUMN_KEEP_TOP = "keep_top";
        public static final String COLUMN_CREATE_TIME = "create_time";
        public static final String COLUMN_LAST_MODIFY_TIME = "last_modify_time";
    }


    /* 分组--资源交叉表*/
    /* 混合在一起*/
    public static class GroupCrossModels implements BaseColumns {
        public static final String TABLE_NAME = "group_cross_models";
        public static final String COLUMN_GID ="gid";
        public static final String COLUMN_MID = "mid";
        public static final String COLUMN_MODEL_TYPE = "model_type";//7001、节奏；7002、词；7003、音高

        public static final int MODEL_TYPE_RH = 7001;
        public static final int MODEL_TYPE_LY = 7002;
        public static final int MODEL_TYPE_PT = 7003;

    }



    /* 操作记录表*/
    public static class ActionRecord implements BaseColumns {
        public static final String TABLE_NAME = "action_records";
        public static final String COLUMN_ACTION_TIME ="action_time";
        public static final String COLUMN_ACTION_TYPE ="action_type";
    }

}
