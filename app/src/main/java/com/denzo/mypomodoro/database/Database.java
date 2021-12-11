package com.denzo.mypomodoro.database;

import android.app.Activity;
import android.content.Context;


import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.denzo.mypomodoro.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@androidx.room.Database(entities = {Pomodoro.class, Activity.class}, version = 1)
public abstract class Database extends RoomDatabase {

    private static volatile Database database;
    public static final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private static Database buildDatabaseInstance(Context context) {
        if (database != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of " +
                    "this class.");
        }
        return Room.databaseBuilder(context, Database.class, Constants.DATABASE_NAME).build();
    }

    public static Database getInstance(Context context) {
        if (database == null) {
            synchronized (Database.class) {
                if (database == null) {
                    database = buildDatabaseInstance(context);
                }
            }
        }
        return database;
    }

    public abstract PomodoroDao pomodoroDao();

    public abstract ActivityDao activityDao();
}