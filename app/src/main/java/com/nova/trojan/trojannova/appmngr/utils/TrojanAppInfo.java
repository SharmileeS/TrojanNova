package com.nova.trojan.trojannova.appmngr.utils;

import android.content.pm.ApplicationInfo;
import android.net.Uri;

import java.io.Serializable;

public class TrojanAppInfo  implements Serializable{

    private static final String STORE_URL_PREFIX = "https://play.google.com/store/apps/details?id=";

    private String title;
    private String desc;
    private String imgUrl;
    private String appId;

    public String getPrice() {
        return price;
    }

    private String price;

    public TrojanAppInfo(String title, String appUrl, String imgUrl, String price) {
        this.title = title;
        this.imgUrl = imgUrl;
        this.price = price;

        int idIndex = appUrl.indexOf("?id=");
        this.appId = idIndex > -1 ? appUrl.substring(idIndex + 4) : "";
    }

    public ApplicationInfo getApplicationInfo() {
        return installed;
    }

    public void setApplicationInfo(ApplicationInfo installed) {
        this.installed = installed;
    }

    private ApplicationInfo installed;

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    private String subTitle;

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getAppId() {
        return appId;
    }

    public String getImgurl() {
        return imgUrl;
    }

    public Uri getUrl() {
        return Uri.parse(TrojanAppInfo.STORE_URL_PREFIX + appId);
    }

    public String getSubTitle() {
        return subTitle;
    }
}
