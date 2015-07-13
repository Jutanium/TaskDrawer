package com.jutanium.tododrawer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Task {
    private final int id;
    private String title;
    private String details;
    private Date dateCreated;
    private Date dateExpires;

    public static class TaskPuts {

        public String title;
        public boolean hasTitle() {
            return title != null;
        }

        public String details;
        public boolean hasDetails() {
            return details != null;
        }

        public Date dateCreated;
        public boolean hasDateCreated() {
            return dateCreated != null;
        }

        public Date dateExpires;
        public boolean hasDateExpires() {
            return dateExpires != null;
        }
    }
    private static final String formatString = "MMddyyyyHHmmss";

    public Task(int id, TaskPuts parameters) {
        this.id = id;
       //if (parameters.hasTitle())
            this.title = parameters.title;
        //if (parameters.hasDetails())
            this.details = parameters.details;
        //if (parameters.hasDateExpires())
            this.dateExpires = parameters.dateExpires;
        //if (parameters.hasDateCreated())
            this.dateCreated = parameters.dateCreated;
    }
    public Task(int id, String title, String details, Date dateCreated, Date dateExpires) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.dateCreated = dateCreated;
        this.dateExpires = dateExpires;
    }

    public Task(int id, String title, String details, String dateCreated, String dateExpires)
    {
        this(id, title, details, stringToDate(dateCreated), stringToDate(dateExpires));
    }

    public Task(int id, String title, String details)
    {
        this.id = id;
        this.title = title;
        this.details = details;
        this.dateCreated = Calendar.getInstance().getTime();
    }

    public Task(int id)
    {
        this.id = id;
    }

    public String getDateCreatedString() {
        DateFormat df = new SimpleDateFormat(formatString);
        return df.format(dateCreated);
    }
    public void setDateCreated(String dateString) {
        this.dateCreated = stringToDate(dateString);
    }

    public String getDateExpiresString() {
        DateFormat df = new SimpleDateFormat(formatString);
        return df.format(dateExpires);
    }
    public void setDateExpires(String dateString) {
        this.dateExpires = stringToDate(dateString);
    }



    private static Date stringToDate(String dateString) {
        DateFormat df = new SimpleDateFormat(formatString);
        Date date = null;
        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public Date getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(Date date) {
        this.dateCreated = date;
    }

    public Date getDateExpires() {
        return dateExpires;
    }
    public void setDateExpires(Date date) {
        this.dateExpires = date;
    }

    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }
}
