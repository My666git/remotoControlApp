package com.example.remotecontrolapp.ui.dashboard;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.remotecontrolapp.tcp.SocketViewModel;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {
    private final SocketViewModel socketViewModel;

    public DashboardViewModelFactory(SocketViewModel socketViewModel) {
        this.socketViewModel = socketViewModel;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(socketViewModel);  // 将 SocketViewModel 传递到构造函数中
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
