package com.essam.chatapp.ui.profile.activity.edit_status;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.essam.chatapp.R;
import com.essam.chatapp.firebase.FirebaseManager;
import com.essam.chatapp.models.Status;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.ProjectUtils;

import java.util.ArrayList;
import java.util.List;

public class EditStatusActivity extends AppCompatActivity implements EditStatusContract.View,
        StatusAdapter.OnClickListener {
    private TextView currentStatusTv;
    private EditText editStatusEt;
    private ConstraintLayout editStatusContainer;
    private ImageView editStatusIcon, confirmEditStatusIcon;

    private String mCurrentStatus;
    private int lastSelectedIndex;
    private List<Status> mStatusList = new ArrayList<>();
    private EditStatusContract.Presenter mPresenter;
    private StatusAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);

        initViews();

        mCurrentStatus = getIntent().getStringExtra(Consts.STATUS);
        currentStatusTv.setText(mCurrentStatus);
        mPresenter = new EditStatusPresenter(FirebaseManager.getInstance(), this);
    }

    private void initViews(){
        currentStatusTv = findViewById(R.id.tv_status);
        editStatusEt = findViewById(R.id.edit_status_et);
        editStatusIcon = findViewById(R.id.editStatusIv);
        confirmEditStatusIcon = findViewById(R.id.confirmStatusIv);
        editStatusContainer = findViewById(R.id.edit_status_cl);

        RecyclerView statusRv = findViewById(R.id.statusRv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mStatusList = Status.getStatusList(getResources().getTextArray(R.array.status_list), mCurrentStatus);
        mAdapter = new StatusAdapter(mStatusList,this);
        statusRv.setAdapter(mAdapter);
        statusRv.setLayoutManager(layoutManager);

        editStatusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEditNameLayout(true);
            }
        });

        confirmEditStatusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleConfirmEditName();
            }
        });

    }

    private void handleEditNameLayout(boolean editing) {
        editStatusIcon.setEnabled(!editing);
        if (editing){
            editStatusContainer.setVisibility(View.VISIBLE);
            editStatusEt.requestFocus();
            editStatusEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0){
                        confirmEditStatusIcon.setImageResource(R.drawable.ic_check);
                    }else {
                        confirmEditStatusIcon.setImageResource(R.drawable.ic_close);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }else {
            editStatusEt.setText("");
            editStatusContainer.setVisibility(View.GONE);
        }
    }

    private void handleConfirmEditName(){
        if (ProjectUtils.isEditTextFilled(editStatusEt)){
            clearSelected();
            updateCurrentStatus(editStatusEt.getText().toString());
        }

        handleEditNameLayout(false);
    }

    private void updateCurrentStatus(String status){
        mCurrentStatus = status;
        currentStatusTv.setText(mCurrentStatus);
        mPresenter.updateCurrentStatus(mCurrentStatus);
    }

    private void swapSelected(int index){
        mStatusList.get(lastSelectedIndex).setCurrent(false);
        mStatusList.get(index).setCurrent(true);
        lastSelectedIndex = index;
        mAdapter.notifyDataSetChanged();
    }

    private void clearSelected(){
        mStatusList.get(lastSelectedIndex).setCurrent(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int selectedIndex) {
        swapSelected(selectedIndex);
        updateCurrentStatus(mStatusList.get(selectedIndex).getStatus());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}