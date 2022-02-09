package com.example.hydrohomies.ui.dashboard;

import static com.google.android.gms.tasks.Tasks.await;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.hydrohomies.R;
import com.example.hydrohomies.databinding.FragmentDashboardBinding;
import com.example.hydrohomies.ui.home.Drink;
import com.example.hydrohomies.ui.home.HomeViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
    private BarChart chart;
    private FirebaseFirestore db;
    private LocalDate curr_date;
    private LocalDate start_date;

    private ArrayList<LocalDate> date_range;
    private HomeViewModel viewModel;
    private List<Integer> sums;
    private int count = 7;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        chart = binding.chart;
        db = FirebaseFirestore.getInstance();
        curr_date = java.time.LocalDate.now();
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        setData();

        chart.invalidate();
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LocalDate getStartDateRange()
    {
        //get the current day value of current date
        int day = curr_date.getDayOfWeek().getValue();
        //get list of date ranging from mon-sun based on current date
        int days_from_mon = day - 1;
        LocalDate start_date = curr_date.minusDays(days_from_mon);
        return start_date;
    }


    public void setData()
    {
        start_date = getStartDateRange();
        Log.i("Start", viewModel.getPath());
        sums = new ArrayList<>();
        //Loop through the dates in the date range and get sums
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                // code goes here.
                for(int i=1; i<8; i++)
                {
                Log.i("Loop", String.valueOf(i));
                try {

                        Tasks.await(db.collection(viewModel.getPath() + start_date.toString()).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            int tot = 0;
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                int amt = ((Long) document.get("amount")).intValue();
                                                tot += amt;
                                            }
                                            Log.i("Totals", String.valueOf(tot));
                                            sums.add(tot);
                                            count--;

                                            if(count == 0)
                                            {
                                                Log.i("Sums", sums.toString());
                                                setChart();
                                            }

                                        } else {
                                            Log.i("No doc for date", "Sum is 0", task.getException());
                                            sums.add(0);
                                        }
                                    }
                                })

                        );
                    } catch (ExecutionException e) {
                        // The Task failed, this is the same exception you'd get in a non-blocking
                        // failure handler.
                        // ...
                    } catch (InterruptedException e) {
                        // An interrupt occurred while waiting for the task to complete.
                        // ...
                    }
                    start_date = start_date.plusDays(1);
                    Log.i("Date",start_date.toString());


                    // }
                }
            }
        });
        t1.start();

        try {
            t1.join();
            Log.i("Thread Finish", "Finished!");
        }
        catch (InterruptedException e)
        {
            //
        }

    }

    public void setChart()
    {
        int ind = 0;
        ArrayList<String> axiss = new ArrayList<>();
        axiss.add("Mon");
        axiss.add("Tues");
        axiss.add("Wed");
        axiss.add("Thurs");
        axiss.add("Fri");
        axiss.add("Sat");
        axiss.add("Sun");

        List<BarEntry> entries = new ArrayList<>();

        for(int s: sums) {
            entries.add(new BarEntry(ind,s));
            ind++;
        }
        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width
        chart.setData(data);
        chart.setFitBars(true); // make the x-axis fit exactly all bars
        YAxis y = chart.getAxisLeft();
        XAxis x = chart.getXAxis();

        x.setDrawGridLines(false);
        x.setDrawAxisLine(false);
        //x.setDrawLabels(true);
        x.setValueFormatter(new IndexAxisValueFormatter(axiss));
        x.setTextSize(15f);
        y.setTextSize(15f);
        chart.setExtraTopOffset(20);
        chart.setExtraBottomOffset(20);
        chart.invalidate(); // refresh
        Log.i("Chart", "Chart created!");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}