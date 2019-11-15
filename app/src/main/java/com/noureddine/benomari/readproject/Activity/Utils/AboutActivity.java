package com.noureddine.benomari.readproject.Activity.Utils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.noureddine.benomari.readproject.R;

public class AboutActivity extends AppCompatActivity {
    final static String webURL = "https://github.com/jhansireddy/AndroidScannerDemo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

    }
    //OPEN THE GITHUB OF THE LIBRARY USED IN THE APP
    public void openWebPage(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(webURL));
        startActivity(i);
    }
}
