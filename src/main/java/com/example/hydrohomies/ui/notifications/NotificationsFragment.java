package com.example.hydrohomies.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.hydrohomies.R;
import com.example.hydrohomies.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    private Spinner spin;
    private Switch notifs;
    private String str_int;
    int interval = 10;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        spin = binding.spinner;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.reminder_intervals, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spin.setAdapter(adapter);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                           @Override
                                           public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                                               Log.i("Reminder int", (String) adapterView.getItemAtPosition(pos));
                                               str_int = (String) adapterView.getItemAtPosition(pos);
                                               setInterval(str_int);
                                           }

                                           @Override
                                           public void onNothingSelected(AdapterView<?> adapterView) {

                                           }
                                       }

        );

        notifs = binding.switch2;
        notifs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isOn) {
                if(isOn)
                {
                    Log.i("ENABLED" , "ON");
                    setReminder();
                }
                else {
                    Log.i("DISABLED" , "OFF");
                }
            }
        });

        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }


    public void setInterval(String val)
    {
        switch(val) {
            case "30 s":
                interval = 30;
                break;
            case "1 min":
                interval = 60;
                break;
            case "5 min":
                interval = 300;
                break;
            case "15 min":
                interval = 1500;
                break;
            case "30 min":
                interval = 3000;
                break;
            case "1 hr":
                interval = 6000;
                break;
            case "2 hr":
                interval = 12000;
                break;
        }
    }

    public void setReminder()
    {
        for(int i = 1; i < 11; i++) {
            Log.i("Interval", String.valueOf(i));
            NotificationUtils _notificationUtils = new NotificationUtils(getContext());
            long _currentTime = System.currentTimeMillis();
            long seconds =  1000 * (interval*i);
            long _triggerReminder = _currentTime + seconds; //triggers 10 reminders spaced out in the specified interval
            _notificationUtils.setReminder(_triggerReminder, i);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}