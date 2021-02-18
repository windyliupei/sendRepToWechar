package com.windy.sendexceltoweichar;

import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.windy.sendexceltoweichar.io.GenerateReport;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;


import static com.windy.sendexceltoweichar.ConstantValues.SHANGBAN;

public class FirstFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Switch aSwitch = view.findViewById(R.id.switch1);

        //Sent Button
        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    GenerateReport.generateReport(getContext(),aSwitch.isChecked());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //处理 Switch
        Calendar today = Calendar.getInstance();
        today.setFirstDayOfWeek(Calendar.MONDAY);
        boolean isWeekEnd = today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        //周末休息,其余上班
        aSwitch.setChecked(!isWeekEnd);
        aSwitch.setText(SHANGBAN);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    aSwitch.setText(SHANGBAN);
                }else {
                    aSwitch.setText(ConstantValues.XIUXI);
                }
            }
        });
    }
}