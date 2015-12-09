package com.nova.trojan.trojannova.appmngr.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StoreAppFetcher {

    public static void main(String[] args) throws Exception {
        String url = "https://play.google.com/store/search?q=usc&c=apps";

        for (TrojanAppInfo app : getStoreApps()) {
            {
                 System.out.println(app.getAppId());
            }
        }
    }

    public static List<TrojanAppInfo> getStoreApps(String url, String token) {

        Document document = getDocument(url,token);

        Elements apps = document.select(".apps.small");

        List<TrojanAppInfo> storeApps = new ArrayList<>();

        for (Element app : apps) {
            {
                String title = app.select(".details .title").text();
                String price = app.select(".reason-set .display-price").text();
                String imgUrl = app.select(".cover img").attr("src");
                String appUrl = app.select(".details .title").attr("abs:href");
                if (StringUtil.isBlank(title) || StringUtil.isBlank(imgUrl) || StringUtil.isBlank(appUrl))
                    continue;

                TrojanAppInfo storeApp = new TrojanAppInfo(title, appUrl, imgUrl,price);
                storeApp.setDesc(app.select(".details .description").text());
                storeApp.setSubTitle(app.select(".subtitle").text());

                storeApps.add(storeApp);
            }
        }
        return storeApps;
    }

    private static Document getDocument(String s) {
        return getDocument(s,"");
    }

    public static Document getDocument(String url, String token){
        Document doc ;
        for (int i = 0; i < 3; i++) {
            try {
                Connection connection = Jsoup.connect(url);
                doc = StringUtil.isBlank(token)? connection.get() : connection.data("pagTok", token).post();
                if(doc != null ) return doc;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<TrojanAppInfo> getStoreApps() {

        // get  the xml config from github
        Document document = getDocument("https://gist.githubusercontent.com/SharmileeS/b9386c8f5b39cc911c0f/raw/trojanapplist.xml");
        if(document == null) return null;

        String xmlConfig = document.html();
        Document doc = Jsoup.parse(xmlConfig, "", Parser.xmlParser());

        // get all the page tokens for different page requests
        List<String> pagetokens = new ArrayList<>();
        for (Element e : doc.select("token")) {
            pagetokens.add(e.text());
        }


        HashSet<String> allowedAppIds = new HashSet<>();
        for (Element e : doc.select("id")) {
            allowedAppIds.add(e.text());
        }

        String url = doc.select("url").text();

        // get all unique apps from all the pages
        List<TrojanAppInfo> uniqueStoreApps = new ArrayList<>();

        for (String token : pagetokens) {
            // remove duplicates
            for (TrojanAppInfo app : getStoreApps(url, token)) {
                if(allowedAppIds.contains(app.getAppId())){
                    uniqueStoreApps.add(app);
                }
            }
        }

        return uniqueStoreApps;
    }


}