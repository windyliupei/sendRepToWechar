package com.windy.sendexceltoweichar;

import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.windy.sendexceltoweichar.io.GenerateReport;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;


import static com.windy.sendexceltoweichar.ConstantValues.SHANGBAN;

public class FirstFragment extends Fragment {

    private LoadingDialog ld;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }


    /**
     * 用Handler来更新UI
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.arg1)
            {
                case 0:
                {
                    ld.show();
                    break;
                }
                case 1:
                {
                    ld.loadSuccess();
                    break;
                }
                case 2:
                {
                    ld.loadFailed();
                    break;
                }
            }

        }};

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Switch aSwitch = view.findViewById(R.id.switch1);


        ld = new LoadingDialog(getContext());
        ld.setLoadingText("加载中")
                .setSuccessText("加载成功")//显示加载成功时的文字
                .setFailedText("加载失败");



        //Sent Button
        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //handler.sendEmptyMessage(0);
                //GenerateReport.generateReport(getContext(),aSwitch.isChecked());
                //新建线程
                new Thread(){
                    @Override
                    public void run() {

                        try {
                            //向handler发消息
                            handler.sendEmptyMessage(0);
                            GenerateReport.generateReport(getContext(),aSwitch.isChecked());
                            handler.sendEmptyMessage(1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(2 );
                        }

                    }}.start();
                //handler.sendEmptyMessage(1);

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