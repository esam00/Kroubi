package com.essam.chatapp.photoEditor;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.essam.chatapp.utils.Consts;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.essam.chatapp.R;

public class PhotoEditorActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mImage;
    private EditText captionEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();

        String imageUri = getIntent().getStringExtra(Consts.EDIT_PHOTO);
        if(imageUri!=null){
            Glide.with(this).load(imageUri).into(mImage);
        }
    }

    private void initViews(){
        mImage = findViewById(R.id.image_present);
        captionEt = findViewById(R.id.caption_et);
        FloatingActionButton fab = findViewById(R.id.fab);

        // click listeners
        captionEt.setOnClickListener(this);
        fab.setOnClickListener(this);
    }

    void setResultData(){
        Intent intent = new Intent();
        intent.putExtra("message",captionEt.getText().toString());
        setResult(RESULT_OK,intent);
        finish();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab:
                setResultData();
                break;
            case R.id.caption_et:
//                centerCaptionLayout();
                break;
        }
    }
}
