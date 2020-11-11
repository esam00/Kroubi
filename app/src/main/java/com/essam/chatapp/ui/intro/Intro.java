package com.essam.chatapp.ui.intro;

import com.essam.chatapp.R;

import java.util.ArrayList;
import java.util.List;

public class Intro {

    public static List<OnBoardingItem> getIntroItems() {
        List<OnBoardingItem> onBoardingItems = new ArrayList<>();
        onBoardingItems.add(firstIntro);
        onBoardingItems.add(secondIntro);
        onBoardingItems.add(thirdIntro);

        return onBoardingItems;
    }

    private static OnBoardingItem firstIntro = new OnBoardingItem(
            R.string.first_intro_title,
            R.string.first_intro_description,
            R.drawable.ic_intro_first
    );

    private static OnBoardingItem secondIntro = new OnBoardingItem(
            R.string.second_intro_title,
            R.string.second_intro_description,
            R.drawable.ic_intro_second
    );

    private static OnBoardingItem thirdIntro = new OnBoardingItem(
            R.string.third_intro_title,
            R.string.third_intro_description,
            R.drawable.ic_intro_third
    );
}
