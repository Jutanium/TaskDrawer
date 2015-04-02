package com.jutanium.tododrawer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class TaskDrawerService extends Service {

    public static final String NotificationDeleted = "NotificationDeleted";
    public static final String DeleteButtonPressed = "DeleteButtonPressed";
    public static final String DialogButtonPressed = "DialogButtonPressed";

   // private Map<Integer, Task> todoMap;
    private ArrayList<Task> taskList;
    private int lastId;
    private NotificationManager notificationManager;
    private NotificationDeletedBroadcastReceiver notificationDeletedBroadcastReceiver;
    private DeleteButtonBroadcastReceiver deleteButtonBroadcastReceiver;
    private DialogButtonBroadcastReceiver dialogButtonBroadcastReceiver;


    private TaskLoader taskLoader;
    public int currentIndex = 0;
    @Override
    //This is the constructor.
    public void onCreate() {
        Log("Constructed");

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        taskLoader = new TaskLoader(this);
        taskList = taskLoader.loadFromStorage();
        lastId = taskLoader.getLastId();

        notificationDeletedBroadcastReceiver = new NotificationDeletedBroadcastReceiver();
        registerReceiver(notificationDeletedBroadcastReceiver, new IntentFilter(NotificationDeleted));
        deleteButtonBroadcastReceiver = new DeleteButtonBroadcastReceiver();
        registerReceiver(deleteButtonBroadcastReceiver, new IntentFilter(DeleteButtonPressed));
        dialogButtonBroadcastReceiver = new DialogButtonBroadcastReceiver();
        registerReceiver(dialogButtonBroadcastReceiver, new IntentFilter(DialogButtonPressed));
        updateNotification();
    }

    @Override
    //This method fires when the service is started using startService()
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log("We got started by " + intent);


        ////bind to service. When the service binds, it calls ServiceConnection's method
        //Intent loadMessagesIntent = new Intent(this, LoadMessagesService.class);
        //bindService(loadMessagesIntent, connection, Context.BIND_AUTO_CREATE);

        // We want this service to continue running until it is explicitly stopped,
        // so return sticky.
        return START_STICKY;
    }

    @Override public void onDestroy () {
        unregisterReceiver(notificationDeletedBroadcastReceiver);
        unregisterReceiver(deleteButtonBroadcastReceiver);
        unregisterReceiver(dialogButtonBroadcastReceiver);
    }


    private void addTask(String title, String details) {

        Task newTask = new Task(++lastId, title, details);
        taskList.add(newTask);
        taskLoader.addTask(newTask);


        updateNotification();
    }

    private void editTask(int index, String title, String details) {
        Task task = new Task(taskList.get(index).id, title, details);
        taskLoader.editTask(task);
        taskList.add(index, task);
        updateNotification();
    }
    private void deleteTask(int index) {

        taskLoader.deleteTask(taskList.get(index).id);
        taskList.remove(index);

    }
    private boolean updateNotification() {

        Intent addButtonIntent = new Intent(this, AddTaskDialogActivity.class);
        PendingIntent addButtonPendingIntent = PendingIntent.getActivity(MyApp.getContext(), 0, addButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent slideIntent = new Intent();
        slideIntent.setAction(NotificationDeleted);

        if (taskList.size() == 0) //Display an instruction notification
        {
            String exampleTitle = getString(R.string.example_title);
            String exampleText = getString(R.string.example_text);
            Notification notification = new NotificationCompat.Builder(MyApp.getContext())
                    .setContentTitle(exampleTitle)
                    .setContentText(exampleText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(exampleText))
                    .setSmallIcon(R.drawable.task)
                    .setOngoing(true)
                    .addAction(new NotificationCompat.Action(R.drawable.plus, "Add Task", addButtonPendingIntent))
                    .setDeleteIntent(PendingIntent.getBroadcast(MyApp.getContext(), 0, slideIntent, 0))
                    .build();
            notificationManager.notify(0, notification);
            return true;
        }
        if (currentIndex >= taskList.size()) currentIndex = 0;
        Task task = taskList.get(currentIndex);
        if (task == null)
            return false;


        //slideIntent.putExtra("id", index);

        Intent editButtonIntent = new Intent(this, AddTaskDialogActivity.class);
        editButtonIntent.putExtra("index", currentIndex);
        editButtonIntent.putExtra("title", taskList.get(currentIndex).title);
        editButtonIntent.putExtra("details", taskList.get(currentIndex).details);

        PendingIntent editButtonPendingIntent = PendingIntent.getActivity(MyApp.getContext(), 0, editButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteButtonIntent = new Intent();
        deleteButtonIntent.setAction(DeleteButtonPressed);
        PendingIntent deleteButtonPendingIntent = PendingIntent.getBroadcast(MyApp.getContext(), 0, deleteButtonIntent, 0, PendingIntent.);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApp.getContext())
                .setContentTitle(task.title)
                .setContentText(task.details)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(task.details))
                .setSmallIcon(R.drawable.task)
                .addAction(new NotificationCompat.Action(R.drawable.plus, getString(R.string.add_task), addButtonPendingIntent))
                .addAction(new NotificationCompat.Action(R.drawable.edit, getString(R.string.edit_task), editButtonPendingIntent))
                .addAction(new NotificationCompat.Action(R.drawable.checkmark, getString(R.string.complete), deleteButtonPendingIntent))
                .setDeleteIntent(PendingIntent.getBroadcast(MyApp.getContext(), 0, slideIntent, 0));

        if (taskList.size() == 1)
            builder.setOngoing(true);



        notificationManager.notify(0, builder.build());

        return true;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void Log (String log) {
        Log.i("TaskDrawerService", log);
    }


    private class NotificationDeletedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("NotificationDeletedBroadcastReceiver", "Notification slid");

           /* if (intent.hasExtra("id")) {
                //Arrays.asList(todoMap.keySet().toArray(new Integer[todoMap.size()])).indexOf()
                int index = intent.getExtras().getInt("id");
                Log.i("NDR", String.valueOf(index));
                updateNotification(index + 1);
            }*/
            currentIndex++;
            updateNotification();
        }
    }

    private class DeleteButtonBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("DeleteButtonBroadcastReceiver", "Delete button pressed");
                deleteTask(currentIndex);
                updateNotification();
            }
        }

    private class DialogButtonBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("DialogButtonBroadcastReceiver", "Dialog button pressed");
            if (intent.hasExtra("title") && intent.hasExtra("details")) {
                int index = intent.getIntExtra("index", -1);
                if (index != -1) {
                    editTask(index, intent.getStringExtra("title"), intent.getStringExtra("details"));
                    return;
                }
                addTask(intent.getStringExtra("title"), intent.getStringExtra("details"));
            }
        }
    }
}


