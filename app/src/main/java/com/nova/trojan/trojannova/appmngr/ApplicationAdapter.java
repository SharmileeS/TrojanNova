package com.nova.trojan.trojannova.appmngr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.nova.trojan.trojannova.R;
import com.nova.trojan.trojannova.appmngr.utils.TrojanAppInfo;

import java.util.List;

public class ApplicationAdapter extends ArrayAdapter<TrojanAppInfo> {
    private PackageManager packageManager;

    private View.OnClickListener switchClickListener =  new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            // you might keep a reference to the CheckBox to avoid this class cast
            boolean checked = ((Switch)v).isChecked();
            int position=(Integer)v.getTag();
            onSwitchButtonClick(checked, position);
        }
    };

    // View lookup cache
    private class ViewHolder {
        TextView appName;
        TextView appPrice;
        TextView packageName;
        ImageView iconview;
        Switch aSwitch;

        public ViewHolder(View row) {
            this.appName = (TextView) row.findViewById(R.id.app_name);
            this.appPrice = (TextView) row.findViewById(R.id.app_price);
            this.packageName = (TextView) row.findViewById(R.id.app_package);
            this.iconview =  (ImageView) row.findViewById(R.id.app_icon);
            this.aSwitch =   (Switch) row.findViewById(R.id.switch1);
            this.aSwitch.setOnClickListener(switchClickListener);
        }
    }

    public ApplicationAdapter(Context context, int textViewResourceId,
                              List<TrojanAppInfo> appsList) {
        super(context, textViewResourceId, appsList);
        packageManager = context.getPackageManager();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final TrojanAppInfo appInfo = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.appmngr_list_row, null);

            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // for proper image download and loading
         AQuery aq = new AQuery(convertView);

        // Populate the data into the template view using the data object
        ApplicationInfo applicationInfo = appInfo.getApplicationInfo();

        // if app is installed
        if(null != applicationInfo) {
            viewHolder.appName.setText(applicationInfo.loadLabel(packageManager));
            viewHolder.packageName.setText(applicationInfo.packageName);
            viewHolder.iconview.setImageDrawable(applicationInfo.loadIcon(packageManager));
            viewHolder.appPrice.setText("Installed");
            viewHolder.aSwitch.setChecked(true);

        }
        else {
            viewHolder.appName.setText(appInfo.getTitle());//data.loadLabel(packageManager));
            viewHolder.packageName.setText(appInfo.getAppId());//data.packageName);
            aq.id(viewHolder.iconview).image(appInfo.getImgurl(), false, true); // cache it in file
            viewHolder.appPrice.setText(appInfo.getPrice());
            viewHolder.aSwitch.setChecked(false);
        }

        viewHolder.aSwitch.setTag(position);

        // Return the completed view to render on screen
        return convertView;
    }

    public void onSwitchButtonClick( boolean checked, int position) {
        TrojanAppInfo appInfo = getItem(position);
        if(!checked ){
            if(packageManager.getLaunchIntentForPackage(appInfo.getAppId()) == null ) return;
            // uninstall
            Uri packageURI = Uri.parse("package:"+appInfo.getAppId());
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            getContext().startActivity(uninstallIntent);
        }else{
            // open app or install
            Intent intent = (appInfo.getApplicationInfo()!=null) ?
                    packageManager.getLaunchIntentForPackage(appInfo.getAppId()) : new Intent(Intent.ACTION_VIEW,appInfo.getUrl());
            getContext().startActivity(intent);
        }
    }
}