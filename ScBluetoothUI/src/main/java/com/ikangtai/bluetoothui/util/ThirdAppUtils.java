package com.ikangtai.bluetoothui.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

public class ThirdAppUtils {
    /**
     * URL类别为淘宝
     */
    public static final int URL_TAOBAO = 1;
    /**
     * URL类别为天猫
     */
    public static final int URL_TIANMAO = 2;
    /**
     * URL类别为京东
     */
    public static final int URL_JD = 3;

    /**
     * URL 类别为淘宝优惠券
     */

    public static final int URL_TAOBAO_QUAN = 4;

    /**
     * URL类别为淘宝客
     */
    public static final int URL_TAOBAO_CLICK = 5;


    public static String mJDMall = "com.jingdong.app.mall";

    public static String mTaoBao = "com.taobao.taobao";

    public static String mTianMall = "com.tmall.wireless";

    public static String mWeiXin = "com.tencent.mm";
    public static String mSina = "com.sina.weibo";
    public static String mQQ = "com.tencent.mobileqq";

    /**
     * 判断URL类别
     *
     * @param url
     * @return
     */
    public static int handleUrl(String url) {

        if (url.contains("click.taobao.com")) {
            return URL_TAOBAO_CLICK;
        }

        if (url.contains("taoquan.taobao.com")) {
            return URL_TAOBAO_QUAN;
        }

        if (url.contains("taobao.com")) {
            return URL_TAOBAO;
        }
        if (url.contains("tmall.com")) {
            return URL_TIANMAO;
        }

        if (url.contains("jd.com")) {
            return URL_JD;
        }


        return 0;
    }

    /**
     * 商品转化为淘宝优惠券schema
     *
     * @param url
     * @return
     */
    public static String getTaoBaoQuan(String url) {

        if (url.contains("taoquan.taobao.com")) {
            if (url.startsWith("https")) {
                return url.replace("https", "taobao");
            }
            if (url.startsWith("http")) {
                return url.replace("http", "taobao");
            }
        }

        return null;
    }

    /**
     * 商品转化为淘宝客schema
     *
     * @param url
     * @return
     */
    public static String getTaoBaoClick(String url) {

        if (url.contains("click.taobao.com")) {
            if (url.startsWith("https")) {
                return url.replace("https", "taobao");
            }
            if (url.startsWith("http")) {
                return url.replace("http", "taobao");
            }
        }

        return null;
    }

    /**
     * 商品转化为淘宝schema
     *
     * @param url
     * @return
     */
    public static String getTaoBaoId(String url) {
        if (url.contains("taobao.com") || url.contains("tmall.com")) {
            String tag = "&id=";
            String result = null;
            if (url.contains(tag)) {
                int index = url.indexOf(tag);
                result = url.substring(index + tag.length());
                if (result.contains("&")) {
                    int firstPos = result.indexOf("&");
                    result = result.substring(0, firstPos);
                }
            }
            if (TextUtils.isEmpty(result)) {
                return null;
            }

            return "taobao://item.taobao.com/item.htm?id=" + result;
        }
        return null;
    }

    /**
     * 商品转化为京东schema
     *
     * @param url
     * @return
     */
    public static String getJDId(String url) {
        if (url.contains("jd.com")) {
            String startTag = "product/";
            String endTag = ".html";
            String result = null;
            if (url.contains(startTag) && url.contains(endTag)) {
                int index = url.indexOf(startTag);
                int lastIndex = url.indexOf(endTag);
                result = url.substring(index + startTag.length(), lastIndex);
                System.out.println(result);
            }
            if (TextUtils.isEmpty(result)) {
                return null;
            }

            return "openApp.jdMobile://virtual?params={\"category\":\"jump\",\"des\":\"productDetail\",\"skuId\":\""
                    + result
                    + "\",\"sourceType\":\"JSHOP_SOURCE_TYPE\",\"sourceValue\":\"JSHOP_SOURCE_VALUE\"}";
        }
        return null;
    }

    /**
     * 商品转化为天猫schema
     *
     * @param url
     * @return
     */
    public static String getTianMaoId(String url) {
        if (url.contains("taobao.com") || url.contains("tmall.com")) {
            String tag = "&id=";
            String result = null;
            if (url.contains(tag)) {
                int index = url.indexOf(tag);
                result = url.substring(index + tag.length());
                if (result.contains("&")) {
                    int firstPos = result.indexOf("&");
                    result = result.substring(0, firstPos);
                }
            }
            if (TextUtils.isEmpty(result)) {
                return null;
            }
            return "tmall://tmallclient/?{\"action\":\"item:id=" + result + "\"}";
        }
        return null;
    }

    public static boolean isWeiXinAppExist(Context mContext) {
        return checkPackage(mContext, mWeiXin);
    }

    public static boolean isSinaAppExist(Context mContext) {
        return checkPackage(mContext, mSina);
    }

    public static boolean isTaoBaoAppExist(Context mContext) {
        return checkPackage(mContext, mTaoBao);
    }

    public static boolean isJDAppExist(Context mContext) {
        return checkPackage(mContext, mJDMall);
    }

    public static boolean isTMAppExist(Context mContext) {
        return checkPackage(mContext, mTianMall);
    }

    public static boolean checkPackage(Context mContext, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        try {
            mContext.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static void handleShop(Context context, String url) {
        if (!TextUtils.isEmpty(url)) {
            String schemalUrl = null;
            int urlType = ThirdAppUtils.handleUrl(url);
            if (urlType == ThirdAppUtils.URL_TAOBAO || urlType == ThirdAppUtils.URL_TIANMAO) {
                //优先打开天猫/淘宝/浏览器
                if (ThirdAppUtils.isTMAppExist(context)) {
                    schemalUrl = ThirdAppUtils.getTianMaoId(url);
                } else if (ThirdAppUtils.isTaoBaoAppExist(context)) {
                    schemalUrl = ThirdAppUtils.getTaoBaoId(url);
                }
            } else if (urlType == ThirdAppUtils.URL_JD) {
                if (ThirdAppUtils.isJDAppExist(context)) {
                    schemalUrl = ThirdAppUtils.getJDId(url);
                }
            } else if (urlType == ThirdAppUtils.URL_TAOBAO_QUAN) {
                if (ThirdAppUtils.isTaoBaoAppExist(context)) {
                    schemalUrl = ThirdAppUtils.getTaoBaoQuan(url);
                }
            } else if (urlType == ThirdAppUtils.URL_TAOBAO_CLICK) {
                if (ThirdAppUtils.isTaoBaoAppExist(context)) {
                    schemalUrl = ThirdAppUtils.getTaoBaoClick(url);
                }
            }
            if (TextUtils.isEmpty(schemalUrl)) {
                Uri shoppingUri = Uri.parse(url);
                Intent shoppingIntent = new Intent(Intent.ACTION_VIEW, shoppingUri);
                shoppingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(shoppingIntent);
            } else {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(schemalUrl);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }


    }


    public static void openSetting(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
            context.startActivity(intent);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } else {
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(localIntent);
        }
    }

}

