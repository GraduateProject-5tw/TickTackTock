package com.GraduateProject.TimeManagementApp.Adapters;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.GraduateProject.TimeManagementApp.R;
import com.GraduateProject.TimeManagementApp.TomatoClockActivity;

import java.sql.Array;
import java.util.Arrays;
import java.util.List;

public class SingleChoooiceAdapter extends ArrayAdapter {
    private Context context;
    private static String[] textResource;
    private static int time = 0;
    private int mCheckedPostion = -1;
    private RadioButton radioButton;

    public SingleChoooiceAdapter(Context context, int resource, String[] textResource){
        super(context, resource, textResource);
        this.context = context;
        this.textResource = textResource;
    }

    public void setSelectedIndex(int index){
        mCheckedPostion = index;
        Log.e("POSITION", " "+mCheckedPostion);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View myView = inflater.inflate(R.layout.list_item, null);
        TextView textView = (TextView)myView.findViewById(R.id.textview);
        radioButton = (RadioButton) myView.findViewById(R.id.radiobutton);

        textView.setText(textResource[position]);

        if(mCheckedPostion == position){
            radioButton.setChecked(true);
        }
        else{
            radioButton.setChecked(false);
        }

        return myView;
    }

    @Override
    public int getCount(){
        return super.getCount();
    }

    public RadioButton getRadioButton(){
        return radioButton;
    }
}
