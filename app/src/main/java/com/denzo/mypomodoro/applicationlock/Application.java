package com.denzo.mypomodoro.applicationlock;

import android.graphics.drawable.Drawable;

final class Application implements Comparable<Application> {

    private final String packageName;
    private final String applicationName;
    private final Drawable applicationIcon;

    public Application(String packageName, String applicationName, Drawable applicationIcon) {
        this.packageName = packageName;
        this.applicationName = applicationName;
        this.applicationIcon = applicationIcon;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Drawable getApplicationIcon() {
        return applicationIcon;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public int compareTo(Application application) {
        return this.applicationName.toLowerCase().compareTo(application.applicationName.toLowerCase());
    }
}