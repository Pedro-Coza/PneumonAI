package com.pneumonai.pneumonai;

import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;


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

    private String url = "https://www.google.com    ";

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_main);

        UploadImage = (Button)findViewById(R.id.UploadImage);
        ChooseImage = (Button)findViewById(R.id.ChooseImage);
        Name = (EditText) findViewById(R.id.Name);
        ImageView = (ImageView)findViewById(R.id.ImageView);
        ChooseImage.setOnClickListener(this);
        UploadImage.setOnClickListener(this);
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
        stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        responseText.setText("de lokos chavales");
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
        switch (view.getId()){
            case R.id.ChooseImage:
                selectImage();
                break;


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
            Uri path = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                ImageView.setImageBitmap(bitmap);
                ImageView.setVisibility(View.VISIBLE);
                Name.setVisibility(View.VISIBLE);
            }catch (IOException e){
                e.getMessage();
                e.printStackTrace();
            }
        }
    }
}