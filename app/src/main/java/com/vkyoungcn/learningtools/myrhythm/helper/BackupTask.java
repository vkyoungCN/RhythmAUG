package com.vkyoungcn.learningtools.myrhythm.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.BackUpActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupTask extends AsyncTask<String, Void, Integer> {
    private static final String TAG = "BackupTask";
    private static final String COMMAND_BACKUP = "backupDatabase";
    public static final String COMMAND_RESTORE = "restoreDatabase";

    private static final int BACKUP_SUCCESS = 1;
    public static final int RESTORE_SUCCESS = 2;
    private static final int BACKUP_ERROR = 3;
    public static final int RESTORE_NOFEERROR = 4;

    private Context mContext;

    public BackupTask(Context context) {
        this.mContext = context;
    }
    @Override
    protected Integer doInBackground(String... strings) {


        File dbFile = mContext.getDatabasePath("MyRhythm");
        // 创建目录
        File exportDir = new File(Environment.getExternalStorageDirectory(),
                "MyRhythmBk");
        if (!exportDir.exists()) {
            exportDir.mkdir();//1/exportDir.createNewFile()；2、mkdirs()建立多级目录
        }

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date data = new Date(System.currentTimeMillis());
        String timeSuffix = sdFormat.format(data)+"vkbk";
        File backup = new File(exportDir, dbFile.getName()+timeSuffix);
        String command = strings[0];
        if (command.equals(COMMAND_BACKUP)) {
            try {
                Log.i(TAG, "doInBackground: bak="+backup.getPath());
                backup.createNewFile();
                Log.i(TAG, "doInBackground: created");
                fileCopy(dbFile, backup);
                Log.i(TAG, "doInBackground: copied");
                return BACKUP_SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                return BACKUP_ERROR;
            }
        } else if (command.equals(COMMAND_RESTORE)) {
            try {
                fileCopy(backup, dbFile);
                return RESTORE_SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                return RESTORE_NOFEERROR;
            }
        } else {
            return null;
        }
    }


    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        switch (result) {
            case BACKUP_SUCCESS:
                Toast.makeText(mContext, "备份成功", Toast.LENGTH_SHORT).show();
                break;
            case BACKUP_ERROR:
                Toast.makeText(mContext, "备份失败", Toast.LENGTH_SHORT).show();
                break;
            case RESTORE_SUCCESS:
                Toast.makeText(mContext, "恢复成功", Toast.LENGTH_SHORT).show();
                break;
            case RESTORE_NOFEERROR:
                Toast.makeText(mContext, "恢复失败", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }


    private void fileCopy(File inFile, File outFile) throws IOException {
        FileChannel inChannel = new FileInputStream(inFile).getChannel();
        FileChannel outChannel = new FileOutputStream(outFile).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            outChannel.close();
        }
    }
}