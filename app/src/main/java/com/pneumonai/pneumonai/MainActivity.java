package com.pneumonai.pneumonai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{

    private static int SPLASH_SCREEN = 3000;

    Animation top_anim, bottom_anim;
    ImageView logo;
    TextView pneumonai, slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //Animations
        top_anim = AnimationUtils.loadAnimation(this, R.anim.top_anim);
        bottom_anim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);

        pneumonai = findViewById(R.id.pneumonai);
        logo = findViewById(R.id.logo);
        slogan = findViewById(R.id.slogan);

        logo.setAnimation(top_anim);
        pneumonai.setAnimation(bottom_anim);
        slogan.setAnimation(bottom_anim);

        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(MainActivity.this, PredictActivity.class);
            startActivity(intent);
            finish();
        }
    }, SPLASH_SCREEN);
    }
}
