package com.example.paperdemo;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.ikangtai.papersdk.Config;
import com.ikangtai.papersdk.PaperAnalysiserClient;
import com.ikangtai.papersdk.event.SampleBitmapAnalysisEventAdapter;
import com.ikangtai.papersdk.model.PaperCoordinatesData;
import com.ikangtai.papersdk.util.ImageUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

@RunWith(AndroidJUnit4.class)
public class PaperAnalysiserClientTest {
    private final Integer LOCK = 1;
    private StringBuffer stringBufferSuccess = new StringBuffer();
    private StringBuffer stringBufferFail = new StringBuffer();
    private Integer successNum = 0;
    private Integer failNum = 0;

    @Test
    public void analysisBitmap() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Config.setTestServer(true);
        PaperAnalysiserClient paperAnalysiserClient = new PaperAnalysiserClient(appContext, "100200", "6e1b1049a9486d49ba015af00d5a0", "xyl1@qq.com");
        analysisFileFolderBitmap(paperAnalysiserClient, "picture_qulified");
        analysisFileFolderBitmap(paperAnalysiserClient, "picture_unqulified");
//        analysisFileFolderBitmap(paperAnalysiserClient, "testpic");
    }

    public void analysisFileFolderBitmap(PaperAnalysiserClient paperAnalysiserClient, String fileFolderName) {
        stringBufferSuccess = new StringBuffer();
        stringBufferFail = new StringBuffer();
        successNum = 0;
        failNum = 0;
        String fileFolder = Environment.getExternalStorageDirectory().getPath() + File.separator + fileFolderName + File.separator;
        File file = new File(fileFolder);
        String[] nameList = file.list();
        if (nameList != null) {
            for (int i = 0; i < nameList.length; i++) {
                final String name = nameList[i];
                Bitmap bitmap = ImageUtil.getBitmapByFile(new File(fileFolder, name));
                paperAnalysiserClient.analysisBitmap(bitmap, new SampleBitmapAnalysisEventAdapter() {
                    @Override
                    public boolean analysisSuccess(PaperCoordinatesData paperCoordinatesData, Bitmap originSquareBitmap, Bitmap clipPaperBitmap) {
                        successNum++;
                        stringBufferSuccess.append(name + "  " + paperCoordinatesData.getBlurValue());
                        stringBufferSuccess.append("\n");
                        synchronized (LOCK) {
                            LOCK.notify();
                        }
                        return true;
                    }

                    @Override
                    public void analysisError(PaperCoordinatesData paperCoordinatesData, String errorResult, int code) {
                        super.analysisError(paperCoordinatesData, errorResult, code);
                        failNum++;
                        stringBufferFail.append(name + "  " + paperCoordinatesData.getBlurValue() + " " + code);
                        stringBufferFail.append("\n");
                        synchronized (LOCK) {
                            LOCK.notify();
                        }
                    }
                });
                try {
                    synchronized (LOCK) {
                        LOCK.wait();
                    }
                } catch (InterruptedException e) {
                    Assert.assertNotNull(e);
                }
            }
        }
        Log.d("xyl", "--------------fileFolderName----------------" + fileFolderName);
        Log.d("xyl", "successNum-------" + successNum);
        //Log.d("xyl", stringBufferSuccess.toString());
        Log.d("xyl", "failNum-------" + failNum);
        //Log.d("xyl", stringBufferFail.toString());
    }
}