package com.example.androidapp.adapters;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.OnItemLongClickListener;
import com.example.androidapp.R;
import com.example.androidapp.databinding.ItemContainerTaskBinding;
import com.example.androidapp.models.ToDo;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private List<ToDo> todoList;
    
    public TaskAdapter(List<ToDo> todoList) {
        todoList = sortByPrio((ArrayList<ToDo>) todoList);
        this.todoList = todoList;
    }
    

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerTaskBinding itemContainerTaskBinding = ItemContainerTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TaskAdapter.TaskViewHolder(itemContainerTaskBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.setToDo(todoList.get(position));
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        ItemContainerTaskBinding binding;

        public TaskViewHolder(ItemContainerTaskBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        void setToDo(ToDo toDo) {
            binding.checkbox.setText(toDo.task);
            binding.checkbox.setChecked(toBoolean(toDo.status));
            binding.dueDate.setText(toDo.due);

            // Dodaj obsługę długiego kliknięcia
            itemView.setOnLongClickListener(v -> {
                toggleIsPinned(getAdapterPosition());
                return true;
            });

            if (toDo.isPinned()) {
                binding.checkbox.setBackgroundColor(Color.argb(230,28, 46, 70));
//                CardView cardView = findViewById(R.id.card_view);
//                cardView.setCardBackgroundColor(Color.argb(230,28, 46, 70));
//          to byłoby dobre ale nie mogę wywołać cardView poza activity
                Log.d("isPinned ","is true");
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        private void toggleIsPinned(int position) {
            ToDo toDo = todoList.get(position);
            toDo.setPinned(!toDo.isPinned());
            todoList = sortByPrio((ArrayList<ToDo>) todoList);
            notifyDataSetChanged();
        }

        private boolean toBoolean(int n) {
            return n != 0;
        }
    }

    private List<ToDo> sortByPrio(ArrayList<ToDo> list) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.sort((o1, o2) -> Boolean.compare(o2.isPinned(), o1.isPinned()));
        }
        return list;
    }
}
