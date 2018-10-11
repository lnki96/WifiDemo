package me.lnki96.op.wifidemo;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Controller {
    private Handler mHandler;
    private WifiManager mWifiMgr;

    private Thread scanThread;

    private boolean mEnabled;

    Controller(Handler handler, WifiManager wifiMgr) {
        mHandler = handler;
        mWifiMgr = wifiMgr;
    }

    public void state(final int interval) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                while (!isInterrupted()) {
                    mEnabled = mWifiMgr.isWifiEnabled();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(MainActivity.WIFI_ENABLED_BOOL_KEY, mEnabled);
                    Message msg = mHandler.obtainMessage(MainActivity.WIFI_STATE_MSG);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    try {
                        sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void toggle(final boolean enabling) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                if (mEnabled != enabling)
                    mWifiMgr.setWifiEnabled(enabling);
            }
        }.start();
    }

    public void startScan(final int interval) {
        if (scanThread == null || !scanThread.isAlive()) {
            scanThread = new Thread() {
                @Override
                public void run() {
                    super.run();

                    Utils.ScanResultList scanResultList = new Utils.ScanResultList();
                    while (!isInterrupted()) {
                        scanResultList.set(mWifiMgr.getScanResults());
                        Message msg = mHandler.obtainMessage(MainActivity.WIFI_SCAN_MSG, scanResultList);
                        mHandler.sendMessage(msg);
                        try {
                            sleep(interval);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            };
            scanThread.start();
        }
    }

    public void stopScan() {
        if (scanThread != null && scanThread.isAlive())
            scanThread.interrupt();
    }

    public void connect(final String SSID, final String passwd) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = SSID;
                wifiConfig.preSharedKey = passwd;
            }
        }.start();
    }

    public void forget() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                mWifiMgr.removeNetwork(mWifiMgr.getConnectionInfo().getNetworkId());

            }
        }.start();
    }
}
