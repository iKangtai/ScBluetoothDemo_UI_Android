package com.ikangtai.bluetoothui.upgrade;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.ikangtai.bluetoothsdk.util.BleParam;
import com.ikangtai.bluetoothsdk.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * desc
 *
 * @author xiongyl 2020/10/16 0:00
 */
public class OadFileUtil {
    private String downloadURL;
    private String downloadJsonURL;
    private Context context;
    private long downloadId;
    private int oadFileType;
    private String latestVer;

    public OadFileUtil(Context context, String latestVer, String downloadURL) {
        this.context = context;
        this.latestVer = latestVer;
        this.downloadJsonURL = downloadURL;
    }

    public void handleFirmwareImgABMsg(int imgType) {
        this.oadFileType = imgType;
        int firmwareImgAB = imgType;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(downloadJsonURL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject != null && firmwareImgAB == BleParam.FIRMWARE_IMAGE_REVERSION_A) {
            try {
                downloadURL = jsonObject.getString("B");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            LogUtils.i("目前固件使用版本A，下载升级固件B.");
            downloadFirmwareImage(BleParam.FIRMWARE_IMAGE_REVERSION_A);
        } else if (jsonObject != null && firmwareImgAB == BleParam.FIRMWARE_IMAGE_REVERSION_B) {
            try {
                downloadURL = jsonObject.getString("A");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            LogUtils.i("目前固件使用版本B，下载升级固件A.");
            downloadFirmwareImage(BleParam.FIRMWARE_IMAGE_REVERSION_B);
        } else {
            LogUtils.e("下载固件时出现错误，非A/B");
        }
    }

    private void downloadFirmwareImage(int type) {
        String filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getFileName(type, latestVer);
        File downloadFile = new File(filePath);
        if (downloadFile.exists()) {
            LogUtils.i("发现已下载了固件镜像文件 " + getFileName(type, latestVer) + ", 无需再次下载!");
            downloadId = -10001;
            Intent intent = new Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            context.sendBroadcast(intent);
            //downloadFile.delete();
        } else {
            //创建下载任务,downloadUrl就是下载链接
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadURL));
            //设置通知栏标题
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setTitle("OAD下载");
            request.setDescription("OAD二进制文件正在下载...");
            request.setAllowedOverRoaming(false);
            //指定下载路径和下载文件名
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, getFileNameTemp(type, latestVer));
            //获取下载管理器
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            //将下载任务加入下载队列，否则不会进行下载
            downloadId = downloadManager.enqueue(request);
            LogUtils.i("正在下载 ... downloadId = " + downloadId);
        }
    }

    public static String getFileName(int imgType, String version) {
        String fileName = String.format(imgType + "otaBinFile_complete_%s.img", version);
        return fileName.replaceAll("\\.", "_");
    }

    public static String getFileNameTemp(int imgType, String version) {
        String fileName = String.format(imgType + "oadBinFile_complete_temp_%s.img", version);
        return fileName.replaceAll("\\.", "_");
    }

    public long getDownloadId() {
        return downloadId;
    }

    public int getOadFileType() {
        return oadFileType;
    }

    public String getLatestVer() {
        return latestVer;
    }
}