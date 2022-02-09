package com.example.hydrohomies.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.Timestamp;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    int curr_total = 0;
    int daily_intake = 64;
    int selected_amt = 4;
    String path;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
    }


    public LiveData<String> getText() {
        return mText;
    }

    public void setSelected(int a)
    {
        this.selected_amt = a;
    }

    public int getSelected()
    {
        return selected_amt;
    }
    public void setCurr(int curr)
    {
        this.curr_total = curr;
    }

    public void addTotal(int i)
    {
        this.curr_total += i;
    }

    public float getProg()
    {
        return ((float)curr_total/daily_intake)*100;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public void setProgress(){
        mText.setValue(curr_total + "/" + daily_intake + " oz");

    }

}