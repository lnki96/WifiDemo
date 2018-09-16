package me.lnki96.op.wifidemo;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class WifiController {
    private Handler mHandler;
    private WifiManager mWifiMgr;

    private Thread scanThread;

    public WifiController(Handler handler, WifiManager wifiMgr) {
        mHandler = handler;
        mWifiMgr = wifiMgr;
    }

    public void state() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                boolean enabled = mWifiMgr.isWifiEnabled();
                Bundle bundle = new Bundle();
                bundle.putBoolean(MainActivity.WIFI_ENABLED_BOOL_KEY, enabled);
                Message msg = new Message();
                msg.what = MainActivity.WIFI_STATE_MSG;
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    public void toggle(final boolean enabling) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                boolean enabled = mWifiMgr.isWifiEnabled();
                if (mWifiMgr.isWifiEnabled() != enabling)
                    mWifiMgr.setWifiEnabled(enabling);
                state();
            }
        }.start();
    }

    public void startScan(final int interval) {
        if (scanThread == null || scanThread.isInterrupted()) {
            scanThread = new Thread() {
                @Override
                public void run() {
                    super.run();

                    WifiUtils.ScanResultList scanResultList = new WifiUtils.ScanResultList();
                    while (true) {
                        scanResultList.set(mWifiMgr.getScanResults());
                        Message msg = new Message();
                        msg.what = MainActivity.WIFI_SCAN_MSG;
                        msg.obj = scanResultList;
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
        if (!scanThread.isInterrupted())
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
