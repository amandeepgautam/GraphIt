package edu.unm.twin_cities.graphit.fragments;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.activity.FileBrowserActivity;
import edu.unm.twin_cities.graphit.application.GraphItApplication;
import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import edu.unm.twin_cities.graphit.processor.dao.DeviceDao;
import edu.unm.twin_cities.graphit.processor.dao.DeviceSensorMapDao;
import edu.unm.twin_cities.graphit.processor.model.Device;
import edu.unm.twin_cities.graphit.processor.model.DeviceSensorMap;
import edu.unm.twin_cities.graphit.processor.model.PlotData;
import edu.unm.twin_cities.graphit.rest.DataProvider;
import edu.unm.twin_cities.graphit.rest.RandomDataProvider;
import edu.unm.twin_cities.graphit.rest.SensorDataProvider;
import edu.unm.twin_cities.graphit.service.DataService;
import edu.unm.twin_cities.graphit.util.CommonUtils;
import edu.unm.twin_cities.graphit.util.ConnectionResouceBundle;
import edu.unm.twin_cities.graphit.util.Measurement;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResouceManager;
import edu.unm.twin_cities.graphit.util.ServerActionUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlotFragment extends Fragment implements View.OnClickListener {

    private static String TAG = PlotFragment.class.getSimpleName();

    private final int REQUEST_ENABLE_BT = 3;

    Deque<BluetoothDevice> bluetoothDevices = new ArrayDeque<BluetoothDevice>();

    Set<String> registeredDevices = null;   //lazy initialization,

    List<BluetoothDevice> erroredOutDevices = Lists.newArrayList();

    private volatile boolean isDataTransfer = false;

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch(action) {
                case DataService.READINGS_DATA_INSERT_COMPLETE:
                    boolean insertSuccessful = (boolean) intent.getExtras().get(DataService.PARAM_INSERT_COMPLETE);
                    if (!insertSuccessful) {
                        Log.i(TAG, "Cannot Transfer successfully.");
                        //TODO: Handle error.
                        //(String) intent.getExtras().get(FileBrowserActivity.PARAM_DEVICE_ID);
                        //erroredOutDevices.add();
                    }
                    if (bluetoothDevices.isEmpty()) {
                        progressDialog.dismiss();
                        lineChart.notifyDataSetChanged();
                    } else {
                        BluetoothDevice bluetoothDevice = bluetoothDevices.pop();
                        startTransferForNewDevice(bluetoothDevice);
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    // If no more devices are in queue and discovery has finished, then dismiss the dialog
                    if (bluetoothDevices.isEmpty() && !isDataTransfer) {
                        progressDialog.dismiss();
                        lineChart.notifyDataSetChanged();
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    // If the device is found and there is no data transfer in progress, start the data
                    // transfer for this device. If not, just add it to the queue.
                    BluetoothDevice foundBtDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String address = foundBtDevice.getAddress();
                    if (registeredDevices.contains(address)) {
                        if (isDataTransfer) {
                            bluetoothDevices.add(foundBtDevice);
                        } else {
                            startTransferForNewDevice(foundBtDevice);
                        }
                    }
                    break;
            }
        }
    };

    RemoteConnectionResouceManager connectionManager = null; //lazy initialization.

    private ProgressDialog progressDialog = null;
    private LineChart lineChart;


    public PlotFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_plot, container, false);

        lineChart = (LineChart) fragmentView.findViewById(R.id.chart);

        //override listeners for button so that activity is not called onClick.
        Button oneWeekGraphViewButton = (Button) fragmentView.findViewById(R.id.one_week_preset_button);
        oneWeekGraphViewButton.setOnClickListener(this);

        Button oneMonthGraphViewButton = (Button) fragmentView.findViewById(R.id.one_month_preset_button);
        oneMonthGraphViewButton.setOnClickListener(this);

        Button defaultGraphViewButton = (Button) fragmentView.findViewById(R.id.default_view_preset_button);
        defaultGraphViewButton.setOnClickListener(this);

        setLineChart();

        return fragmentView;
    }

    private void setLineChart() {
        //enable zoom behaviour.
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        //Customizing the no text data.
        lineChart.setNoDataText(getResources().getString(R.string.no_data_message));
        lineChart.setNoDataTextDescription(getResources().getString(R.string.no_data_next_action));

        Paint p = lineChart.getPaint(Chart.PAINT_INFO);
        p.setColor(Color.WHITE);
        //p.setTypeface(...);
        //p.setTextSize(...);

        //Do not draw anything with the grid.
        lineChart.setDrawGridBackground(false);
        //lineChart.setBackgroundColor(Color.WHITE);

        //A way to paint the background of the grid (works when background color is not set).
        //Paint p1 = lineChart.getPaint(Chart.PAINT_GRID_BACKGROUND);
        //p1.setColor(Color.RED);

        lineChart.getLegend().setWordWrapEnabled(true);
        lineChart.getLegend().setTextColor(Color.WHITE);
        //lineChart.setVisibleXRange(1);
        //lineChart.moveViewToX();

        setAxisProperties(lineChart);

        DataProvider dataProvider = new RandomDataProvider(getActivity());
        //DataProvider dataProvider = new SensorDataProvider(getActivity());
        PlotData plotData = dataProvider.getData();

        ArrayList<LineDataSet> dataSet = Lists.newArrayList();
        for (Map.Entry<String, List<Entry>> elem : plotData.getData().entrySet()) {
            //generate a random color. Hope that it would not clash,if things are truely random.
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

            LineDataSet lineDataSet = new LineDataSet(elem.getValue(), elem.getKey());
            lineDataSet.setDrawValues(false);   //removes the values adjacent to point in graph.
            lineDataSet.setColor(color);
            lineDataSet.setCircleColor(color);
            lineDataSet.setCircleSize(1f);

            dataSet.add(lineDataSet);
        }

        LineData data = new LineData(plotData.getXValues(), dataSet);
        lineChart.setData(data);
        lineChart.setDescription(getResources().getString(R.string.graph_label));
        lineChart.setDescriptionColor(Color.WHITE);
        lineChart.animateXY(1000, 1000);
    }

    private void setAxisProperties(LineChart lineChart) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(Color.WHITE);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    public void updateGraph(View view) {
        CommonUtils.startBluetoothDiscovery(getActivity(), REQUEST_ENABLE_BT, bReceiver);
        // Note that the following logic might change depending on how registration
        // of device is done. The following piece is good until when this activity is
        // destroyed when a new device is registered.
        if (registeredDevices == null) {
            registeredDevices = Sets.newHashSet();
            DeviceDao deviceDao = new DeviceDao(getActivity());
            List<Device> devices = deviceDao.fetchAll();
            for (Device device : devices) {
                registeredDevices.add(device.getDeviceId());
            }
        }

        GraphItApplication application = ((GraphItApplication) getActivity().getApplicationContext());
        connectionManager = application.getConnectionManager();

        setProgressDialog();
        progressDialog.show();
    }

    private void setProgressDialog() {
        if(progressDialog == null)
            progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(R.string.please_wait_message);
        progressDialog.setMessage(getResources().getString(R.string.scanning_for_devices));
        progressDialog.setCancelable(false);
    }

    private void updateData(BluetoothDevice bluetoothDevice) {
        try {
            ConnectionResouceBundle connectionResouceBundle = connectionManager.getConnectionResource(bluetoothDevice, getActivity().getApplicationContext());

            ServerActionUtil serverActionUtil = new ServerActionUtil(connectionResouceBundle);

            //aggregate file paths.
            DeviceSensorMapDao deviceSensorMapDao = new DeviceSensorMapDao(getActivity());
            Map<String, String> constraint = Maps.newHashMap();
            constraint.put(DatabaseHelper.Fields.DEVICE_ID.getFieldName(), bluetoothDevice.getAddress());
            List<DeviceSensorMap> deviceSensorMaps = deviceSensorMapDao.fetchWithConstraints(constraint);
            Set<String> filePaths = Sets.newHashSet();
            for (DeviceSensorMap deviceSensorMap : deviceSensorMaps) {
                filePaths.add(deviceSensorMap.getSensorFileLocation());
            }

            //get each file.
            //TODO: Test this with multiple files.
            for (String filePath : filePaths) {
                List<Measurement<Long, Float>> collection = serverActionUtil.transferFile(filePath);
                if (collection != null) {
                    Intent intent = new Intent(getActivity(), DataService.class);
                    intent.setAction(FileBrowserActivity.ACTION_INSERT_READINGS_DATA);
                    intent.putExtra(FileBrowserActivity.PARAM_SENSOR_DATA, (Serializable) collection);
                    intent.putExtra(FileBrowserActivity.PARAM_DEVICE_ID, bluetoothDevice.getAddress());     //wierd that it can throw NPE here.

                    //register a local broadcast.
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(DataService.READINGS_DATA_INSERT_COMPLETE);
                    localBroadcastManager.registerReceiver(bReceiver, intentFilter);

                    getActivity().startService(intent);
                }
            }
        } catch(Exception e) {
            Log.e(TAG, "Exception while transsfer file component of updating graphs." ,e);
        }

    }

    /**
     * Start the transfer of a new device putting new message on ProcessDialog.
     * @param bluetoothDevice
     */
    private void startTransferForNewDevice(BluetoothDevice bluetoothDevice) {
        String identifier = bluetoothDevice.getName();
        if (identifier == null)
            identifier = bluetoothDevice.getAddress();
        String msg = String.format(getResources().getString(R.string.data_transfer_message), identifier);
        progressDialog.setMessage(msg);
        updateData(bluetoothDevice);
    }

    public void setOneWeekAxisView(View view) {
        //Assumption is 2 readins a day. and Hence the scale. Ideally one could have
        //parsed the dates to identify the reading frequency.
        float scaleX = lineChart.getXValCount() / 14f;
        lineChart.setScaleMinima(scaleX, 1f);
        lineChart.invalidate();
    }

    public void setOneMonthAxisView(View view) {
        //Assumption is 2 readins a day. and Hence the scale. Ideally one could have
        //parsed the dates to identify the reading frequency.
        float scaleX = lineChart.getXValCount() / 60f;
        lineChart.setScaleMinima(scaleX, 1f);
        lineChart.invalidate();
    }

    public void setFreeAxisView(View view) {
        lineChart.setScaleMinima(1f, 1f);
        lineChart.invalidate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.one_month_preset_button:
                setOneMonthAxisView(v);
                break;
            case R.id.one_week_preset_button:
                setOneWeekAxisView(v);
                break;
            case R.id.default_view_preset_button:
                setOneMonthAxisView(v);
                break;
            default:
                break;
        }
    }
}
