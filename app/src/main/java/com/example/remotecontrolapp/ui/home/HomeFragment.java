package com.example.remotecontrolapp.ui.home;

import android.os.Bundle;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.remotecontrolapp.databinding.FragmentHomeBinding;
import com.example.remotecontrolapp.tcp.SocketViewModel;

import java.net.Socket;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private SocketViewModel socketViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // homeViewModel = new HomeViewModel(requireContext()); // 获取应用级别的 Context
        // 获取 ViewModel
        socketViewModel = new ViewModelProvider(requireActivity()).get(SocketViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 正确获取 EditText 控件
        final EditText serverIPEditText = binding.serverIP;
        final EditText serverPortText = binding.serverPort;
        final EditText localIp = binding.localIP;
        final EditText localPort = binding.localPort;

        // 设置点击按钮的事件监听器
        binding.connectServerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取输入的服务器地址和端口
                String serverIP = serverIPEditText.getText().toString();
                int serverPort = Integer.parseInt(serverPortText.getText().toString());
                // 连接服务器
                socketViewModel.connect(serverIP, serverPort);
            }
        });

        // 观察错误信息
        socketViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // 显示 AlertDialog
                new AlertDialog.Builder(getContext())
                        .setTitle("connect")
                        .setMessage(errorMessage)
                        .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
                        .show();
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
