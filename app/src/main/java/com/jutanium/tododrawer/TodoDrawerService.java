package com.jutanium.tododrawer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


//OK, THIS IS OUR APP'S GAMEPLAN:
//POPULATE ARRAYLIST OF TODOS FROM STORAGE
    //IF THERE IS NO STORAGE, ADD A NEW TTODO THAT SAYS, "ADD MY FIRST TTODO"
//SEND OUT A NOTIFICATION OF THE FIRST TTODO
//WHEN THE TTODO IS DELETED, SHOW THE NEXT TTODO
//WHEN THE DONE BUTTON IS PRESSED, CHANGE THE TTODO'S DONE PROPERTY TO FALSE
//WHEN THE ADD NEW TTODO BUTTON IS PRESSED, SHOW A DIALOG WITH A TEXT BUTTON AND A SUBMIT BUTTON
//WHEN THE SUBMIT BUTTON OF THE DIALOG IS PRESSED, ADD A NEW TTODO TO STORAGE AND TO THE ARRAY

public class TodoDrawerService extends Service {
    public static final String PREFS_METADATA = "MetaPrefs";
    public static final String PREFS_TODOS = "TodosPrefs";
    public static final String NotificationDeleted = "NotificationDeleted";
    public static final String DeleteButtonPressed = "DeleteButtonPressed";
    public static final String DialogButtonPressed = "DialogButtonPressed";

    private SharedPreferences todosPrefs;
    private SharedPreferences metaPrefs;
   // private Map<Integer, Todo> todoMap;
    private ArrayList<Todo> todoList;
    private int lastId;
    private NotificationManager notificationManager;
    private NotificationDeletedBroadcastReceiver notificationDeletedBroadcastReceiver;
    private DeleteButtonBroadcastReceiver deleteButtonBroadcastReceiver;
    private DialogButtonBroadcastReceiver dialogButtonBroadcastReceiver;

    public int currentIndex = 0;
    @Override
    //This is the constructor.
    public void onCreate() {
        Log("Constructed");
        todosPrefs = getSharedPreferences(PREFS_TODOS, 0);
        metaPrefs = getSharedPreferences(PREFS_METADATA, 0);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        todoList = loadFromStorage();
        lastId = getSharedPreferences(PREFS_METADATA, 0).getInt("lastId", 0);
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
    private ArrayList<Todo> loadFromStorage() {

        ArrayList<Todo> newList = new ArrayList<Todo>();

        Map<String, ?> storage = todosPrefs.getAll();

        for (String key : storage.keySet()) {
            String[] values = new String[2];
            values = todosPrefs.getStringSet(key, Collections.<String>emptySet()).toArray(values);

            if (values[0].charAt(0) == 't' && values[1].charAt(0) == 'd')
                newList.add(new Todo(Integer.valueOf(key), values[0].substring(2), values[1].substring(2)));
            else if (values[0].charAt(0) == 'd' && values[1].charAt(0) == 't')
                newList.add(new Todo(Integer.valueOf(key), values[1].substring(2), values[0].substring(2)));
        }

        return newList;
    }

    private void addTodo(String title, String details) {

        todoList.add(new Todo(++lastId, title, details));
        metaPrefs.edit()
                .putInt("lastId", lastId)
                .apply();

        todosPrefs.edit()
                .putStringSet(String.valueOf(lastId), new HashSet<String>(Arrays.asList("t:"+title, "d:"+details )))
                .apply();


        updateNotification();
    }

    private void deleteTodo(Todo todo) {
        deleteTodo(todo.id);
    }

    private void deleteTodo(int index) {

        todosPrefs.edit()
                .remove(String.valueOf(todoList.get(index).id))
                .apply();
        todoList.remove(index);

    }
    private boolean updateNotification() {

        Intent addButtonIntent = new Intent(this, AddTaskDialogActivity.class);
        PendingIntent addButtonPendingIntent = PendingIntent.getActivity(MyApp.getContext(), 0, addButtonIntent, 0);

        Intent slideIntent = new Intent();
        slideIntent.setAction(NotificationDeleted);

        if (todoList.size() == 0) //Display an instruction notification
        {
            String exampleTitle = getResources().getString(R.string.example_title);
            String exampleText = getResources().getString(R.string.example_text);
            Notification notification = new NotificationCompat.Builder(MyApp.getContext())
                    .setContentTitle(exampleTitle)
                    .setContentText(exampleText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(exampleText))
                    .setSmallIcon(R.drawable.task)
                    .addAction(new NotificationCompat.Action(R.drawable.checkmark, "Add Task", addButtonPendingIntent))
                    .setDeleteIntent(PendingIntent.getBroadcast(MyApp.getContext(), 0, slideIntent, 0))
                    .build();
            notificationManager.notify(0, notification);
            return true;
        }
        if (currentIndex >= todoList.size()) currentIndex = 0;
        Todo todo = todoList.get(currentIndex);
        if (todo == null)
            return false;


        //slideIntent.putExtra("id", index);

        Intent deleteButtonIntent = new Intent();
        deleteButtonIntent.setAction(DeleteButtonPressed);
        PendingIntent deleteButtonPendingIntent = PendingIntent.getBroadcast(MyApp.getContext(), 0, deleteButtonIntent, 0);

        Notification notification = new NotificationCompat.Builder(MyApp.getContext())
                .setContentTitle(todo.title)
                .setContentText(todo.details)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(todo.details))
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
        Log.i("TodoDrawerService", log);
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
                deleteTodo(currentIndex);
                updateNotification();
            }
        }

    private class DialogButtonBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("DialogButtonBroadcastReceiver", "Dialog button pressed");
            if (intent.hasExtra("title") && intent.hasExtra("details")) {
                addTodo(intent.getStringExtra("title"), intent.getStringExtra("details"));
            }
        }
    }
}


