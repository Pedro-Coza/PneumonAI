package com.pneumonai.pneumonai;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private static final String TAG = MainActivity.class.toString();

    private Button btnSendRequest;

    private RequestQueue mRequestQueue;

    private StringRequest stringRequest;

    //private TextView text = findViewById(R.id.responseText);

    private Button ChooseImage, UploadImage;
    private EditText Name;
    private ImageView ImageView;
    private final int IMG_REQUEST = 1;
    private String filePath;
    private Bitmap bitmap;
    private static final int REQUEST_PERMISSIONS = 100;

    private static final String URL = "https://servidor-definitivo.ew.r.appspot.com";

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_main);

        UploadImage = (Button)findViewById(R.id.UploadImage);
        ChooseImage = (Button)findViewById(R.id.ChooseImage);
        Name = (EditText) findViewById(R.id.Name);
        ImageView = (ImageView)findViewById(R.id.ImageView);
        ChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        UploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(bitmap);
            }
        });
        btnSendRequest = (Button) findViewById(R.id.btnSendRequest);
        btnSendRequest.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //send request and print response using volley
                sendRequestAndPrintResponse();
            }
        });
    }

    private void sendRequestAndPrintResponse() {

        mRequestQueue = Volley.newRequestQueue(this);
        stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        responseText.setText(response.toString());
                    }
                });

                Log.i(TAG,"Response: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,"Error: " + error.toString());
            }
        });

        mRequestQueue.add(stringRequest);
    }

    @Override
    public void onClick(View view) {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE))) {

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        } else {
            Log.e("Else", "Else");
            selectImage();
        }
        switch (view.getId()){
            case R.id.UploadImage:
                uploadImage(bitmap);
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMG_REQUEST && resultCode== RESULT_OK && data!=null){
            Uri uri = data.getData();
            filePath = getPath(uri);
            if(filePath != null) {
                try {
                    ImageView.setVisibility(View.VISIBLE);
                    Name.setVisibility(View.VISIBLE);
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 150,150, false);
                    ImageView.setImageBitmap(bitmap);
                    Log.d("filePath", String.valueOf(filePath));
                } catch (IOException e) {
                    e.getMessage();
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(MainActivity.this,"no image selected", Toast.LENGTH_LONG).show(); }
        }
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        String path = "";
        if(cursor !=null && cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }

        return path;
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadImage(final Bitmap bitmap) {

        //Para mostrar el loading
        //ProgressDialog.show(this, "Loading", "Wait while loading...");

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(final NetworkResponse response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                                    TextView responseText = findViewById(R.id.responseText);
                                    responseText.setText(json);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                    e.getMessage();
                                }
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

}