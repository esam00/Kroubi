package com.essam.chatapp.home.activity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.essam.chatapp.R;
import com.essam.chatapp.contacts.activity.ContactsActivity;
import com.essam.chatapp.home.adapter.ViewPagerAdapter;
import com.essam.chatapp.login.LoginActivity;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.ProjectUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private ViewPager homeViewPager;
    private FloatingActionButton fab;
    private FragmentManager fragmentManager;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager.OnPageChangeListener mPageChangeListener;

    private final static String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                changeFabIcon(position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        initViews();
    }

    private void initViews() {
        // toolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu);
        fragmentManager = getSupportFragmentManager();

        // viewPager and tabLayout
        homeViewPager = findViewById(R.id.view_pager);
        TabLayout homeTabLayout = findViewById(R.id.tabLayout);
        viewPagerAdapter = new ViewPagerAdapter(fragmentManager, 1);
        homeViewPager.setAdapter(viewPagerAdapter);
        homeTabLayout.setupWithViewPager(homeViewPager);
        homeViewPager.addOnPageChangeListener(mPageChangeListener);

        // fab button
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClicked();
            }
        });
    }

    private void onFabClicked() {
        switch (homeViewPager.getCurrentItem()) {
            case 0:
                openContactsActivity();
                break;
            case 1:
                openCamera();
                break;
            case 2:
                makeNewVoiceCall();
        }
    }

    private void openContactsActivity() {
        if (ProjectUtils.hasPermissionInManifest(this, Consts.DISPLAY_CONTACTS_ACTIVITY, Manifest.permission.READ_CONTACTS))
            startActivity(new Intent(HomeActivity.this, ContactsActivity.class));
    }

    private void changeFabIcon(int position) {
        switch (position) {
            case 0:
                fab.setImageResource(R.drawable.ic_chat);
                break;
            case 1:
                fab.setImageResource(R.drawable.ic_camera_white);
                break;
            case 2:
                fab.setImageResource(R.drawable.ic_call_white);
        }
    }

    private void openCamera() {
        // TODO: 3/7/2020 implement status by opening camera
    }

    private void makeNewVoiceCall() {
        // TODO: 3/7/2020 this will be a whole new feature i hope i will be able to implement voice calls one day ^_^
    }

    private void refreshChatsFragment() {
        if (!(viewPagerAdapter == null)) {
            Log.i(TAG, "refreshChatsFragment..");
            viewPagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (homeViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            homeViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            // just want to refresh home chats and need permission to fetch contacts data {name} to be displayed
            case Consts.READ_CONTACTS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.i("TAG", "onRequestPermissionsResult: Granted");
                        // refresh chats fragment
                        refreshChatsFragment();
                } else {
                    Log.i("TAG", "onRequestPermissionsResult: denied");
                }

                break;
                // permission to access user's contacts data to be displayed in contacts activity
                case Consts.DISPLAY_CONTACTS_ACTIVITY:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            Log.i("TAG", "onRequestPermissionsResult: Granted");
                            // refresh chats fragment
                            startActivity(new Intent(HomeActivity.this, ContactsActivity.class));
                    } else {
                        Log.i("TAG", "onRequestPermissionsResult: denied");
                    }
        }

    }

}
