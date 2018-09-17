/*TODO: wifi functions
    scan
    (dis)connect
    show ap info
*/

package me.lnki96.op.wifidemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int WIFI_STATE_MSG = 0;
    public static final int WIFI_SCAN_MSG = 1;

    public static final String WIFI_ENABLED_BOOL_KEY = "wifi_enabled";

    private static class MainHandler extends Handler {
        private WeakReference<MainActivity> mActivity;

        MainHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case WIFI_STATE_MSG:
                        boolean enabled = msg.getData().getBoolean(WIFI_ENABLED_BOOL_KEY);
                        activity.mWifiToggle.setChecked(enabled);
                        if (enabled)
                            activity.mWifiCtrl.startScan(1000);
                        else
                            activity.mWifiCtrl.stopScan();
                    case WIFI_SCAN_MSG:
                        if (msg.obj instanceof WifiUtils.ScanResultList) {
                            activity.mApListAdapter.setData(((WifiUtils.ScanResultList) msg.obj).get());
                            activity.mApListAdapter.notifyDataSetChanged();
                        }
                    default:
                }
            }
        }
    }

    class ApListAdapter extends RecyclerView.Adapter {
        class ItemViewHolder extends RecyclerView.ViewHolder {
            View v;

            ItemViewHolder(@NonNull View v) {
                super(v);
                this.v = v;
            }
        }

        class EmptyViewHolder extends RecyclerView.ViewHolder {
            View v;

            EmptyViewHolder(@NonNull View v) {
                super(v);
                this.v = v;
            }
        }

        private LayoutInflater mLayoutInflater;

        private List<ScanResult> mScanResults;

        ApListAdapter(@Nullable List<ScanResult> scanResultList) {
            mLayoutInflater = LayoutInflater.from(mContext);
            mScanResults = scanResultList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            return viewType == R.id.ap_empty ?
                    new EmptyViewHolder(mLayoutInflater.inflate(R.layout.ap_empty, viewGroup, false)) :
                    new ItemViewHolder(mLayoutInflater.inflate(R.layout.ap_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof ItemViewHolder) {
                final ScanResult scanResult = mScanResults.get(position);
                final boolean open = !(scanResult.capabilities.contains("WPA") && scanResult.capabilities.contains("WEP"));
                TextView textView = (TextView) viewHolder.itemView;
                textView.setText(scanResult.SSID);
                if (!open)
                    textView.setCompoundDrawables(null, null, getDrawable(R.drawable.ic_signal_wifi_4_bar_lock_36dp), null);

                textView.setOnClickListener(new View.OnClickListener() {
                    String mPasswd = "";

                    DialogInterface.OnClickListener mWifiConnectBtnClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    mWifiCtrl.connect(scanResult.SSID, mPasswd);
                                case DialogInterface.BUTTON_NEGATIVE:
                                    dialog.dismiss();
                                default:
                            }
                        }
                    };

                    @SuppressLint("InflateParams")
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder wifiConnectDialogBuilder = new AlertDialog.Builder(mContext);
                        wifiConnectDialogBuilder.setTitle(scanResult.SSID);
                        wifiConnectDialogBuilder.setView(mLayoutInflater.inflate(R.layout.wifi_connect, null));

                        EditText passwdEditText = findViewById(R.id.wifi_password);
                        if (!open) {
                            passwdEditText.setVisibility(View.VISIBLE);
                            mPasswd = passwdEditText.getText().toString();
                        }
                        if (mWifiManager.getConnectionInfo().getSSID().equals(scanResult.SSID))
                            wifiConnectDialogBuilder.setPositiveButton(R.string.wifi_connect, mWifiConnectBtnClickListener);
                        wifiConnectDialogBuilder.setNegativeButton(R.string.wifi_connect_dismiss, mWifiConnectBtnClickListener);
                        wifiConnectDialogBuilder.create();
                    }
                });

                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    DialogInterface.OnClickListener mApInfoDialogBtnClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mWifiCtrl.forget();
                        }
                    };

                    @SuppressLint("InflateParams")
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder wifiInfoDialogBuilder = new AlertDialog.Builder(mContext);
                        wifiInfoDialogBuilder.setTitle(scanResult.SSID);
                        wifiInfoDialogBuilder.setView(mLayoutInflater.inflate(R.layout.ap_info, null));
                        ((TextView) findViewById(R.id.ap_info_bssid)).setText(scanResult.BSSID);
                        if (mWifiManager.getConnectionInfo().getSSID().equals(scanResult.SSID))
                            wifiInfoDialogBuilder.setNegativeButton(R.string.wifi_forget, mApInfoDialogBtnClickListener);
                        wifiInfoDialogBuilder.create();
                        return false;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return (mScanResults == null || mScanResults.size() == 0) ? 1 : mScanResults.size();
        }

        @Override
        public int getItemViewType(int position) {
            return (mScanResults == null || mScanResults.size() == 0) ? R.id.ap_empty : R.id.ap_item;
        }

        public void setData(List<ScanResult> scanResults) {
            mScanResults = scanResults;
        }
    }

    private Context mContext;

    WifiManager mWifiManager;
    private WifiController mWifiCtrl;

    private Switch mWifiToggle;

    private ApListAdapter mApListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        MainHandler mHandler = new MainHandler(this);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mWifiToggle = findViewById(R.id.wifi_toggle);
        mWifiToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mWifiCtrl.toggle(isChecked);
            }
        });

        ViewGroup viewGroup = findViewById(R.id.main);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mApListAdapter = new ApListAdapter(null);
        RecyclerView mApListView = new RecyclerView(mContext);
        mApListView.setAdapter(mApListAdapter);
        mApListView.setLayoutManager(mLayoutManager);
        mApListView.setBackgroundColor(Color.WHITE);
        viewGroup.addView(mApListView);

        mWifiCtrl = new WifiController(mHandler, mWifiManager);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWifiCtrl.state();
    }
}
