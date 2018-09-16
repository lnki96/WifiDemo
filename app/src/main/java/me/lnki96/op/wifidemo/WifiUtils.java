package me.lnki96.op.wifidemo;

import android.net.wifi.ScanResult;

import java.util.List;

class WifiUtils {
    static class ScanResultList {
        private List<ScanResult> list;

        ScanResultList() {
            list = null;
        }

        public ScanResultList(List<ScanResult> list) {
            set(list);
        }

        public void set(List<ScanResult> list) {
            this.list = list;
        }

        public List<ScanResult> get() {
            return list;
        }
    }
}
