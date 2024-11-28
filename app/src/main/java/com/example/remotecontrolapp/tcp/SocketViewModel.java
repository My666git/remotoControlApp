package com.example.remotecontrolapp.tcp;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketViewModel extends AndroidViewModel {

    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;  // 新增 InputStream
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2); // 使用固定大小的线程池

    public SocketViewModel(Application application) {
        super(application);
    }

    // 获取 Socket 连接
    public Socket getSocket() {
        return socket;
    }

    // 获取 OutputStream
    public OutputStream getSocketOutputStream() {
        return outputStream;
    }

    // 获取 InputStream
    public InputStream getSocketInputStream() {
        return inputStream;
    }

    // 连接到服务器
    public void connect(final String serverIP, final int serverPort) {
        if (socket != null && socket.isConnected()) {
            Log.d("Socket", "Already connected.");
            return;  // 如果已连接，则不再执行连接操作
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(serverIP, serverPort), 5000); // 设置连接超时
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream(); // 获取 InputStream
                    errorMessage.postValue("连接成功");
                    Log.d("Socket", "Connected to server");
                } catch (UnknownHostException e) {
                    errorMessage.postValue("无法解析服务器地址，请检查 IP 地址。");
                    Log.e("Socket", "Unknown host", e);
                } catch (IOException e) {
                    errorMessage.postValue("无法连接到服务器，请检查网络设置或服务器状态。");
                    Log.e("Socket", "Error connecting to server", e);
                }
            }
        });
    }

    // 发送数据
    public void sendData(String data) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (socket != null && outputStream != null && socket.isConnected()) {
                    try {
                        outputStream.write(data.getBytes());
                        outputStream.flush();
                        Log.d("Socket", "Data sent: " + data);
                    } catch (IOException e) {
                        Log.e("Socket", "Error sending data", e);
                    }
                } else {
                    Log.e("Socket", "Socket or OutputStream is null, or not connected");
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理资源
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("Socket", "Error closing socket", e);
            }
        }
    }
}
