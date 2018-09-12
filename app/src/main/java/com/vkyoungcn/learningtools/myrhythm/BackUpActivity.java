package com.vkyoungcn.learningtools.myrhythm;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.helper.BackupTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackUpActivity extends AppCompatActivity {
    private static final String TAG = "BackUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up_main);

    }


    public void fileSelectBkLocal(View view){

    }


    public void bakToLocal(View view){
        dataBackup();
}

       /* try {
            FileInputStream is = new FileInputStream(dbFile);
            FileOutputStream out = new FileOutputStream(exportDir);

            byte[] buff = new byte[1024];
            int n = 0;
            while ((n = is.read(buff)) > 0) {
                out.write(buff, 0, n);
            }
            is.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/


     /*
    * 示例中的常量
    *  private static String DB_PATH = "/data/data/com.example.cjm.englishlearn/databases/";
    private static final String DB_NAME="English";
    * */
  /*  public void recover(InputStream inputStream) throws IOException{
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        inputStream.close();
    }*/

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
//        Log.i(TAG, "bakTest: be" );
        //官方代码
        if(isExternalStorageWritable()){
//            Log.i(TAG, "bakTest: 2");
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date data = new Date(System.currentTimeMillis());
            String timeSuffix = sdFormat.format(data)+"vkbk";
            String fileName = "myRhythmDB"+timeSuffix;
            File fileTo = getPublicDocumentStorageDir(fileName);

            saveFileToStorage(fileTo);
        }


    }

    public void saveFileToStorage(File fileTo) {
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
//        File file = new File("/sdcard/aaa");
        //如果getExternalStorageDirectory，则是获取SD目录（？区别，待）；
            if (!file.mkdirs()) {//mkdirs()
                Log.e(TAG, "Directory not created");
            }
        return file;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }



/*StorageManager sm = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        StorageVolume volume = sm.getPrimaryStorageVolume();
        Intent intent = volume.createAccessIntent(Environment.DIRECTORY_DOCUMENTS);
        startActivityForResult(intent, request_code);*/

}
