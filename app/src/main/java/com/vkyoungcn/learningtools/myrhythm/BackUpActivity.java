package com.vkyoungcn.learningtools.myrhythm;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.fragments.OnGeneralDfgInteraction;
import com.vkyoungcn.learningtools.myrhythm.fragments.SelectFileNameDiaFragment;
import com.vkyoungcn.learningtools.myrhythm.helper.BackupTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BackUpActivity extends AppCompatActivity implements OnGeneralDfgInteraction {
    private static final String TAG = "BackUpActivity";

    ArrayList<String> fileNamesForChoose = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up_main);

    }


    public void localRecover(View view){
        File privateFile;
        try {
            privateFile = getFilesDir();
        } catch (Exception e) {
            e.printStackTrace();
//            Log.i(TAG, "localRecover: wrong.");
            return;
        }
        File[] files = privateFile.listFiles();
        Log.i(TAG, "localRecover: file.length="+files.length);
        for (File file : files) {
            if (file.getName().contains(".vbk")) {
                fileNamesForChoose.add(file.getName());
            }
        }
        if (fileNamesForChoose.isEmpty()){
            Toast.makeText(this, "未找到备份文件", Toast.LENGTH_SHORT).show();
            return;
        }

        //弹出dfg，选择恢复文件
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("SELECT");

        if (prev != null) {
            Toast.makeText(getApplicationContext(), "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }
        DialogFragment dfg = SelectFileNameDiaFragment.newInstance(fileNamesForChoose);
        dfg.show(transaction, "SELECT");

    }




    public void fileSelectBkRemote(View view){
        /*
        Intent intent = new Intent(this,SelectAndProcessActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        this.startActivity(intent);//下页含有一个fg(两种：本地、网络选择)选好后直接在该act中对本地DB进行操作，
        */// 然后显示结果，然后回到Main页。
    }

    //数据恢复
    private void dataResume(){
        new BackupTask(this).execute("restoreDatabase");
    }

//数据备份

    private void dataBackup(){
        new BackupTask(this).execute("backupDatabase");
    }


    public void bakTest(View view){
        //官方代码
        if(isExternalStorageWritable()){
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date data = new Date(System.currentTimeMillis());
            String timeSuffix = sdFormat.format(data)+"vkbk";
            String fileName = "myRhythmDB"+timeSuffix;
            File fileTo = getPublicDocumentStorageDir(fileName);

            saveFileToFile(fileTo);
        }
    }

    public void saveFileToFile(File fileTo) {
        File dbFile = getDatabasePath("MyRhythm.db");

        try {
            FileInputStream inStream = new FileInputStream(dbFile);
            FileOutputStream outStream = new FileOutputStream(fileTo);

            byte[] buff = new byte[1024];
            int n = 0;
            while ((n = inStream.read(buff)) > 0) {
                outStream.write(buff, 0, n);
            }
            inStream.close();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public File getPublicDocumentStorageDir(String docName) {
        // Get the directory for the user's public directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(//共享/外部的顶层目录
                Environment.DIRECTORY_DOWNLOADS), docName);
        //如果getExternalStorageDirectory，则是获取SD目录（？区别，待）；
//        File file = new File("/sdcard/aaa");
            if (!file.mkdirs()) {//mkdirs()
                Log.e(TAG, "Directory not created");
            }
        return file;
    }

    public void privateBackUp(View view) {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date data = new Date(System.currentTimeMillis());
        String timeSuffix = sdFormat.format(data)+".vbk";
        String fileName = "dbBack"+timeSuffix;

        File dbFile = getDatabasePath("MyRhythm.db");

        try {
            FileInputStream inStream = new FileInputStream(dbFile);
            FileOutputStream outStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            byte[] buff = new byte[1024];
            int n = 0;
            while ((n = inStream.read(buff)) > 0) {
                outStream.write(buff, 0, n);
            }
            inStream.close();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }



/* 7.0以上可用。
StorageManager sm = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        StorageVolume volume = sm.getPrimaryStorageVolume();
        Intent intent = volume.createAccessIntent(Environment.DIRECTORY_DOCUMENTS);
        startActivityForResult(intent, request_code);*/


    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        switch (dfgType){
            case SELECT_FILE:

                break;
        }
    }
}
