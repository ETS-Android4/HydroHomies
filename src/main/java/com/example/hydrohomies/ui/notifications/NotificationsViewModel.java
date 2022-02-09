package com.example.hydrohomies.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotificationsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private String rem_int;
    private boolean notif_on;

    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Enable reminders for every: ");
    }

    public LiveData<String> getText() {
        return mText;
    }
}