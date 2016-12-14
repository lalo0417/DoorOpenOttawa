package com.algonquincollege.lalo0417.dooropenottawa;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.algonquincollege.lalo0417.dooropenottawa.model.Building;
import com.algonquincollege.lalo0417.dooropenottawa.parsers.BuildingJSONParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.android.gms.internal.a.v;

/**
 * List app of buildings in Ottawa
 * Created by CalebLalonde on 2016-11-08.
 * v1.0
 */

public class MainActivity extends ListActivity {

    public static final String IMAGES_BASE_URL = "https://doors-open-ottawa-hurdleg.mybluemix.net/";
    public static final String REST_URI = "https://doors-open-ottawa-hurdleg.mybluemix.net/buildings";
    public static final String LOGOUT = IMAGES_BASE_URL + "users/logout";

    private ProgressBar pb;
    private List<MyTask> tasks;
    private SwipeRefreshLayout mySwipeRefreshLayout;

    protected boolean[] favourites;

    private List<Building> buildingList;

    private static final String ABOUT_DIALOG_TAG;

    static {
        ABOUT_DIALOG_TAG = "About Dialog";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        tasks = new ArrayList<>();
        //load in the data
        requestData(REST_URI);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BuildingAdapter adapter= (BuildingAdapter) parent.getAdapter();
                Building itemSelected = adapter.getItem(position);
                //when you click an item it passes an intent with all it's info.
                Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                intent.putExtra("bId", itemSelected.getBuildingID());
                intent.putExtra("bName", itemSelected.getName());
                intent.putExtra("bAddress", itemSelected.getAddress());

                String date = "";

                for (int x = 0; x < itemSelected.getOpenHours().size(); x++) {
                    date += (itemSelected.getOpenHours().get(x) + "\n");
                }

                intent.putExtra("bHours", date);
                intent.putExtra("bDescription", itemSelected.getDescription());
                startActivity(intent);
            }
        });

        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.

                        requestData(REST_URI);
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        //searchview, when you type it changes the list live and when you hit enter.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ((BuildingAdapter) getListAdapter()).getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ((BuildingAdapter) getListAdapter()).getFilter().filter(newText);
                return false;
            }
        });

        //this makes scrolling the list view hide the keyboard.
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //hide KB
                InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            DialogFragment newFragment = new AboutDialogFragment();
            newFragment.show(getFragmentManager(), ABOUT_DIALOG_TAG);
            return true;
        }

        if (id == R.id.action_add) {
            Intent intent = new Intent(getApplicationContext(), NewBuildingActivity.class);
            startActivity(intent);
            return true;
        }

        if (item.isCheckable()) {
            // leave if the list is null
            if (buildingList == null) {
                return true;
            }

            // which sort menu item did the user pick?
            switch (item.getItemId()) {
                case R.id.sort_az:
                    Collections.sort(buildingList, new Comparator<Building>() {
                        @Override
                        public int compare(Building lhs, Building rhs) {
                            Log.i("BUILDING", "Sorting planets by name (a-z)");
                            return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                        }
                    });
                    break;

                case R.id.sort_za:
                    Collections.sort(buildingList, Collections.reverseOrder(new Comparator<Building>() {
                        @Override
                        public int compare(Building lhs, Building rhs) {
                            Log.i("BUILDING", "Sorting planets by name (z-a)");
                            return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                        }
                    }));
                    break;
            }
            // remember which sort option the user picked
            item.setChecked(true);
            // re-fresh the list to show the sort order
            ((BuildingAdapter) getListAdapter()).notifyDataSetChanged();
        } // END if item.isChecked()

        return true;
        //return super.onOptionsItemSelected(item);
    }

    private void requestData(String uri) {
        MyTask task = new MyTask();
        task.execute(uri);
    }

    protected void updateDisplay() {
        BuildingAdapter adapter = new BuildingAdapter(this, R.layout.item_building, buildingList);
        setListAdapter(adapter);
        mySwipeRefreshLayout.setRefreshing(false);

    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private class MyTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            if (tasks.size() == 0) {
                pb.setVisibility(View.VISIBLE);
            }
            tasks.add(this);
        }

        @Override
        protected String doInBackground(String... params) {

            String content = HttpManager.getData(params[0], "lalo0417", "password");
            buildingList = BuildingJSONParser.parseFeed(content);
            return content;
        }

        @Override
        protected void onPostExecute(String result) {

            tasks.remove(this);
            if (tasks.size() == 0) {
                pb.setVisibility(View.INVISIBLE);
            }

            if (result == null) {
                Toast.makeText(MainActivity.this, "Web service not available", Toast.LENGTH_LONG).show();
                return;
            }

            buildingList = BuildingJSONParser.parseFeed(result);
            updateDisplay();
        }

    }

    private class MyTask_Logout extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            String content = HttpManager.getData(params[0], "lalo0417", "password");
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Result", result);
        }
    }

    public void onDestroy() {
        //ondestroy do the logout task
        new MyTask_Logout().execute(LOGOUT);
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

}
