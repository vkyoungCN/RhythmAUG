package com.vkyoungcn.learningtools.myrhythm;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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




}
