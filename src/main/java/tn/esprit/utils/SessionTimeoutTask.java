package tn.esprit.utils;

import java.util.Timer;
import java.util.TimerTask;

public class SessionTimeoutTask {
    private Timer timer;
    private final long timeoutPeriod; // en millisecondes

    public SessionTimeoutTask(long timeoutPeriod) {
        this.timeoutPeriod = timeoutPeriod;
    }

    public void start() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SessionManager.getInstance().logout();
                System.out.println("Session expirée par inactivité");
            }
        }, timeoutPeriod);
    }

    public void reset() {
        if (timer != null) {
            timer.cancel();
        }
        start();
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }
}