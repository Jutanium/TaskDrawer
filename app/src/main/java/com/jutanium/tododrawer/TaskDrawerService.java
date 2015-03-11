package com.jutanium.tododrawer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

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

    private void deleteTask(int index) {

        taskLoader.deleteTask(taskList.get(index).id);
        taskList.remove(index);

    }
    private boolean updateNotification() {

        Intent addButtonIntent = new Intent(this, AddTaskDialogActivity.class);
        PendingIntent addButtonPendingIntent = PendingIntent.getActivity(MyApp.getContext(), 0, addButtonIntent, 0);

        Intent slideIntent = new Intent();
        slideIntent.setAction(NotificationDeleted);

        if (taskList.size() == 0) //Display an instruction notification
        {
            String exampleTitle = getResources().getString(R.string.example_title);
            String exampleText = getResources().getString(R.string.example_text);
            Notification notification = new NotificationCompat.Builder(MyApp.getContext())
                    .setContentTitle(exampleTitle)
                    .setContentText(exampleText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(exampleText))
                    .setSmallIcon(R.drawable.task)
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

        Intent deleteButtonIntent = new Intent();
        deleteButtonIntent.setAction(DeleteButtonPressed);
        PendingIntent deleteButtonPendingIntent = PendingIntent.getBroadcast(MyApp.getContext(), 0, deleteButtonIntent, 0);

        Notification notification = new NotificationCompat.Builder(MyApp.getContext())
                .setContentTitle(task.title)
                .setContentText(task.details)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(task.details))
                .setSmallIcon(R.drawable.task)
                .addAction(new NotificationCompat.Action(R.drawable.plus, "Add Task", addButtonPendingIntent))
                .addAction(new NotificationCompat.Action(R.drawable.checkmark, "Done", deleteButtonPendingIntent))
                .setDeleteIntent(PendingIntent.getBroadcast(MyApp.getContext(), 0, slideIntent, 0))
                .build();

        notificationManager.notify(0, notification);

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
                addTask(intent.getStringExtra("title"), intent.getStringExtra("details"));
            }
        }
    }
}


