package com.essam.chatapp.ui.photoEditor;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.ProjectUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.essam.chatapp.R;

public class PhotoEditorActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mImage;
    private EditText captionEt;
    private ImageButton cropIb, emojyIb, textIb, typeIb;
    private int requestCode;

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

        requestCode = getIntent().getIntExtra("requestCode",-1);
    }

    private void initViews(){
        mImage = findViewById(R.id.image_present);
        captionEt = findViewById(R.id.caption_et);
        cropIb = findViewById(R.id.action_crop);
        emojyIb = findViewById(R.id.action_emojy);
        textIb = findViewById(R.id.action_text);
        typeIb = findViewById(R.id.action_type);
        FloatingActionButton fab = findViewById(R.id.fab);

        // click listeners
        captionEt.setOnClickListener(this);
        cropIb.setOnClickListener(this);
        emojyIb.setOnClickListener(this);
        textIb.setOnClickListener(this);
        typeIb.setOnClickListener(this);
        fab.setOnClickListener(this);
    }

    void setResultData(){
        Intent intent = new Intent();
        intent.putExtra("message",captionEt.getText().toString());
        intent.putExtra("requestCode",requestCode);
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
            case R.id.action_crop:
            case R.id.action_emojy:
            case R.id.action_type:
            case R.id.action_text:
                ProjectUtils.showToast(this,"Coming Soon ..");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("requestCode",requestCode);
        setResult(RESULT_OK,intent);
        finish();
    }
}
