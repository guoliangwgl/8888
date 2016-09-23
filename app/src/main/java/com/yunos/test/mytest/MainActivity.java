package com.yunos.test.mytest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {
    String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,BuildConfig.DEBUG+"");
        if (BuildConfig.DEBUG) Log.d("MainActivity", getDefaultDeviceName());

    }
    public String getDefaultDeviceName() {
        int vendorid = 30242;
        String ott_model = SystemProperties.get("ro.product.model");
        String vendorID = Integer.toHexString(vendorid);
        if (BuildConfig.DEBUG) Log.d("MainActivity", vendorID);
        String configFile = "/system/custom/"+vendorID+"/model_config.xml";
        File config = new File(configFile);
        if(config.exists() && config.canRead()){
            if (BuildConfig.DEBUG) Log.d("MainActivity", "config file:" + config.toString());
            ott_model = getConfigModel(config);
        }else {
            String supVendorId = vendorID.substring(0,3);
            if (BuildConfig.DEBUG) Log.d("MainActivity", supVendorId);
            String supconfigFile = "/system/custom/"+supVendorId+"/model_config.xml";
            config = new File(supconfigFile);
            if(config.exists() && config.canRead()){
                if (BuildConfig.DEBUG) Log.d("MainActivity", "config file:" + config.toString());
                ott_model = getConfigModel(config);
            }else{
                if (BuildConfig.DEBUG) Log.d("MainActivity", "no model config file exist");
            }
        }
        return ott_model;
    }

    private String getConfigModel(File file) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            InputStream ins = new FileInputStream(file.toString());
            parser.setInput(ins, "UTF-8");
            String Model = null;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String item = parser.getName();
                        if (item.equals("Model")) {
                            Model = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
            ins.close();
            if (Model != null) {
                return Model;
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private double getExSdAvailable(){
        String state = Environment.getStorageState(new File("/mnt/external_sd"));
        if(state.equals(Environment.MEDIA_MOUNTED)){
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long  blockSize = sf.getBlockSizeLong();
            long  blockCount = sf.getBlockCountLong();
            long  availCount = sf.getAvailableBlocksLong();
            Log.d(TAG,"block size is:"+blockSize+";availCount is:"+availCount);
            Log.d(TAG,"getExSdAvailable is:"+formatFreeSize(blockSize*availCount));
            return formatFreeSize(blockSize*availCount);
        }
        return 0;
    }


    private double formatFreeSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        double freeSize = (double) size / gb;

        Log.d(TAG, "formatFreeSize, before : " + size + " free gb : " + freeSize);
        return freeSize;
    }

    private int formatTotalSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        float fTotal = (float) size / gb;
        int nTotal = (int) Math.pow(2, Math.max(2, Math.ceil(Math.log(fTotal) / Math.log(2))));

        if (nTotal < 4) {
            Log.d(TAG, "formatTotalSize, total flash size less than 4G, change it to 4 : " + nTotal);
            nTotal = 4;
        }

        Log.d(TAG, "formatTotalSize, size : " + size + " total gb : " + fTotal + " total flash size : " + nTotal);
        return nTotal;
    }
}
