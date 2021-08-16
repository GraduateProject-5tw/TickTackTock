package com.GraduateProject.TimeManagementApp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private static List<String> apps;
    private static List<AppInfo> appList;
    private Context context;

    AppListAdapter(List<AppInfo> appInfos, List<String> apps){
        this.appList=appInfos;
        this.apps = apps;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
        apps = LoadingApp.getAllowedApps();
        if(context==null)
            context=viewGroup.getContext();
        View view= LayoutInflater.from(context).inflate(R.layout.customapp_format,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i){
        viewHolder.appLogo.setImageDrawable(appList.get(i).appLogo);
        viewHolder.appName.setText(appList.get(i).appName);
        //禁用為true 開訪則false
        if(appList.get(i).appStatus) {
            viewHolder.appStatus.setImageResource(R.drawable.ic_lock);
            Log.e("EDIT", "add banned : " + appList.get(i).packageName);
        }
        else
            viewHolder.appStatus.setImageResource(R.drawable.ic_lock_open);

        viewHolder.appStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appList.get(i).appStatus) {
                    appList.get(i).appStatus = false;
                    viewHolder.appStatus.setImageResource(R.drawable.ic_lock_open);
                    apps.remove(appList.get(i).packageName);
                    Log.e("EDIT", "remove banned : " + appList.get(i).packageName);
                }else {
                    appList.get(i).appStatus = true;
                    viewHolder.appStatus.setImageResource(R.drawable.ic_lock);
                    apps.add(appList.get(i).packageName);
                    Log.e("EDIT", "add banned : " + appList.get(i).packageName);
                }
            }
        });
    }

    @Override
    public int getItemCount(){
        return appList.size();
    }

    public static List<String> getEditedApps(){
        return apps;
    }

    public static List<AppInfo> getEditedAppInfos(){
        return appList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView appLogo,appStatus;
        TextView appName;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            appName=itemView.findViewById(R.id.app_name);
            appStatus=itemView.findViewById(R.id.app_status);
            appLogo=itemView.findViewById(R.id.app_logo);
        }
    }


}
