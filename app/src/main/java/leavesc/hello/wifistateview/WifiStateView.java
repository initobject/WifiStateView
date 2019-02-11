package leavesc.hello.wifistateview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * 作者：leavesC
 * 时间：2019/2/11 20:21
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class WifiStateView extends AppCompatImageView {

    private WifiManager wifiManager;

    private WifiHandler wifiHandler;

    //没有开启Wifi或开启了Wifi但没有连接
    private static final int LEVEL_NONE = 0;

    //Wifi信号等级（最弱）
    private static final int LEVEL_1 = 1;

    //Wifi信号等级
    private static final int LEVEL_2 = 2;

    //Wifi信号等级
    private static final int LEVEL_3 = 3;

    //Wifi信号等级（最强）
    private static final int LEVEL_4 = 4;

    private static final String TAG = "WifiStateView";

    private static class WifiHandler extends Handler {

        //虚引用
        private WeakReference<WifiStateView> stateViewWeakReference;

        WifiHandler(WifiStateView wifiStateView) {
            stateViewWeakReference = new WeakReference<>(wifiStateView);
        }

        @Override
        public void handleMessage(Message msg) {
            WifiStateView wifiStateView = stateViewWeakReference.get();
            if (wifiStateView == null) {
                return;
            }
            switch (msg.what) {
                case LEVEL_1:
                    wifiStateView.setImageResource(R.drawable.wifi_1);
                    break;
                case LEVEL_2:
                    wifiStateView.setImageResource(R.drawable.wifi_2);
                    break;
                case LEVEL_3:
                    wifiStateView.setImageResource(R.drawable.wifi_3);
                    break;
                case LEVEL_4:
                    wifiStateView.setImageResource(R.drawable.wifi_4);
                    break;
                case LEVEL_NONE:
                default:
                    wifiStateView.setImageResource(R.drawable.wifi_none);
                    break;
            }
        }
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                Log.e(TAG, "action " + intent.getAction());
                switch (intent.getAction()) {
                    case WifiManager.WIFI_STATE_CHANGED_ACTION:
                        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
                            wifiHandler.sendEmptyMessage(LEVEL_NONE);
                        }
                        break;
                    case WifiManager.RSSI_CHANGED_ACTION:
                        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
                            wifiHandler.sendEmptyMessage(LEVEL_NONE);
                            return;
                        }
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
                        Log.e(TAG, "level:" + level);
                        wifiHandler.sendEmptyMessage(level);
                        break;
                }
            }
        }
    };


    public WifiStateView(Context context) {
        this(context, null);
    }

    public WifiStateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WifiStateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiHandler = new WifiHandler(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter();
        //Wifi连接状态变化
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        //Wifi信号强度变化
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        getContext().registerReceiver(wifiStateReceiver, intentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        wifiHandler.removeCallbacksAndMessages(null);
        getContext().unregisterReceiver(wifiStateReceiver);
    }

}
