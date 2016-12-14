package com.algonquincollege.lalo0417.dooropenottawa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.algonquincollege.lalo0417.dooropenottawa.model.Building;
import com.algonquincollege.lalo0417.dooropenottawa.parsers.BuildingJSONParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CalebLalonde on 2016-12-06.
 */

public class NewBuildingActivity extends Activity {
    public static final String REST_URI = "https://doors-open-ottawa-hurdleg.mybluemix.net/buildings";
    public static final String IMG_URI = "http://doors-open-ottawa-hurdleg.mybluemix.net/buildings/";
    private final int CAMERA_REQUEST_CODE = 100;
    private Button cancelBtn;
    private Button saveBtn;
    private EditText editName;
    private EditText editAddress;
    private EditText editDesc;
    private ImageView img;

    private List<MyTask> tasks;
    private ProgressBar pb;
    private Bitmap imageBitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.building_add);

        cancelBtn = (Button) findViewById(R.id.buttonCancel);
        saveBtn = (Button) findViewById(R.id.buttonSave);
        img = (ImageView) findViewById(R.id.imageView);

        editDesc = (EditText) findViewById(R.id.editText_Description);
        editAddress = (EditText) findViewById(R.id.editText_Address);
        editName = (EditText) findViewById(R.id.editText_Name);

        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        tasks = new ArrayList<>();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //create new building and add name, desc, img and address from text fields.
                Building building = new Building();
                building.setName( editName.getText().toString() );

                //test to see if I could send the image as a bitmap = inconclusive.
//                building.setImage( IMG_URI + "278/" + bitmapToBase64(imageBitmap) );
                building.setImage(IMG_URI);

                building.setDescription( editDesc.getText().toString() );
                building.setAddress( editAddress.getText().toString() );

                //set pkg params to the values in the building
                RequestPackage pkg = new RequestPackage();
                pkg.setMethod( HttpMethod.POST );
                pkg.setUri( REST_URI );
                pkg.setParam("name", building.getName() );
                pkg.setParam("address", building.getAddress() );

                pkg.setParam("image", building.getImage() );

                pkg.setParam("description", building.getDescription() );

                MyTask postTask = new MyTask();
                postTask.execute( pkg );

                finish();
            }
        });

        //on click listener for the camera to open
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
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

            Log.d("PARAMS: " , ""+params);
            String content = HttpManager.getDataForEverythingelse(params[0],"lalo0417","password");
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            pb.setVisibility(View.INVISIBLE);

            if (result == null) {
                Toast.makeText(NewBuildingActivity.this, "Web service not available", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent){
        Bundle extras;

        if (resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        switch(requestCode){
            case CAMERA_REQUEST_CODE:
                extras = resultIntent.getExtras();
                //saving the bitmap to the global variable imageBitmap to convert it later int the bitmap to base 64 converter
                imageBitmap = (Bitmap) extras.get("data");
                if(imageBitmap != null){
                    img.setImageBitmap(imageBitmap);
                }
                break;
        }
    }

    //This is a bitmap to Base64 converter to be used for the picture upload.
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}




