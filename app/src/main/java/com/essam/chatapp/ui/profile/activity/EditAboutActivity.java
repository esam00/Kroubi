package com.essam.chatapp.ui.profile.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.essam.chatapp.R;
import com.essam.chatapp.utils.Consts;

public class EditAboutActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_about);

        String currentAbout = getIntent().getStringExtra(Consts.STATUS);
    }

    private void returnResultAbout(String resultAbout){
        Intent data = new Intent();
        data.putExtra(Consts.STATUS, resultAbout);
        setResult(RESULT_OK,data);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}