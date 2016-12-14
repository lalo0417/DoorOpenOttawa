package com.algonquincollege.lalo0417.dooropenottawa.parsers;

import android.widget.ProgressBar;

import com.algonquincollege.lalo0417.dooropenottawa.model.Building;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CalebLalonde on 2016-11-08.
 */

public class BuildingJSONParser {

    public static List<Building> parseFeed(String content) {

        try {
            JSONObject jsonResponse = new JSONObject(content);
            JSONArray buildingArray = jsonResponse.getJSONArray("buildings");
            List<Building> buildingList = new ArrayList<>();

            for (int i = 0; i < buildingArray.length(); i++) {

                JSONObject obj = buildingArray.getJSONObject(i);
                Building building = new Building();

                building.setBuildingID(obj.getInt("buildingId"));
                building.setName(obj.getString("name"));
                building.setImage(obj.getString("image"));
                building.setAddress(obj.getString("address"));
                building.setDescription(obj.getString("description"));

                JSONArray open_hours = obj.getJSONArray("open_hours");
                for (int x = 0; x < open_hours.length(); x++) {
                    building.addDate(open_hours.getJSONObject(x).getString("date"));
                    //System.out.print(open_hours.getString(x));
                }
                buildingList.add(building);
            }

            return buildingList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
