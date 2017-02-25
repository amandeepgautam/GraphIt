package edu.unm.twin_cities.graphit.activity;

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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Fields;
import edu.unm.twin_cities.graphit.processor.dao.DeviceSensorMapDao;
import edu.unm.twin_cities.graphit.processor.model.DeviceSensorMap;
import edu.unm.twin_cities.graphit.processor.model.PlotData;
import edu.unm.twin_cities.graphit.rest.DataProvider;
import edu.unm.twin_cities.graphit.rest.SensorDataProvider;
import edu.unm.twin_cities.graphit.service.DataService;
import edu.unm.twin_cities.graphit.util.ConnectionResourceBundle;
import edu.unm.twin_cities.graphit.util.Measurement;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResourceManager;
import edu.unm.twin_cities.graphit.util.ServerActionUtil;

public class PlotActivity extends DrawerActivity {

    private static String TAG = PlotActivity.class.getSimpleName();

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

    RemoteConnectionResourceManager connectionManager = null; //lazy initialization.

    private ProgressDialog progressDialog = null;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setLineChart();
    }

    private void setLineChart() {
        lineChart = (LineChart) findViewById(R.id.chart);

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

        //DataProvider dataProvider = new RandomDataProvider(this);
        DataProvider dataProvider = new SensorDataProvider(this);
        PlotData plotData = dataProvider.getData(null);

        ArrayList<LineDataSet> dataSet = Lists.newArrayList();
        /*for (Map.Entry<String, List<Entry>> elem : plotData.getData().entrySet()) {
            //generate a random color. Hope that it would not clash,if things are truely random.
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

            LineDataSet lineDataSet = new LineDataSet(elem.getValue(), elem.getKey());
            lineDataSet.setDrawValues(false);   //removes the values adjacent to point in graph.
            lineDataSet.setColor(color);
            lineDataSet.setCircleColor(color);
            lineDataSet.setCircleSize(1f);

            dataSet.add(lineDataSet);
        }*/

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item= menu.findItem(R.id.action_settings);
        item.setVisible(false);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    //TODO: unregister broadcast receiver.

    private ArrayList<LineDataSet> getDataSet() {

        ArrayList<LineDataSet> dataSets = Lists.newArrayList();

        List<Entry> entryOne = Lists.newArrayList();
        entryOne.add(new Entry(1, 0));
        entryOne.add(new Entry(2, 1));
        entryOne.add(new Entry(3, 2));
        entryOne.add(new Entry(1, 3));
        entryOne.add(new Entry(5, 4));
        entryOne.add(new Entry(0, 5));

        LineDataSet lineDataSetOne = new LineDataSet(entryOne, "first");
        dataSets.add(lineDataSetOne);
        List<Entry> entryTwo = Lists.newArrayList();
        entryTwo.add(new Entry(5, 0));
        entryTwo.add(new Entry(1, 1));
        entryTwo.add(new Entry(3, 2));
        entryTwo.add(new Entry(7, 3));
        entryTwo.add(new Entry(3, 4));
        entryTwo.add(new Entry(2, 5));

        LineDataSet lineDataSetTwo = new LineDataSet(entryTwo, "Second");
        dataSets.add(lineDataSetTwo);
        //return the result for default devices only.

        return dataSets;
    }

    private void setProgressDialog() {
        if(progressDialog == null)
            progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.please_wait_message);
        progressDialog.setMessage(getResources().getString(R.string.scanning_for_devices));
        progressDialog.setCancelable(false);
    }

    private void updateData(BluetoothDevice bluetoothDevice) {
        try {
            ConnectionResourceBundle connectionResourceBundle = connectionManager.getConnectionResource(bluetoothDevice, getApplicationContext());

            ServerActionUtil serverActionUtil = new ServerActionUtil(connectionResourceBundle);

            //aggregate file paths.
            DeviceSensorMapDao deviceSensorMapDao = new DeviceSensorMapDao(this);
            Map<String, String> constraint = Maps.newHashMap();
            constraint.put(Fields.DEVICE_ID.getFieldName(), bluetoothDevice.getAddress());
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
                    Intent intent = new Intent(this, DataService.class);
                    intent.setAction(FileBrowserActivity.ACTION_INSERT_READINGS_DATA);
                    intent.putExtra(FileBrowserActivity.PARAM_SENSOR_DATA, (Serializable) collection);
                    intent.putExtra(FileBrowserActivity.PARAM_DEVICE_ID, bluetoothDevice.getAddress());     //wierd that it can throw NPE here.

                    //register a local broadcast.
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(DataService.READINGS_DATA_INSERT_COMPLETE);
                    localBroadcastManager.registerReceiver(bReceiver, intentFilter);

                    startService(intent);
                }
            }
        } catch(Exception e) {
            Log.e(TAG, "Exception while transfer file component of updating graphs." ,e);
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
        float scaleX = lineChart.getXValCount() / 3f;
        //lineChart.setScaleX(scaleX);
        //lineChart.zoom(scaleX, 1, );
        lineChart.setScaleMinima(10f, 1f);
        //XAxis xAxis = lineChart.getXAxis();
        lineChart.invalidate();
    }

    public void setOneMonthAxisView() {

    }

    public void setFreeAxisView() {

    }
}