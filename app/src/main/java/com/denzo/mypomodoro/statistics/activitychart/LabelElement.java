package com.denzo.mypomodoro.statistics.activitychart;

public class LabelElement {

    private final int ID;
    private final String Name;

    public LabelElement(int ID, String Name) {
        this.ID = ID;
        this.Name = Name;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return Name;
    }
}