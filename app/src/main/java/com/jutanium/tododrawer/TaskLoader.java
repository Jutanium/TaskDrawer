package com.jutanium.tododrawer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Dan Jutan on 3/10/2015.
 */
public class TaskLoader {
    private static final String PREFS_METADATA = "MetaPrefs";
    private static final String PREFS_TODOS = "TodosPrefs";

    private static final String titleAccessor = "t:";
    private static final String descriptionAccessor = "d:";

    private SharedPreferences todosPrefs;
    private SharedPreferences metaPrefs;

    public TaskLoader(Context context) {
        todosPrefs = context.getSharedPreferences(PREFS_TODOS, 0);
        metaPrefs = context.getSharedPreferences(PREFS_METADATA, 0);
    }

    public int getLastId() {
       return metaPrefs.getInt("lastId", 0);
    }

    public ArrayList<Task> loadFromStorage() {

        ArrayList<Task> newList = new ArrayList<Task>();

        Map<String, ?> storage = todosPrefs.getAll();

        for (String key : storage.keySet()) {

            String[] values = new String[2];
            values = todosPrefs.getStringSet(key, Collections.<String>emptySet()).toArray(values);
            Task task = new Task();
            for (String value : values) {

                switch (String.valueOf(value.toCharArray(), 0, 2)) {
                    case titleAccessor:
                        task.title = value.substring(2);
                        break;
                    case descriptionAccessor:
                        task.details = value.substring(2);
                        break;
                }
            }

            task.id = Integer.valueOf(key);
            newList.add(task);
        }

        return newList;
    }

    public void addTask(Task task) {
        metaPrefs.edit()
                .putInt("lastId", task.id)
                .apply();

        todosPrefs.edit()
                .putStringSet(String.valueOf(task.id), new HashSet<String>(Arrays.asList(
                        titleAccessor + task.title,
                        descriptionAccessor + task.details)))
                .apply();
    }

    public void editTask(int id, Task task) {
        todosPrefs.edit()
                .putStringSet(String.valueOf(id), new HashSet<String>(Arrays.asList(
                        titleAccessor + task.title,
                        descriptionAccessor + task.details)))
                .apply();
    }
    public void deleteTask(int id) {
        todosPrefs.edit()
                .remove(String.valueOf(id))
                .apply();
    }
}
