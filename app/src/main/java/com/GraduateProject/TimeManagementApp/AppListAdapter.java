package com.GraduateProject.TimeManagementApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    List<AppInfo> appList;
    Context context;
    AppListAdapter(List<AppInfo> appInfos){
        this.appList=appInfos;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
        if(context==null)
            context=viewGroup.getContext();
        View view= LayoutInflater.from(context).inflate(R.layout.controllerview_tomato,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i){
        viewHolder.appLogo.setImageDrawable(appList.get(i).appLogo);
        viewHolder.appName.setText(appList.get(i).appName);
        if(appList.get(i).appStatus)
            viewHolder.appStatus.setImageResource(R.drawable.ic_lock);
        else
            viewHolder.appStatus.setImageResource(R.drawable.ic_lock_open);

        viewHolder.appStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appList.get(i).appStatus) {
                    appList.get(i).appStatus = false;
                    viewHolder.appStatus.setImageResource(R.drawable.ic_lock_open);

                }else {
                    appList.get(i).appStatus = true;
                    viewHolder.appStatus.setImageResource(R.drawable.ic_lock);
                }
            }
        });
    }

    @Override
    public int getItemCount(){
        return appList.size();
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
