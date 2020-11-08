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
            "Title1",
            "العنوان الأول",
            "Description1",
            "الوصف الأول",
            R.drawable.ic_status
    );

    private static OnBoardingItem secondIntro = new OnBoardingItem(
            "Title2",
            "العنوان الثاني",
            "Description2",
            "الوصف الثاني",
            R.drawable.ic_status
    );

    private static OnBoardingItem thirdIntro = new OnBoardingItem(
            "Title3",
            "العنوان الثالث",
            "Description3",
            "الوصف الثالث",
            R.drawable.ic_status
    );
}
