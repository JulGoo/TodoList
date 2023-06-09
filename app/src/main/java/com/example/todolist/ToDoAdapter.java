package com.example.todolist;

import static com.example.todolist.R.id.m_check_box;

import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder>{

    private ArrayList<ToDoModel> todoList = new ArrayList<>();

    private ToDoDB db;

    public ToDoAdapter(ToDoDB db) {
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.task_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ToDoModel item = todoList.get(position);

        boolean status = toBoolean(item.getStatus());

        holder.mCheckBox.setText(item.getTask());
        holder.mCheckBox.setChecked(toBoolean(item.getStatus()));

        //체크박스 체크 이벤트
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                //체크 상태: 1 아니면: 0
                if(isChecked){
                    db.updateStatus(item.getId(), 1);
                    holder.mCheckBox.setPaintFlags(holder.mCheckBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }else {
                    db.updateStatus(item.getId(), 0);
                    holder.mCheckBox.setPaintFlags(holder.mCheckBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
            }
        });

    }


    //체크 상태 boolean으로 변경
    private boolean toBoolean(int n){
        return n != 0;
    }

    //리스트에 데이터 담기
    public void setTasks(ArrayList<ToDoModel> todoList){

        this.todoList = todoList;
        notifyDataSetChanged();;
    }

    //할일 삭제
    public void removeItem(int position){

        todoList.remove(position);
        notifyItemRemoved(position);
    }

    //할일 개수
    @Override
    public int getItemCount() {
        return todoList.size();
    }

    //이이템 순서 변경
    public void onItemMove(int fromposition, int toposition) {
        Collections.swap(todoList, fromposition, toposition);
        notifyItemMoved(fromposition, toposition);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        CheckBox mCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mCheckBox = itemView.findViewById(R.id.m_check_box);
        }
    }

}
