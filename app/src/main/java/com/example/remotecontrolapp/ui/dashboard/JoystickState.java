package com.example.remotecontrolapp.ui.dashboard;

public class JoystickState {
    private float angle;
    private float distance;

    public JoystickState(float angle, float distance) {
        this.angle = angle;
        this.distance = distance;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
