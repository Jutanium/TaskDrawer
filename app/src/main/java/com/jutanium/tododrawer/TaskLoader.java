package com.jutanium.tododrawer;

import android.content.Context;
import android.content.SharedPreferences;

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
    private static final String detailsAccessor = "d:";
    private static final String createdAccessor = "c:";
    private static final String expiredAccessor = "e:";

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

            Task task = new Task(Integer.valueOf(key));
            for (String value : values) {

                switch (String.valueOf(value.toCharArray(), 0, 2)) {
                    case titleAccessor:
                        task.setTitle(value.substring(titleAccessor.length()));
                        break;
                    case detailsAccessor:
                        task.setDetails(value.substring(detailsAccessor.length()));
                        break;
                    case createdAccessor:
                        task.setDateCreated(value.substring(createdAccessor.length()));
                    case expiredAccessor:
                        task.setDateExpires(value.substring(expiredAccessor.length()));
                }
            }
            newList.add(task);
        }

        return newList;
    }

    public void addTask(Task task) {
        metaPrefs.edit()
                .putInt("lastId", task.getId())
                .apply();

        todosPrefs.edit()
                .putStringSet(String.valueOf(task.getId()), new HashSet<String>(taskToProperties(task)))
                .apply();
    }

    public void editTask(Task task) {
        todosPrefs.edit()
                .putStringSet(String.valueOf(task.getId()), new HashSet<String>(taskToProperties(task)))
                .apply();
    }
    public void deleteTask(int id) {
        todosPrefs.edit()
                .remove(String.valueOf(id))
                .apply();
    }

    private ArrayList<String> taskToProperties(Task task) {

        ArrayList<String> properties = new ArrayList<>();
        String title = task.getTitle();
        if (title != null) properties.add(titleAccessor + title);
        String details = task.getDetails();
        if (details != null) properties.add(detailsAccessor + details);
        String dateCreated = task.getDateCreatedString();
        if (dateCreated != null) properties.add(createdAccessor + dateCreated);
        String dateExpired = task.getDateExpiresString();
        if (dateExpired != null) properties.add(expiredAccessor + dateExpired);

        return properties;
    }
}
