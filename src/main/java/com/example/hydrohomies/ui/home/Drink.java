package com.example.hydrohomies.ui.home;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

//Class to create drink with a amt/timestamp

public class Drink {

    private int amt;
    private Date date;
    private String id;

    public Drink(int amt, Date date, String id)
    {
        this.amt = amt;
        this.date = date;
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public int getAmt(){
        return amt;
    }

    @Override
    public String toString()
    {
        SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        return "Amount: " + amt + " oz\n"
                + "Time: " + sdf.format(date.getTime());
    }

}
