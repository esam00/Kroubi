package com.essam.chatapp.ui.intro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.essam.chatapp.R;
import com.essam.chatapp.ui.LoginPhoneNumberActivity;
import com.rd.PageIndicatorView;

public class OnBoardingActivity extends AppCompatActivity {
    private PageIndicatorView mPageIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        setUpViewPager();

        Button loginButton = findViewById(R.id.login_btn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OnBoardingActivity.this, LoginPhoneNumberActivity.class));
                finish();
            }
        });
    }

    private void setUpViewPager() {
        mPageIndicatorView = findViewById(R.id.pageIndicatorView);
        ViewPager2 onBoardingViewPager = findViewById(R.id.introViewPager);

        onBoardingViewPager.setAdapter(new OnBoardingAdapter(Intro.getIntroItems()));

        onBoardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                mPageIndicatorView.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        });
    }
}