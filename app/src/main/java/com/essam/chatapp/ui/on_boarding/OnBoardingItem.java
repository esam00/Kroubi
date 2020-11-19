package com.essam.chatapp.ui.on_boarding;

public class OnBoardingItem {
    private int titleSrcId;
    private int descriptionSrcId;
    private int imageSrcId;

    public OnBoardingItem(int titleSrcId, int descriptionSrcId, int imageSrcId) {
        this.titleSrcId = titleSrcId;
        this.descriptionSrcId = descriptionSrcId;
        this.imageSrcId = imageSrcId;
    }

    public int getTitleSrcId() {
        return titleSrcId;
    }

    public int getDescriptionSrcId() {
        return descriptionSrcId;
    }

    public int getImageSrcId() {
        return imageSrcId;
    }
}
