package com.weiqilab.hackathon.nextdoorhelp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.weiqilab.hackathon.nextdoorhelp.R;

public class NeedHelperActivity extends AppCompatActivity {

    TextView tvYourchoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_need_helper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvYourchoice = (TextView)findViewById(R.id.yourchoice);


        Button btnHelpMe = (Button)findViewById(R.id.btn_helpme);
        btnHelpMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                intent = new Intent(NeedHelperActivity.this, NearbyActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);
            }
        });

        final ImageButton btnDress = (ImageButton)findViewById(R.id.dressbtn);
        btnDress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btnDress.setBackgroundColor(Color.GREEN);
                tvYourchoice.setText("I need dress!");
            }
        });

        final ImageButton btnFurniture = (ImageButton)findViewById(R.id.furniturebtn);
        btnDress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btnFurniture.setBackgroundColor(Color.GREEN);
                tvYourchoice.setText("I need furniture!");
            }
        });

        final ImageButton btnCatering = (ImageButton)findViewById(R.id.cateringbtn);
        btnDress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btnCatering.setBackgroundColor(Color.GREEN);
                tvYourchoice.setText("I need catering!");
            }
        });

        final ImageButton btnBathroom = (ImageButton)findViewById(R.id.bathroombtn);
        btnDress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btnBathroom.setBackgroundColor(Color.GREEN);
                tvYourchoice.setText("I need bathroom!");
            }
        });

        final ImageButton btnToilet = (ImageButton)findViewById(R.id.toiletbtn);
        btnDress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btnToilet.setBackgroundColor(Color.GREEN);
                tvYourchoice.setText("I need a restroom!");
            }
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //
//            }
//        });
    }

}
