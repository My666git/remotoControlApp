package com.example.remotecontrolapp.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.remotecontrolapp.tcp.SocketViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardViewModel extends ViewModel {
    private final MutableLiveData<JoystickState> joystickPosition = new MutableLiveData<>();
    private final SocketViewModel socketViewModel;
    private int seekBar;
    private int head1, head2;
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    private boolean isRunning = true;

    // 构造函数，传入 SocketViewModel
    public DashboardViewModel(SocketViewModel socketViewModel) {
        this.socketViewModel = socketViewModel;
    }

    void init() {
        this.seekBar = 0;
        this.head1 = 0xaa;
        this.head2 = 0xbb;
    }

    public void sendMessage(String message) {
        if (socketViewModel != null) {
            socketViewModel.sendData(message);
        }
    }

    // 处理 joystickPosition 变化的业务逻辑
    public void setJoystickPosition(JoystickState position) {
        joystickPosition.setValue(position);
    }

    public int getSeekBar() {
        return seekBar;
    }

    public void setSeekBar(int seekBar) {
        this.seekBar = seekBar;
    }

    // 启动一个线程来周期性地接收数据
    public void startSocketRevThread() {
        executor.scheduleWithFixedDelay(() -> {
            if (isRunning) {
                readDragonflyHeartbeat();
            } else {
                stopSocketRevThread();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);  // 初始延迟0毫秒，周期100毫秒
    }

    // 启动一个线程来周期性地发送数据
    public void startSocketTransThread() {
        executor.scheduleWithFixedDelay(() -> {
            if (isRunning) {
            float ange = joystickPosition.getValue().getAngle();
            float distance = joystickPosition.getValue().getDistance();
            int accelerator = seekBar;




                sendData(new byte[30]);
            } else {
                stopSocketTransThread();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);  // 初始延迟0毫秒，周期100毫秒
    }

    public void readDragonflyHeartbeat() {
        int hFlag = 0;
        int len = 0;
        boolean revDone = false;
        byte[] revList = null;

        long startTime = System.currentTimeMillis();  // 用于超时检测

        while (!revDone) {
            byte[] receivedData = receiveData(1);  // 接收 1 字节数据

            if (receivedData != null) {
                int data = receivedData[0] & 0xff;

                // 根据 hFlag 状态处理数据
                switch (hFlag) {
                    case 0:
                        if (data == 0xaa) hFlag = 1;  // 等待头标志 0xaa
                        break;
                    case 1:
                        if (data == 0xbb) hFlag = 2;  // 等待第二个头标志 0xbb
                        break;
                    case 2:
                        len = data;  // 数据长度
                        revList = receiveData(len);  // 接收实际数据
                        if (revList != null) {
                            revDone = true;  // 数据接收完成
                        } else {
                            hFlag = 0;  // 如果接收失败，重新等待头标志
                        }
                        break;
                }
            }

            if (System.currentTimeMillis() - startTime > 5000) {  // 超时检测（5秒）
                System.out.println("Timeout waiting for data.");
                break;
            }
        }

        if (revDone) {
            // 处理数据
            processReceivedData(revList);
        } else {
            System.out.println("Failed to receive complete data.");
        }
    }

    // 停止接收数据线程
    public void stopSocketRevThread() {
        isRunning = false;
        if (!executor.isShutdown()) {
            executor.shutdown();  // 关闭executor
        }
    }

    // 停止发送数据线程
    public void stopSocketTransThread() {
        isRunning = false;
        if (!executor.isShutdown()) {
            executor.shutdown();  // 关闭executor
        }
    }

    // 接收数据
    private byte[] receiveData(int byteCount) {
        try {
            InputStream inputStream = socketViewModel.getSocketInputStream();
            byte[] buffer = new byte[byteCount];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead == byteCount) {
                return buffer;
            } else {
                // 数据不足
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error receiving data: " + e.getMessage());
            return null;
        }
    }

    // 发送数据
    private void sendData(byte[] data) {
        try {
            OutputStream outputStream = socketViewModel.getSocketOutputStream();
            outputStream.write(data);
        } catch (IOException e) {
            System.err.println("Error sending data: " + e.getMessage());
        }
    }

    // 处理接收到的数据
    private void processReceivedData(byte[] data) {
        // 处理接收到的数据
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        System.out.println("Received Data: " + sb.toString());
    }
}
