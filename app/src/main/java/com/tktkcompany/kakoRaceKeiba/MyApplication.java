package com.tktkcompany.kakoRaceKeiba;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import java.io.File;

public class MyApplication extends Application implements Configuration.Provider {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeWorkManagerWithRecovery();
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(Log.INFO)
                .build();
    }

    private void initializeWorkManagerWithRecovery() {
        try {
            // This will trigger initialization if the provider is correctly set up
            WorkManager.getInstance(this);
            Log.i(TAG, "WorkManager initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize WorkManager, attempting recovery", e);
            
            // Check if it's a database-related failure
            if (isDatabaseCorruption(e)) {
                deleteWorkManagerDatabase();
                try {
                    // Try again after deleting the corrupted database
                    WorkManager.initialize(this, getWorkManagerConfiguration());
                    Log.i(TAG, "WorkManager recovered and initialized");
                } catch (Exception ex) {
                    Log.e(TAG, "Critical: Failed to re-initialize WorkManager", ex);
                }
            }
        }
    }

    private boolean isDatabaseCorruption(Throwable t) {
        if (t == null) return false;
        String message = t.getMessage();
        if (message != null && (message.contains("WorkDatabase") || message.contains("Room") || message.contains("SQLite"))) {
            return true;
        }
        return isDatabaseCorruption(t.getCause());
    }

    private void deleteWorkManagerDatabase() {
        try {
            Log.w(TAG, "Deleting WorkManager database files...");
            // WorkManager uses this specific database name
            deleteDatabase("androidx.work.workdb");
            
            // Also cleanup potential WAL/SHM files manually if deleteDatabase doesn't catch them
            File dbDir = new File(getFilesDir().getParent(), "databases");
            if (dbDir.exists()) {
                File[] files = dbDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().startsWith("androidx.work.workdb")) {
                            if (file.delete()) {
                                Log.d(TAG, "Deleted file: " + file.getName());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while deleting WorkManager database", e);
        }
    }
}
