package com.noureddine.benomari.readproject.Activity.Activity;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.noureddine.benomari.readproject.Activity.Fragment.MainFragment;
import com.noureddine.benomari.readproject.Activity.Fragment.ScannerFragment;
import com.noureddine.benomari.readproject.Activity.Utils.AboutActivity;
import com.noureddine.benomari.readproject.R;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private Fragment mainFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        configureToolbar();
        configureNavigationView();
        configureDrawerLayout();

        showFirstFragment();


    }

    //CONFIGURE TOOLBAR
    public void configureToolbar() {
        toolbar.setTitle("OCR Read");
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_36dp);
        setSupportActionBar(toolbar);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }


    //CONFIGURE DRAWER LAYOUT
    private void configureDrawerLayout() {
        this.drawerLayout = findViewById(R.id.activity_main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            showMainFragment();
        }
        if (item.getItemId() == R.id.action_dont) {
            showScannerFragment();
        }

        return true;
    }


    //CONFIGURE NAVIGATION VIEW
    private void configureNavigationView() {
        NavigationView navigationView = findViewById(R.id.activity_main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.getItemId();
        switch (item.getItemId()) {
            case R.id.activity_main_drawer_feedback:
                askFeedback();
                return true;
            case R.id.activity_main_drawer_contact:
                sendMail();
                return true;
            case R.id.activity_main_drawer_about:
                startAboutActivity();
                return true;

        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;


    }


    //CONFIGURE DRAWER LAYOUT MENU
    private void startAboutActivity() {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
    }
    //CONTACT BY MAIL THE DEVELOPER
    private void sendMail() {
        Intent mail = new Intent(Intent.ACTION_SEND);
        mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"sudout.h@gmail.com"});
        mail.setType("message/rfc822");
        startActivity(Intent.createChooser(mail, "Send email via:"));
    }
    //ASK USER FOR FEEDBACK AND REDIRECT HIM TO APP PAGE IN THE PLAY STORE
    private void askFeedback() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
        }
    }

    //CONFIGURE FRAGMENT
    private void showFirstFragment() {
        Fragment visibleFragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_frame_layout);
        if (visibleFragment == null) {
            this.showFragment();
        }
    }

    private void showFragment() {
        if (this.mainFragment == null) this.mainFragment = MainFragment.newInstance();
        this.startTransactionFragment(this.mainFragment);

    }

    private void startTransactionFragment(Fragment fragment) {
        if (!fragment.isVisible()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_frame_layout, fragment).commit();


        }

    }
    //SHOW SCANNER FRAGMENT
    public void showScannerFragment() {
        Fragment scannerFragment = new ScannerFragment();
        this.startTransactionFragment(scannerFragment);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorScanner));
        Window window = MainActivity.this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorScannerToolbar));
        toolbar.setTitle("Scanner");

    }
    //SHOW OCR FRAGMENT
    public void showMainFragment() {
        this.mainFragment = new MainFragment();
        this.startTransactionFragment(this.mainFragment);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorToolBar));
        Window window = MainActivity.this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        toolbar.setTitle("OCR Read");

    }


}

