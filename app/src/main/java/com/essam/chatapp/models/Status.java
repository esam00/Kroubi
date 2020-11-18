package com.essam.chatapp.models;

import java.util.ArrayList;
import java.util.List;

public class Status {
    private String status;
    private boolean isCurrent;

    public Status(String status, boolean isCurrent) {
        this.status = status;
        this.isCurrent = isCurrent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public static List<Status> getStatusList(CharSequence [] array, String currentStatus){
        List<Status> statusList = new ArrayList<>();
        for (CharSequence status : array){
            statusList.add(new Status(status.toString(), status.equals(currentStatus)));
        }

        return statusList;
    }
}
