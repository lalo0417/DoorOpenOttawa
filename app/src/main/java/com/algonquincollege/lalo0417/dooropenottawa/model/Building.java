package com.algonquincollege.lalo0417.dooropenottawa.model;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CalebLalonde on 2016-11-07.
 */

public class Building {
    private int buildingID;
    private String name;
    private String address;
    private String image;
    private String description;
    private List<String> openHours = new ArrayList<>();
    private Bitmap bitmap;

    public String getAddress() { return address; }
    public void setAddress( String address ) {
        this.address = address + " Ottawa, Ontario";
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getBuildingID() { return buildingID; }
    public void setBuildingID(int buildingID) { this.buildingID = buildingID; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getDescription() { return description; }
    public void setDescription(String description) {this.description = description;}
    public Bitmap getBitmap() { return bitmap; }
    public void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }
    public List<String> getOpenHours( ) { return openHours; }
    public void addDate( String date ) { this.openHours.add(date); }
}