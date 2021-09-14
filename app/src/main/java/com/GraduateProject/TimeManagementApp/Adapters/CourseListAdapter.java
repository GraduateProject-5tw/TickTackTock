package com.GraduateProject.TimeManagementApp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.GraduateProject.TimeManagementApp.R;
import com.GraduateProject.TimeManagementApp.ToDoModel;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import java.util.List;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.ViewHolder> {
    private static List<String> course_list;
    private static List<Integer> color_list;
    private static List<Integer> text_list;
    private Context context;

    public CourseListAdapter(List<String> course, List<Integer> color, List<Integer> text){
        course_list = course;
        color_list = color;
        text_list = text;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
        if(context==null)
            context=viewGroup.getContext();
        View view= LayoutInflater.from(context).inflate(R.layout.course_edit_format,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i){
        viewHolder.courseName.setText(course_list.get(i));
        viewHolder.courseName.setBackgroundColor(color_list.get(i));
        viewHolder.courseName.setTextColor(text_list.get(i));
        viewHolder.courseColor.setBackgroundColor(color_list.get(i));
        viewHolder.textColor.setBackgroundColor(text_list.get(i));

        viewHolder.courseColor.setOnClickListener(v -> ColorPickerDialogBuilder
                .with(context)
                .setTitle("選擇時間塊顏色")
                .initialColor(color_list.get(i))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(selectedColor -> Log.e("Color", "select" + Integer.toHexString(selectedColor)))
                .setPositiveButton("確定", (dialog, selectedColor, allColors) -> {
                    viewHolder.courseColor.setBackgroundColor(selectedColor);
                    viewHolder.courseName.setBackgroundColor(selectedColor);
                    color_list.set(i, selectedColor);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                })
                .build()
                .show());

        viewHolder.textColor.setOnClickListener(v -> ColorPickerDialogBuilder
                .with(context)
                .setTitle("選擇時間塊顏色")
                .initialColor(text_list.get(i))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(selectedColor -> Log.e("Color", "select" + Integer.toHexString(selectedColor)))
                .setPositiveButton("確定", (dialog, selectedColor, allColors) -> {
                    viewHolder.textColor.setBackgroundColor(selectedColor);
                    viewHolder.courseName.setTextColor(selectedColor);
                    text_list.set(i, selectedColor);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                })
                .build()
                .show());

        viewHolder.courseName.setOnClickListener(v -> {
            final EditText editText = new EditText(context);
            AlertDialog.Builder courseDialog = new AlertDialog.Builder(context);
            courseDialog.setTitle("編輯名稱");
            courseDialog.setView(editText);
            courseDialog.setPositiveButton("確定", ((dialogs, y) -> {
            }));
            AlertDialog alert = courseDialog.create();
            alert.show();
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x -> {
                if (editText.getText().toString().isEmpty()) {
                    Toast.makeText(context, "科目不可空白", Toast.LENGTH_SHORT).show();
                } else {
                    course_list.set(i, editText.getText().toString());
                    viewHolder.courseName.setText(course_list.get(i));
                    Log.e("COURSE", "selected course is " + course_list.get(i));
                    alert.dismiss();
                }
            }));
            alert.setCancelable(false);
            alert.setCanceledOnTouchOutside(false);
        });
    }

    @Override
    public int getItemCount() {
        return course_list.size();
    }

    public Context getContext() { return context; }

    public static List<String> getCourse_list(){
        return course_list;
    }

    public static List<Integer> getColor_list(){
        return color_list;
    }

    public static List<Integer> getText_list() { return text_list;}

    public void deleteCourse(int position) {
        course_list.remove(position);
        color_list.remove(position);
        text_list.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView courseName, courseColor, textColor;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            courseName=itemView.findViewById(R.id.course_name);
            courseColor=itemView.findViewById(R.id.course_color);
            textColor=itemView.findViewById(R.id.text_color);
        }
    }


}
