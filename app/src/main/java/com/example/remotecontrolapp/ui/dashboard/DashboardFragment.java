package com.example.remotecontrolapp.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.remotecontrolapp.R;
import com.example.remotecontrolapp.databinding.FragmentDashboardBinding;
import com.example.remotecontrolapp.tcp.SocketViewModel;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private JoystickView joystickView;
    private ImageView joystickButton;
    SocketViewModel socketViewModel;
    private DashboardViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // 获取 ViewModel
        socketViewModel = new ViewModelProvider(requireActivity()).get(SocketViewModel.class);
        // 使用工厂模式创建 DashboardViewModel 实例
        dashboardViewModel = new ViewModelProvider(this, new DashboardViewModelFactory(socketViewModel))
                .get(DashboardViewModel.class);
        // 初始化角度和距离显示
        TextView angleTextView = binding.angleTextView;
        angleTextView.setText(String.format("angle:%s", String.valueOf(0)));
        TextView distextView = binding.distanceTextView;
        distextView.setText(String.format("distance:%s", String.valueOf(0)));
        joystickView = root.findViewById(R.id.joystickView);
        joystickView.getJoystickPosition().observe(getViewLifecycleOwner(), new Observer<JoystickState>() {
            @Override
            public void onChanged(JoystickState state) {
                // 更新 ViewModel 中的值
                dashboardViewModel.setJoystickPosition(state);
                int value = (int)(state.getAngle());
                angleTextView.setText(String.format("angle:%s", String.valueOf(value)));
                int distance = (int)(state.getDistance());
                distextView.setText(String.format("distance:%s", String.valueOf(distance)));
            }
        });

        SeekBar seekBar = binding.seekbar1;
        TextView seekBarValue = binding.seekBarValue;
        seekBarValue.setText(String.format("油门:%s", String.valueOf(0)));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 每当进度发生变化时，获取当前的进度
                seekBarValue.setText(String.format("油门:%s", String.valueOf(progress)));
                dashboardViewModel.setSeekBar(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 用户开始滑动时的操作
                Log.d("SeekBar", "Started Tracking");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 用户停止滑动时的操作
                Log.d("SeekBar", "Stopped Tracking");
            }
        });

        binding.connectFly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dashboardViewModel.startSocketRevThread();
                dashboardViewModel.startSocketTransThread();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}