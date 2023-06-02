package com.example.androidapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.databinding.ItemContainerTaskBinding;
import com.example.androidapp.models.ToDo;
import com.example.androidapp.utilities.Constants;
import com.example.androidapp.utilities.PreferenceManager;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private List<ToDo> todoList;
    private PreferenceManager preferenceManager;

    public TaskAdapter(List<ToDo> todoList) {
        this.todoList = todoList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        preferenceManager = new PreferenceManager(parent.getContext());
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
//            binding.taskImage.setImageURI(toDo.imageURI);
//            Picasso.get().load(toDo.imageURI).into(binding.taskImage);
            if(toDo.taskImage != null) {
                binding.taskImage.setImageBitmap(toDo.taskImage);
            }
        }

        private boolean toBoolean(int n){
            return n!=0;
        }
    }
}
