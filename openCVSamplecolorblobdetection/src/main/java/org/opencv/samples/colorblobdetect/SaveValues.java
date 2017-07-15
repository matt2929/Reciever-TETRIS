package org.opencv.samples.colorblobdetect;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Matth on 7/11/2017.
 */

public class SaveValues {
    private int BlockSquare;
    private int SquareWidth;
    private int SquareHeight;
    private Calendar calendar;
    String filename = "";
    FileOutputStream outputStream;

    public SaveValues(int BlockSize, int SquareWidth, int SquareHeight) {
        this.BlockSquare = BlockSize;
        this.SquareWidth = SquareWidth;
        this.SquareHeight = SquareHeight;
        calendar =  Calendar.getInstance();
        filename = "I_SAW_" + calendar.getTime().toString()+".txt";

    }

    public void saveBarCode(Context context, ArrayList<String> colors) {
        try {

FileOutputStream fileOutputStream = new FileOutputStream(getAlbumStorageDir(context,filename));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            String string = "";
            for (int i = 0; i < colors.size(); i++) {
                string += i + ":" + colors.get(i) + "\n";
            }
            outputStreamWriter.append(string);
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

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    public File getAlbumStorageDir(Context context, String albumName) throws IOException {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir("CobraReceived"), albumName);
        if(!file.exists()){
            file.createNewFile();
        }
        if (!file.mkdirs()) {
            Log.e("file error", "Directory not created");
        }
        return file;
    }
}