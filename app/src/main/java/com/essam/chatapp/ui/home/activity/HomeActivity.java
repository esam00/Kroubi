package com.essam.chatapp.ui.home.activity;

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
import androidx.viewpager2.widget.ViewPager2;

import com.essam.chatapp.R;
import com.essam.chatapp.firebase.FirebaseManager;
import com.essam.chatapp.ui.contacts.activity.ContactsActivity;
import com.essam.chatapp.ui.home.adapter.ViewPagerAdapter;
import com.essam.chatapp.ui.login.LoginActivity;
import com.essam.chatapp.ui.settings.SettingsActivity;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.ProjectUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 mViewPager2;
    private FloatingActionButton fab;
    private ViewPagerAdapter viewPagerAdapter;

    private final static String TAG = HomeActivity.class.getSimpleName();
    private FirebaseManager mFirebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mFirebaseManager = FirebaseManager.getInstance();
        initViews();
    }

    private void toggleOnlineState(boolean isOnline) {
        mFirebaseManager.toggleOnlineState(isOnline);
    }

    private void initViews() {
        // toolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.home_menu);

        // viewPager and tabLayout
        mViewPager2 = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        ViewPager2.OnPageChangeCallback pageChangeListener = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                changeFabIcon(position);
            }
        };

        viewPagerAdapter = new ViewPagerAdapter(this);
        mViewPager2.setAdapter(viewPagerAdapter);
        mViewPager2.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);

        // set tabs text using TabLayoutMediator
        new TabLayoutMediator(tabLayout, mViewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText(getString(R.string.chats).toUpperCase());
                        break;
                    case 1:
                        tab.setText(getString(R.string.status).toUpperCase());
                        break;
                    case 2:
                        tab.setText(getString(R.string.calls).toUpperCase());
                        break;
                }
            }
        }).attach();

        mViewPager2.registerOnPageChangeCallback(pageChangeListener);

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
        switch (mViewPager2.getCurrentItem()) {
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
            viewPagerAdapter.updateFragments();
        }
    }

    private void signOut() {
        mFirebaseManager.signOutUser();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        // I couldn't figure a way to get notified when user kills the entire process
        // I've tried to use onStop() then check if isFinishing() to set online state false
        // But this only happens when the activity goes in back stack [when pressing back button]
        // SO >>>>>> Online state will be false when ever onPause() method get called and then back to
        // online when onResume() called >>
        // then in every other activity we choose to set it back to online or keep it offline
        // I think the most important activity is ChatActivity
         toggleOnlineState(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        toggleOnlineState(true);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                break;
            case R.id.settings:
                goToSettings();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public void onBackPressed() {
        if (mViewPager2.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mViewPager2.setCurrentItem(0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
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
