package com.algonquincollege.lalo0417.dooropenottawa;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.algonquincollege.lalo0417.dooropenottawa.model.Building;

/**
 * Created by CalebLalonde on 2016-12-12.
 */

public class UpdateBuildingActivity extends FragmentActivity {

    public static final String REST_URI = "https://doors-open-ottawa-hurdleg.mybluemix.net/buildings";

    private Button cancelBtn;
    private Button saveBtn;
    private EditText editName;
    private EditText editAddress;
    private EditText editDesc;
    private ProgressBar pb;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.building_edit);

        cancelBtn = (Button) findViewById(R.id.buttonCancel);
        saveBtn = (Button) findViewById(R.id.buttonSave);

        editDesc = (EditText) findViewById(R.id.editExistingBuildingDescription);
        editAddress = (EditText) findViewById(R.id.editExistingBuildingAddress);
        editName = (EditText) findViewById(R.id.editExistingBuildingName);

        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        //set the edit text's text to the values so they can be edited
        final Bundle bundle = getIntent().getExtras();
        id = bundle.getInt("bId");
        Log.d("ID: " , ""+id);
        editName.setText(bundle.getString("bName"));
        editAddress.setText(bundle.getString("bAddress"));
        editDesc.setText(bundle.getString("bDescription"));

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //save the new values to the server
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = REST_URI + "/" + id;
                Building building = new Building();
                building.setName( editName.getText().toString() );
                building.setDescription( editDesc.getText().toString() );
                building.setAddress( editAddress.getText().toString() );

                RequestPackage pkg = new RequestPackage();
                pkg.setMethod( HttpMethod.PUT );
                pkg.setUri( uri );
                pkg.setParam("name", building.getName() );
                pkg.setParam("address", building.getAddress() );
                pkg.setParam("description", building.getDescription() );

                UpdateBuildingActivity.MyTask putTask = new UpdateBuildingActivity.MyTask();
                putTask.execute( pkg );

                //reload details page pass that the result is OK to update info on activity switch
                Intent i = getIntent();
                setResult(DetailsActivity.RESULT_OK,i);
                finish();

            }
        });
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
                Toast.makeText(UpdateBuildingActivity.this, "Web service not available", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }
}
