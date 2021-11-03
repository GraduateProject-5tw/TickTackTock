package com.GraduateProject.TimeManagementApp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.GraduateProject.TimeManagementApp.Adapters.CourseListAdapter;
import com.GraduateProject.TimeManagementApp.Adapters.ToDoAdapter;


public class CourseTouchHelper extends ItemTouchHelper.SimpleCallback {

    private CourseListAdapter adapter;

    public CourseTouchHelper(CourseListAdapter adapter) {
        super(0, ItemTouchHelper.LEFT);
        this.adapter = adapter;
    }


    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getBindingAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            final Dialog leave = new Dialog(adapter.getContext());
            leave.requestWindowFeature(Window.FEATURE_NO_TITLE);
            leave.setCancelable(false);
            leave.setContentView(R.layout.activity_popup_yesnobutton);

            TextView title = (TextView) leave.findViewById(R.id.txt_tit);
            title.setText("刪除讀書科目");

            TextView content = (TextView) leave.findViewById(R.id.txt_dia);
            content.setText("請問是否要刪除此科目？");

            Button no = (Button) leave.findViewById(R.id.btn_no);
            no.setText("取消");
            no.setOnClickListener(v -> {
                adapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
                leave.dismiss();
            });

            Button yes = (Button) leave.findViewById(R.id.btn_yes);
            yes.setText("確定");
            yes.setOnClickListener(v -> {
                adapter.deleteCourse(position);
                leave.dismiss();
            });
            leave.show();
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        Drawable icon;
        ColorDrawable background;

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_delete_24);
        background = new ColorDrawable(Color.RED);

        assert icon != null;
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX < 0) { // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }
}