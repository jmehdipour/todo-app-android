package com.example.todoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.idescout.sql.SqlScoutServer;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AddTaskDialog.AddNewTaskCallback, TaskAdapter.TaskItemEventListener, EditTaskDialog.EditTaskCallback {
    private SqLiteHelper sqLiteHelper;
    private static final String TAG = "MainActivity";
    private TaskAdapter taskAdapter = new TaskAdapter(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SqlScoutServer.create(this, getPackageName());
        sqLiteHelper = new SqLiteHelper(this);
        final Task task = new Task();
        task.setTitle("test title");
        task.setCompleted(false);
        long result = sqLiteHelper.addTask(task);
        Log.i(TAG, "onCreate: "+ result);

        RecyclerView recyclerView = findViewById(R.id.rv_main_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(taskAdapter);

        final EditText searchEt = findViewById(R.id.et_main_search);
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0){
                    List<Task> tasks = sqLiteHelper.searchInTasks(charSequence.toString());
                    taskAdapter.setItems(tasks);
                }else{
                    List<Task> tasks = sqLiteHelper.getTasks();
                    taskAdapter.setItems(tasks);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        List<Task> tasks = sqLiteHelper.getTasks();
        taskAdapter.addItems(tasks);

        View addNewTaskFab = findViewById(R.id.fab_main_addNewTask);
        addNewTaskFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddTaskDialog dialog =  new AddTaskDialog();
                dialog.show(getSupportFragmentManager(), null);
            }
        });

        View clearTasksBtn = findViewById(R.id.iv_main_clearTasks);
        clearTasksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sqLiteHelper.clearAllTasks();
                taskAdapter.clearItems();
            }
        });
    }

    @Override
    public void onNewTask(Task task) {
        long taskId = sqLiteHelper.addTask(task);
        if (taskId != -1){
            task.setId(taskId);
            taskAdapter.addItem(task);
        }else{
            Log.e(TAG, "onNewTask: task did not inserted");
        }

    }

    @Override
    public void onDeleteButtonClicked(Task task) {
        int result = sqLiteHelper.deleteTask(task);
        if (result>0){
            taskAdapter.removeItem(task);
        }
    }

    @Override
    public void onItemLongPress(Task task) {
        EditTaskDialog editTaskDialog =  new EditTaskDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("task", task);
        editTaskDialog.setArguments(bundle);
        editTaskDialog.show(getSupportFragmentManager(), null);

    }

    @Override
    public void onItemCheckedChange(Task task) {
        sqLiteHelper.updateTask(task);
    }

    @Override
    public void onEditTask(Task task) {
        int result = sqLiteHelper.updateTask(task);
        if (result > 0){
            taskAdapter.updateItem(task);
        }
    }
}