package com.algonquincollege.lalo0417.dooropenottawa;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.algonquincollege.lalo0417.dooropenottawa.model.Building;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by CalebLalonde on 2016-11-22.
 */

public class DetailsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String REST_URI = "https://doors-open-ottawa-hurdleg.mybluemix.net/buildings";
    private TextView bNameTv;
    private TextView bAddressTv;
    private TextView bDescriptionTv;
    private TextView bHoursTv;
    private GoogleMap mMap;
    private Geocoder mGeocoder;
    private ProgressBar pb;
    private int bIDFromMainActivity;
    private List<MyTask> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_page);

        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        mGeocoder = new Geocoder(this, Locale.getDefault());

        bNameTv = (TextView) findViewById(R.id.tvTitle);
        bAddressTv = (TextView) findViewById(R.id.tvAddress);
        bDescriptionTv = (TextView) findViewById(R.id.tvDescription);
        bHoursTv = (TextView) findViewById(R.id.tvHours);

        // Get the bundle of extras that was sent to this activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String bNameFromMainActivity = bundle.getString("bName");
            String bAddressFromMainActivity = bundle.getString("bAddress");
            String bDescriptionFromMainActivity = bundle.getString("bDescription");
            String bHoursFromMainActivity = bundle.getString("bHours");
            bIDFromMainActivity = bundle.getInt("bId");

            bNameTv.setText(bNameFromMainActivity);
            bAddressTv.setText(bAddressFromMainActivity);
            bDescriptionTv.setText(bDescriptionFromMainActivity);
            bHoursTv.setText(bHoursFromMainActivity);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_details, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_delete){
            //alert dialogue to make sure you want to delete the building.
            new AlertDialog.Builder(DetailsActivity.this)
                    .setTitle("Delete entry")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            RequestPackage pkg = new RequestPackage();
                            pkg.setMethod( HttpMethod.DELETE );
                            pkg.setUri( REST_URI + "/" + bIDFromMainActivity );
                            MyTask deleteTask = new MyTask();
                            deleteTask.execute( pkg );
                            finish();                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
        //if you select edit, it passes an intent with the info from the view.
        if(id == R.id.action_edit){
            Bundle bundle = getIntent().getExtras();
            Intent intent = new Intent(getApplicationContext(), UpdateBuildingActivity.class);
            intent.putExtra("bId", bundle.getInt("bId"));
            intent.putExtra("bName", bundle.getString("bName"));
            intent.putExtra("bAddress", bundle.getString("bAddress"));
            intent.putExtra("bDescription", bundle.getString("bDescription"));
            startActivityForResult(intent, 99);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 99){
            if(resultCode == DetailsActivity.RESULT_OK) {
                MyTaskRefresh refresh = new MyTaskRefresh();
                refresh.execute(REST_URI + "/" + bIDFromMainActivity);
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String pinAddress = (bundle.getString("bAddress") + " Ottawa");
            pin(pinAddress);
        }
    }

    private void pin(String locationName) {
        try {
            Address address = mGeocoder.getFromLocationName(locationName, 1).get(0);
            LatLng ll = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(ll).title(locationName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 15.0f));
            Toast.makeText(this, "Pinned: " + locationName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Not found: " + locationName, Toast.LENGTH_SHORT).show();
        }
    }

    //This task is to have the data refresh on edit.
    private class MyTaskRefresh extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {

            String content = HttpManager.getData(params[0], "lalo0417", "password");
            return content;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result == null) {
                Toast.makeText(DetailsActivity.this, "Web service not available", Toast.LENGTH_LONG).show();
                return;
            }

            //This parses the result string into a JSON and assigns the data when you refresh the page.
            //Like this the data is edited as soon as you as the edited DetailsActivity restarts.
            try {
                JSONObject obj = new JSONObject(result);
                bNameTv.setText(obj.getString("name"));
                bAddressTv.setText(obj.getString("address"));
                bDescriptionTv.setText(obj.getString("description"));
            } catch (Throwable t) {
                Log.e("My App", "Could not parse malformed JSON: \"" + result + "\"");
            }
        }

    }

    private class MyTask extends AsyncTask<RequestPackage, String, String> {

        @Override
        protected void onPreExecute() {
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(RequestPackage ... params) {

            String content = HttpManager.getDataForEverythingelse(params[0],"lalo0417","password");
            return content;
        }

        @Override
        protected void onPostExecute(String result) {

            pb.setVisibility(View.INVISIBLE);

            if (result == null) {
                Toast.makeText(DetailsActivity.this, "Web service not available", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }
}
