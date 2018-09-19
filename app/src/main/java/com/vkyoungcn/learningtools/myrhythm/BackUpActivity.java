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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BackUpActivity extends AppCompatActivity implements OnGeneralDfgInteraction {
    private static final String TAG = "BackUpActivity";
    public static final String SD_DIR ="RhythmSepBak";
    public static final String BK_FILE_EXT = ".rhmbk";

    ArrayList<String> fileNamesForChoose = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up_main);

    }


    public void localPrivateRecover(View view){
        File privateFile;
        try {
            privateFile = getFilesDir();
        } catch (Exception e) {
            e.printStackTrace();
//            Log.i(TAG, "localPrivateRecover: wrong.");
            return;
        }
        File[] files = privateFile.listFiles();
//        Log.i(TAG, "localPrivateRecover: file.length="+files.length);
        for (File file : files) {
            if (file.getName().contains(BK_FILE_EXT)) {
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

    public void localSdRecover(View view){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e(TAG, "saveBitmapLocalTest: mounted.");
        }else {
            Toast.makeText(this, "未检测到SD卡。", Toast.LENGTH_SHORT).show();
            return;
        }

        File sdBkDir;
        try {
            sdBkDir = getPublicStorageDir(SD_DIR);
        } catch (Exception e) {
            e.printStackTrace();
//            Log.i(TAG, "localPrivateRecover: wrong.");
            return;
        }
        File[] files = sdBkDir.listFiles();
//        Log.i(TAG, "localPrivateRecover: file.length="+files.length);
        for (File file : files) {
            if (file.getName().contains(BK_FILE_EXT)) {
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


    /*public void bakTest(View view){
        //官方代码
        if(isExternalStorageWritable()){
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date data = new Date(System.currentTimeMillis());
            String timeSuffix = sdFormat.format(data)+"vkbk";
            String fileName = "myRhythmDB"+timeSuffix;
            File fileTo = getPublicDocumentStorageDir(fileName);

            saveFileToFile(fileTo);
        }
    }*/

  /*  public void saveFileToFile(File fileTo) {
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

    }*/


    public void privateBackUp(View view) {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date(System.currentTimeMillis());
        String timeSuffix = sdFormat.format(date)+BK_FILE_EXT;
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


    public void sdBackUp(View view) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e(TAG, "saveBitmapLocalTest: mounted.");
        }else {
            Toast.makeText(this, "未检测到SD卡。", Toast.LENGTH_SHORT).show();
            return;

        }

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date(System.currentTimeMillis());
        String timeSuffix = sdFormat.format(date)+BK_FILE_EXT;
        String fileName = "RhythmSepDBk"+timeSuffix;

        File dbFile = getDatabasePath("MyRhythm.db");


        try {
            File outFile = new File(getPublicStorageDir(SD_DIR),fileName);
            if (!outFile.createNewFile()) {
                Log.e(TAG, "File not created");
                return;
            }
            FileInputStream inStream = new FileInputStream(dbFile);
            FileOutputStream outStream = new FileOutputStream(outFile);
//            FileOutputStream outStream = new FileOutputStream(getPublicDocumentStorageDir(fileName));
//            FileOutputStream outStream = openFileOutput(fileName, Context.MODE_PRIVATE);

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

        Toast.makeText(this, "成功。", Toast.LENGTH_SHORT).show();

    }

        public File getPublicStorageDir(String dirName) {
            // Get the directory for the user's public pictures directory.
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), dirName);
            if (!file.mkdirs()) {
                Log.e(TAG, "Directory not created.98");
            }
            return file;
        }

    public void privateRecovery(String sourceFileName) {
//        File sourceFile;
       /* try {
            sourceFile = new File(getFilesDir().getPath()+sourceFileName) ;
        } catch (Exception e) {
            e.printStackTrace();
//            Log.i(TAG, "localPrivateRecover: wrong.");
            return;
        }*/
        File dbFile = getDatabasePath("MyRhythm.db");

        try {
            FileInputStream inStream = new FileInputStream(getFilesDir().getPath()+"/"+sourceFileName);
            FileOutputStream outStream = new FileOutputStream(getDatabasePath("MyRhythm.db").getPath());
            //如果用openFileOutput，要求路径中不能有分隔符/

            byte[] buff = new byte[1024];
            int n = 0;
            while ((n = inStream.read(buff)) > 0) {
                outStream.write(buff, 0, n);
            }
            inStream.close();
            outStream.close();
            Toast.makeText(this, "成功。", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sdRecovery(String sourceFileName) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e(TAG, "saveBitmapLocalTest: mounted.");
        }else {
            Toast.makeText(this, "未检测到SD卡。", Toast.LENGTH_SHORT).show();
            return;
        }

        File sdBkDir;
        try {
            sdBkDir = getPublicStorageDir(SD_DIR);
        } catch (Exception e) {
            e.printStackTrace();
//            Log.i(TAG, "localPrivateRecover: wrong.");
            return;
        }

        File dbFile = getDatabasePath("MyRhythm.db");

        try {
            FileInputStream inStream = new FileInputStream(sdBkDir.getPath()+"/"+sourceFileName);
            FileOutputStream outStream = new FileOutputStream(getDatabasePath("MyRhythm.db").getPath());
            //如果用openFileOutput，要求路径中不能有分隔符/

            byte[] buff = new byte[1024];
            int n = 0;
            while ((n = inStream.read(buff)) > 0) {
                outStream.write(buff, 0, n);
            }
            inStream.close();
            outStream.close();
        } catch (Exception e) {
            Toast.makeText(this, "失败。", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        Toast.makeText(this, "成功。", Toast.LENGTH_SHORT).show();

    }



    /*public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }*/



/* 7.0以上可用。
StorageManager sm = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        StorageVolume volume = sm.getPrimaryStorageVolume();
        Intent intent = volume.createAccessIntent(Environment.DIRECTORY_DOCUMENTS);
        startActivityForResult(intent, request_code);*/


    @Override
    public void onButtonClickingDfgInteraction(int dfgType, Bundle data) {
        switch (dfgType){
            case SELECT_FILE:
                String fileName = data.getString("FILE_NAME");
                privateRecovery(fileName);

                break;
        }
    }
}
