package com.essam.chatapp.ui.intro;

public class OnBoardingItem {
    private String enTitle;
    private String arTitle;
    private String enDescription;
    private String arDescription;
    private int image;

    public OnBoardingItem(String enTitle, String arTitle, String enDescription, String arDescription, int image) {
        this.enTitle = enTitle;
        this.arTitle = arTitle;
        this.enDescription = enDescription;
        this.arDescription = arDescription;
        this.image = image;
    }

    public int getImage() {
        return image;
    }

    public String getTitle(String locale){
        if (locale.equals("en")){
            return enTitle;
        }else {
            return arTitle;
        }
    }

    public String getDescription(String locale){
        if (locale.equals("en")){
            return enDescription;
        }else {
            return arDescription;
        }
    }
}
