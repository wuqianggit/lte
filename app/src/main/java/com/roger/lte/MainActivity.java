package com.roger.lte;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.roger.lte.utils.FileWriteUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    public static final int NP_CELL_INFO_UPDATE = 1001;
    private PhoneInfoThread phoneInfoThread;
    public Handler mMainHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == NP_CELL_INFO_UPDATE) {
                Bundle bundle = msg.getData();
                TextView tvTime = (TextView) findViewById(R.id.tvTimeleaps);
                Date now = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                tvTime.setText(formatter.format(now));
                historyRecycleViewAdapter.notifyDataSetChanged();

                TextView tvDeviceId = (TextView) findViewById(R.id.tvDeviceId);
                tvDeviceId.setText("DeviceId:" + phoneGeneralInfo.deviceId);

                TextView tvRatType = (TextView) findViewById(R.id.tvRatType);
                tvRatType.setText("RatType:" + phoneGeneralInfo.ratType);

                TextView tvMnc = (TextView) findViewById(R.id.tMnc);
                tvMnc.setText("Mnc:" + phoneGeneralInfo.mnc);

                TextView tvMcc = (TextView) findViewById(R.id.tvMcc);
                tvMcc.setText("Mcc:" + phoneGeneralInfo.mcc);

                TextView tvOperatorName = (TextView) findViewById(R.id.tvOperaterName);
                tvOperatorName.setText("Operator:" + phoneGeneralInfo.operaterName);

                TextView tvSdk = (TextView) findViewById(R.id.tvSdk);
                tvSdk.setText("Sdk:" + phoneGeneralInfo.sdk);

                TextView tvImsi = (TextView) findViewById(R.id.tvImsi);
                tvImsi.setText("Imsi:" + phoneGeneralInfo.Imsi);

                TextView tvSerialNum = (TextView) findViewById(R.id.tvSerialNum);
                tvSerialNum.setText("SN:" + phoneGeneralInfo.serialNumber);

                TextView tvModel = (TextView) findViewById(R.id.tvModel);
                tvModel.setText("Model:" + phoneGeneralInfo.phoneModel);

                TextView tvSoftwareVersion = (TextView) findViewById(R.id.tvSoftware);
                tvSoftwareVersion.setText("Version:" + phoneGeneralInfo.deviceSoftwareVersion);

                TextView tvAllCellInfo = (TextView) findViewById(R.id.tvStaticInfoLableHistory);
                tvAllCellInfo.setText("History cells list(" + HistoryServerCellList.size() + ")");
            }
            super.handleMessage(msg);
        }
    };

    // for current
    public PhoneGeneralInfo phoneGeneralInfo;
    public CellGeneralInfo serverCellInfo;

    //for history
    private List<CellGeneralInfo> HistoryServerCellList;
    private CellnfoRecycleViewAdapter historyRecycleViewAdapter;
    private RecyclerView historyrecyclerView;
    TelephonyManager phoneManager;
    private MyPhoneStateListener myPhoneStateListener;
    /*需要写入内存卡的权限*/
    public String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
    List mPermissionList = new ArrayList();
    boolean mShowRequestPermission = true;//用户是否禁止权限
    public SignalStrength mySignal;

    private EditText etFileName; //输入的文件名称 by wuqiang
    private Button btnOn; //开始或结束按钮 by wuqiang
    private SendTask task=new SendTask(); // 向服务器发送的任务 by wuqiang
    private Timer timer=new Timer(); //定时任务
    private SendMessageThread smt=new SendMessageThread(null,10086);
    private Boolean isFirstStart=true; //是否是第一次该应用
    private TextView tvCellInfo;
    private TextView tvFilePath; /*系统存入的文件路径*/
    void InitProcessThread() {
        phoneInfoThread = new PhoneInfoThread(MainActivity.this);
        phoneInfoThread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*检查是否有权限*/
        checkPermission();
        /*首先判断手机是否插入了内存卡*/
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "手机没有插入内存卡，系统不能使用", Toast.LENGTH_SHORT).show();
            return;
        }
        etFileName = (EditText) this.findViewById(R.id.et_file_name); //文件名称
        btnOn = (Button) this.findViewById(R.id.btn_on);
        tvCellInfo= (TextView) this.findViewById(R.id.tv_cellInfo);
        tvFilePath= (TextView) findViewById(R.id.tv_file_path);
        //反写按钮信息
        if(smt!=null&&Boolean.TRUE.equals(smt.getOn())){
            btnOn.setText("结束！");
        }else{
            btnOn.setText("开始！");
        }
    }

    /**
     * 开始/结束 发送数据 按钮点击事件
     * @param view
     */
    public void click(View view){
        String fileName= etFileName.getText().toString();
        if(btnOn.getText().toString().startsWith("开始")){
            if(TextUtils.isEmpty(fileName)){
                Toast.makeText(getApplicationContext(),"请输入文件名称！",Toast.LENGTH_SHORT).show();
                return;
            }
            //开始写入数据
            smt.setOn(true);
            Toast.makeText(getApplicationContext(),"系统正在记录数据，按“结束”按钮，保存数据！",Toast.LENGTH_SHORT).show();
            //如果是第一次启动该应用，就start
            if(Boolean.TRUE.equals(isFirstStart)){
                smt.start();
                isFirstStart=false;
            }
            btnOn.setText("结束！");
            /*文件名不可修改*/
            etFileName.setEnabled(false);
        }else{//停止写入数据
            smt.setOn(false);
            /*写入文件的时候，设置按钮不可以用*/
            btnOn.setEnabled(false);
            Toast.makeText(getApplicationContext(),"系统正在写入信息，请稍等...",Toast.LENGTH_SHORT).show();
            /*输出文件内容，并清空*/
            System.out.println(smt.CELL_INFO_DATA.toString());/*将文件写入内存卡*/
            String filePath=FileWriteUtil.writeAppend(fileName,smt.CELL_INFO_DATA.toString());
            if(TextUtils.isEmpty(filePath)){
                Toast.makeText(getApplicationContext(),"系统写入失败！",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(),"系统写入成功，文件路径为:"+filePath,Toast.LENGTH_SHORT).show();
                tvFilePath.setText("文件保存路径："+filePath);
            }
            smt.CELL_INFO_DATA.delete(0,smt.CELL_INFO_DATA.length());/*清空文件内容*/
            btnOn.setText("开始！");
            btnOn.setEnabled(true);/*设置按钮可用*/
            etFileName.setEnabled(true);/*设置文件名可修改*/
        }
    }

    public void checkPermission() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            String _permission = permissions[i];
            if ((ContextCompat.checkSelfPermission(MainActivity.this, _permission)) !=
                    PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(_permission);
            }
        }
        if (mPermissionList.isEmpty()) {
            doProcess();
        } else {
            String[] permissions = (String[]) mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i]);
                        if (showRequestPermission) {
                            checkPermission();
                            return;
                        } else {
                            mShowRequestPermission = false;
                        }
                    }
                }
                doProcess();
                break;
            default:
                break;
        }
    }

    public void doProcess() {
        serverCellInfo = new CellGeneralInfo();
        phoneGeneralInfo = new PhoneGeneralInfo();

        myPhoneStateListener = new MyPhoneStateListener();
        phoneManager = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
        phoneManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        HistoryServerCellList = new ArrayList<CellGeneralInfo>();
        historyrecyclerView = (RecyclerView) findViewById(R.id.historyrcv);
        LinearLayoutManager historylayoutManager = new LinearLayoutManager(this);
        historylayoutManager.setOrientation(OrientationHelper.VERTICAL);
        historyrecyclerView.setLayoutManager(historylayoutManager);
        historyRecycleViewAdapter = new CellnfoRecycleViewAdapter(MainActivity.this, HistoryServerCellList);
        historyrecyclerView.setAdapter(historyRecycleViewAdapter);
        historyrecyclerView.setItemAnimator(new DefaultItemAnimator());
        //更新页面上的信息。 by wuqiang
        InitProcessThread();
    }

    public void updateServerCellView() {
        TextView tvCellType = (TextView) findViewById(R.id.tvCellType);
        tvCellType.setText("Rat:" + serverCellInfo.type);

        TextView tvTac = (TextView) findViewById(R.id.tvTac);
        tvTac.setText("Tac:" + serverCellInfo.tac);

        TextView tvCellId = (TextView) findViewById(R.id.tvCellId);
        tvCellId.setText("Ci:" + serverCellInfo.CId);

        TextView tvPCI = (TextView) findViewById(R.id.tvPCI);
        tvPCI.setText("Pci:" + serverCellInfo.pci);

        TextView tvRsrp = (TextView) findViewById(R.id.tvRsrp);
        tvRsrp.setText("Rsrp:" + serverCellInfo.rsrp);

        TextView tvRsrq = (TextView) findViewById(R.id.tvRsrq);
        tvRsrq.setText("Rsrq:" + serverCellInfo.rsrq);

        TextView tvSINR = (TextView) findViewById(R.id.tvSINR);
        tvSINR.setText("Sinr:" + serverCellInfo.sinr);

        TextView tvCqi = (TextView) findViewById(R.id.tvCqi);
        tvCqi.setText("cqi:" + serverCellInfo.cqi);

        TextView tvGetCellType = (TextView) findViewById(R.id.tvGetCellType);
        tvGetCellType.setText("type:" + serverCellInfo.getInfoType);
    }

    //监听电话状态改变
    class MyPhoneStateListener extends PhoneStateListener {
        //Callback invoked when network signal strengths changes.当信号改变的时候，会调用这个方法
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mySignal = signalStrength;
            getPhoneGeneralInfo();
            getServerCellInfo();
            if (phoneGeneralInfo.ratType == TelephonyManager.NETWORK_TYPE_LTE) {
                try {
                    serverCellInfo.rssi = (Integer) signalStrength.getClass().getMethod("getLteSignalStrength").invoke(signalStrength);
                    serverCellInfo.rsrp = (Integer) signalStrength.getClass().getMethod("getLteRsrp").invoke(signalStrength);
                    serverCellInfo.rsrq = (Integer) signalStrength.getClass().getMethod("getLteRsrq").invoke(signalStrength);
                    serverCellInfo.sinr = (Integer) signalStrength.getClass().getMethod("getLteRssnr").invoke(signalStrength);
                    serverCellInfo.cqi = (Integer) signalStrength.getClass().getMethod("getLteCqi").invoke(signalStrength);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else if (phoneGeneralInfo.ratType == TelephonyManager.NETWORK_TYPE_GSM) {
                try {
                    serverCellInfo.rssi = signalStrength.getGsmSignalStrength();
                    serverCellInfo.rsrp = (Integer) signalStrength.getClass().getMethod("getGsmDbm").invoke(signalStrength);
                    serverCellInfo.asulevel = (Integer) signalStrength.getClass().getMethod("getAsuLevel").invoke(signalStrength);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else if (phoneGeneralInfo.ratType == TelephonyManager.NETWORK_TYPE_TD_SCDMA) {
                try {
                    serverCellInfo.rssi = (Integer) signalStrength.getClass().getMethod("getTdScdmaLevel").invoke(signalStrength);
                    serverCellInfo.rsrp = (Integer) signalStrength.getClass().getMethod("getTdScdmaDbm").invoke(signalStrength);
                    serverCellInfo.asulevel = (Integer) signalStrength.getClass().getMethod("getAsuLevel").invoke(signalStrength);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
            serverCellInfo.time = formatter.format(now);
            updateHistoryCellList(serverCellInfo);
            updateServerCellView();
            getPhoneGeneralInfo();
            //信号消息 by wuqiang 2017-11-11
            smt.setMsg(serverCellInfo.getCellInfoData());
        }

        /**
         * 获取手机信息
         */
        public void getPhoneGeneralInfo() {
            phoneGeneralInfo.operaterName = phoneManager.getNetworkOperatorName();
            phoneGeneralInfo.operaterId = phoneManager.getNetworkOperator();
            phoneGeneralInfo.mnc = Integer.parseInt(phoneGeneralInfo.operaterId.substring(0, 3));
            phoneGeneralInfo.mcc = Integer.parseInt(phoneGeneralInfo.operaterId.substring(3));
            phoneGeneralInfo.phoneDatastate = phoneManager.getDataState();
            phoneGeneralInfo.deviceId = phoneManager.getDeviceId();
            phoneGeneralInfo.Imei = phoneManager.getSimSerialNumber();
            phoneGeneralInfo.Imsi = phoneManager.getSubscriberId();
            phoneGeneralInfo.serialNumber = phoneManager.getSimSerialNumber();
            phoneGeneralInfo.deviceSoftwareVersion = android.os.Build.VERSION.RELEASE;
            phoneGeneralInfo.phoneModel = android.os.Build.MODEL;
            phoneGeneralInfo.ratType = phoneManager.getNetworkType();
            phoneGeneralInfo.sdk = android.os.Build.VERSION.SDK_INT;
        }

        /**
         * 获取信号类型
         */
        public void getServerCellInfo() {
            try {
                List<CellInfo> allCellinfo;
                allCellinfo = phoneManager.getAllCellInfo();
                if (allCellinfo != null) {
                    CellInfo cellInfo = allCellinfo.get(0);
                    serverCellInfo.getInfoType = 1;
                    if (cellInfo instanceof CellInfoGsm) {
                        System.out.println("是 CellInfoGsm");
                        tvCellInfo.setText("CellInfoGsm");
                        CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                        serverCellInfo.CId = cellInfoGsm.getCellIdentity().getCid();
                        serverCellInfo.rsrp = cellInfoGsm.getCellSignalStrength().getDbm();
                        serverCellInfo.asulevel = cellInfoGsm.getCellSignalStrength().getAsuLevel();
                        serverCellInfo.lac = cellInfoGsm.getCellIdentity().getLac();
                        serverCellInfo.RatType = TelephonyManager.NETWORK_TYPE_GSM;
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        System.out.println("是 CellInfoWcdma");
                        tvCellInfo.setText("CellInfoWcdma");
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                        serverCellInfo.CId = cellInfoWcdma.getCellIdentity().getCid();
                        serverCellInfo.psc = cellInfoWcdma.getCellIdentity().getPsc();
                        serverCellInfo.lac = cellInfoWcdma.getCellIdentity().getLac();
                        serverCellInfo.rsrp = cellInfoWcdma.getCellSignalStrength().getDbm();
                        serverCellInfo.asulevel = cellInfoWcdma.getCellSignalStrength().getAsuLevel();
                        serverCellInfo.RatType = TelephonyManager.NETWORK_TYPE_UMTS;
                    } else if (cellInfo instanceof CellInfoLte) {
                        tvCellInfo.setText("CellInfoLte");
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                        serverCellInfo.CId = cellInfoLte.getCellIdentity().getCi();
                        serverCellInfo.pci = cellInfoLte.getCellIdentity().getPci();
                        serverCellInfo.tac = cellInfoLte.getCellIdentity().getTac();
                        serverCellInfo.rsrp = cellInfoLte.getCellSignalStrength().getDbm();
                        serverCellInfo.asulevel = cellInfoLte.getCellSignalStrength().getAsuLevel();
                        serverCellInfo.RatType = TelephonyManager.NETWORK_TYPE_LTE;
                    }
                } else{
                    getServerCellInfoOnOlderDevices();
                }
            } catch (Exception e) {
                getServerCellInfoOnOlderDevices();
            }

        }

        void getServerCellInfoOnOlderDevices() {
            Log.d("oldCell", "inside getServerCellInfoOnOlderDevices");
            List<CellInfo> allCellinfo;
            allCellinfo = phoneManager.getAllCellInfo();
            Log.d("oldCell", allCellinfo.size() + "");
            if (!allCellinfo.isEmpty()) {
                GsmCellLocation location = (GsmCellLocation) phoneManager.getAllCellInfo();
                serverCellInfo.getInfoType = 0;
                serverCellInfo.CId = location.getCid();
                serverCellInfo.tac = location.getLac();
                serverCellInfo.psc = location.getPsc();
                serverCellInfo.type = phoneGeneralInfo.ratType;
            }
        }
        void updateHistoryCellList(CellGeneralInfo serverinfo) {
            CellGeneralInfo newcellInfo = (CellGeneralInfo) serverinfo.clone();
            HistoryServerCellList.add(serverinfo);
        }
    }

    class PhoneInfoThread extends Thread {
        private Context context;
        public int timecount;

        public PhoneInfoThread(Context context) {
            this.context = context;
            timecount = 0;
        }
        public void run() {
            while (true) {
                try {
                    timecount++;
                    Message message = new Message();
                    message.what = NP_CELL_INFO_UPDATE;
                    Bundle bundle = new Bundle();
                    bundle.putString("UPDATE", "UPDATE_TIME");
                    message.setData(bundle);
                    mMainHandler.sendMessage(message);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


;