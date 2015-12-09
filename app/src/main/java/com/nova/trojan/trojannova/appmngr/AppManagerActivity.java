package com.nova.trojan.trojannova.appmngr;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Switch;

import com.nova.trojan.trojannova.R;
import com.nova.trojan.trojannova.utils.CacheStorage;
import com.nova.trojan.trojannova.appmngr.utils.StoreAppFetcher;
import com.nova.trojan.trojannova.appmngr.utils.TrojanAppInfo;
import com.nova.trojan.trojannova.utils.ContextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class AppManagerActivity extends ListActivity {
    private static final String KEY = "applist";
    private PackageManager packageManager = null;
    private List<TrojanAppInfo> applist = null;
    private ApplicationAdapter listadaptor = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appmngr);

        packageManager = getPackageManager();

        new LoadApplications().execute();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;

        switch (item.getItemId()) {
            case R.id.menu_about: {
                displayAboutDialog();

                break;
            }
            default: {
                result = super.onOptionsItemSelected(item);

                break;
            }
        }
        return result;
    }

    private void displayAboutDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.appmngr_about_title));
        builder.setMessage(getString(R.string.appmngr_about_desc));

        builder.setNeutralButton("Good to know", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TrojanAppInfo app = applist.get(position);
        // toggle the switch
        Switch aSwitch = (Switch) v.findViewById(R.id.switch1);
        aSwitch.setChecked(app.getApplicationInfo() == null);
        // start ot install app
        Intent intent = (app.getApplicationInfo() != null) ?
                packageManager.getLaunchIntentForPackage(app.getAppId()) : new Intent(Intent.ACTION_VIEW, app.getUrl());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listadaptor != null) {
            listadaptor.notifyDataSetChanged();
        }
    }

    private static List<TrojanAppInfo> getAppList() {
        // Retrieve the list from internal storage
        return CacheStorage.get(KEY, new Callable<List<TrojanAppInfo>>() {
            public List<TrojanAppInfo> call() {
                return StoreAppFetcher.getStoreApps();
            }
        });
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {

            // get all store apps
            applist = getAppList();
            if(applist == null || applist.size() ==0){
                return null;
            }

            // get all installed apps
            List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            HashMap<String, ApplicationInfo> infoHashMap = new HashMap<>();
            for (ApplicationInfo applicationInfo : installedApps) {
                if (null != packageManager.getLaunchIntentForPackage(applicationInfo.packageName))
                    infoHashMap.put(applicationInfo.packageName, applicationInfo);
            }

            // add the installation status
            for (TrojanAppInfo trojanApp : applist) {
                ApplicationInfo applicationInfo = infoHashMap.get(trojanApp.getAppId());
                trojanApp.setApplicationInfo(applicationInfo);
            }

            listadaptor = new ApplicationAdapter(AppManagerActivity.this,
                    R.layout.appmngr_list_row, applist);

            return null;
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            setListAdapter(listadaptor);
            progress.dismiss();
            super.onPostExecute(result);

            if(applist == null || applist.size() ==0)
                ContextUtils.displayExceptionDialog("Check your network..", AppManagerActivity.this, new Callable<Void>() {
                    public Void call() {
                        finish();
                        return null;
                    }
                });
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(AppManagerActivity.this, null,
                    "Loading application info...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}