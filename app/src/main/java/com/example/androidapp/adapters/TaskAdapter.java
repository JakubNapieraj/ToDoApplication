package com.example.androidapp.adapters;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.androidapp.ItemTouchHelperAdapter;
import com.example.androidapp.ItemTouchHelperCallback;
import com.example.androidapp.databinding.ItemContainerTaskBinding;
import com.example.androidapp.models.ToDo;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> implements ItemTouchHelperAdapter {

    private List<ToDo> todoList;
    
    public TaskAdapter(List<ToDo> todoList, ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
        todoList = sortByPrio((ArrayList<ToDo>) todoList);
        this.todoList = todoList;
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(null);
    }

    private final ItemTouchHelper itemTouchHelper;

    @Override
    public void onItemDismiss(int position) {
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerTaskBinding itemContainerTaskBinding = ItemContainerTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        TaskViewHolder viewHolder = new TaskViewHolder(itemContainerTaskBinding);
        itemTouchHelper.attachToRecyclerView((RecyclerView) parent); // Attach the ItemTouchHelper to the RecyclerView
        return viewHolder;
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
