package edu.unm.twin_cities.graphit.fragments;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dd.morphingbutton.MorphingButton;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.common.collect.Lists;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.activity.DrawerActivity.Fragments;
import edu.unm.twin_cities.graphit.activity.DrawerActivity;
import edu.unm.twin_cities.graphit.processor.dao.SensorTypeDao;
import edu.unm.twin_cities.graphit.processor.model.PlotData;
import edu.unm.twin_cities.graphit.processor.model.SensorType;
import edu.unm.twin_cities.graphit.processor.model.UserPref;
import edu.unm.twin_cities.graphit.rest.DataProvider;
import edu.unm.twin_cities.graphit.rest.SensorDataProvider;
import edu.unm.twin_cities.graphit.util.LineColor;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResourceManager;
import edu.unm.twin_cities.graphit.util.UserPreferencesUtil;
import fr.ganfra.materialspinner.MaterialSpinner;
import lombok.Data;

import static edu.unm.twin_cities.graphit.R.color.defaultTextColor;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlotFragment extends Fragment implements View.OnClickListener {

    private static String TAG = PlotFragment.class.getSimpleName();

    private final int REQUEST_ENABLE_BT = 3;

    private Deque<BluetoothDevice> bluetoothDevices = new ArrayDeque<BluetoothDevice>();

    private Set<String> registeredDevices = null;   //lazy initialization,

    private List<BluetoothDevice> erroredOutDevices = Lists.newArrayList();
    private DrawerActivity parentActivity;

    RemoteConnectionResourceManager connectionManager = null; //lazy initialization.

    private ProgressDialog progressDialog = null;
    private LineChart lineChart;
    private View fragmentView;


    public PlotFragment() {
        setArguments(new Bundle());
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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (fragmentView == null) {
            // Inflate the layout for this fragment
            fragmentView = inflater.inflate(R.layout.fragment_plot, container, false);

            parentActivity = (DrawerActivity) getActivity();
            parentActivity.setTitle(R.string.plot_fragment_title);

            //override listeners for button so that activity is not called onClick.
            MaterialSpinner sensorTypeSpinner = (MaterialSpinner) fragmentView.findViewById(R.id.sensor_type_selection);
            final List<SensorType> sensorTypes = getSensorTypes();
            CustomArrayAdapter customArrayAdapter = new CustomArrayAdapter(parentActivity,
                    android.R.layout.simple_spinner_item, sensorTypes);
            customArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sensorTypeSpinner.setAdapter(customArrayAdapter);

            sensorTypeSpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            //0th position is a string and if it is selected nothing has to be done.
                            System.out.println(pos);
                            if (pos >= 0) {
                                SensorType sensorType = (SensorType) parent.getItemAtPosition(pos);
                                drawSubView(inflater, sensorType.getSensorTypeId());
                            }
                        }

                        public void onNothingSelected(AdapterView<?> parent) {
                            //Do nothing when nothing is selected.
                        }
                    });

            SensorType.SensorTypeID sensorTypeID = UserPreferencesUtil.getLastViewingSensorType(parentActivity);
            if (sensorTypeID == null)
                sensorTypeID = SensorType.SensorTypeID.SOIL_MOISTURE;
            for(int i=0; i< sensorTypes.size(); ++i ) {
                if (sensorTypes.get(i).getSensorTypeId() == sensorTypeID) {
                    sensorTypeSpinner.setSelection(i);
                    break;
                }
            }

            drawSubView(inflater, sensorTypeID);
        }
        return fragmentView;
    }

    private void drawSubView(LayoutInflater inflater, SensorType.SensorTypeID sensorTypeID) {
        DataProvider dataProvider = new SensorDataProvider(getActivity());
        PlotData plotData = dataProvider.getData(sensorTypeID);

        FrameLayout inclusionViewGroup = (FrameLayout) fragmentView.findViewById(R.id.chart_frame);

        if (inclusionViewGroup.getChildCount() > 0)
            inclusionViewGroup.removeAllViews();

        if (!plotData.isEmpty()) {
            View child = inflater.inflate(R.layout.chart_layout, null);
            inclusionViewGroup.addView(child);
            lineChart = (LineChart) child.findViewById(R.id.chart);

/*                Button oneWeekGraphViewButton = (Button) child.findViewById(R.id.one_week_preset_button);
                oneWeekGraphViewButton.setOnClickListener(this);

                Button oneMonthGraphViewButton = (Button) child.findViewById(R.id.one_month_preset_button);
                oneMonthGraphViewButton.setOnClickListener(this);

                Button defaultGraphViewButton = (Button) child.findViewById(R.id.default_view_preset_button);
                defaultGraphViewButton.setOnClickListener(this);
*/
            setLineChart(plotData);
        } else {
            View child = inflater.inflate(R.layout.no_data_view, null);
            inclusionViewGroup.addView(child);

            Button addDeviceButton = (MorphingButton) fragmentView.findViewById(R.id.add_new_device_btn);
            addDeviceButton.setOnClickListener(this);

            Button addSensorButton = (MorphingButton) fragmentView.findViewById(R.id.add_new_sensor_btn);
            addSensorButton.setOnClickListener(this);
        }
    }

    private List<SensorType> getSensorTypes() {
        SensorTypeDao sensorTypeDao = new SensorTypeDao(parentActivity);
        return sensorTypeDao.fetchAll();
    }

    private void setLineChart(PlotData plotData) {
        //enable zoom behaviour.
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        //Customizing the no text data.
        lineChart.setNoDataText(getResources().getString(R.string.no_data_message));
        lineChart.setNoDataTextDescription(getResources().getString(R.string.no_data_next_action));

        //Paint p = lineChart.getPaint(Chart.PAINT_INFO);
        //p.setColor(Color.WHITE);
        //p.setTypeface(...);
        //p.setTextSize(...);

        //Do not draw anything with the grid.
        lineChart.setDrawGridBackground(false);
        //lineChart.setBackgroundColor(Color.WHITE);

        //A way to paint the background of the grid (works when background color is not set).
        //Paint p1 = lineChart.getPaint(Chart.PAINT_GRID_BACKGROUND);
        //p1.setColor(Color.RED);

        lineChart.getLegend().setWordWrapEnabled(true);
        lineChart.getLegend().setTextColor(Color.BLACK);
        //lineChart.setVisibleXRange(1);
        //lineChart.moveViewToX();

        setChartAxisProperties(lineChart);
        setChartColorProperties(lineChart);

        //DataProvider dataProvider = new RandomDataProvider(getActivity());
        ArrayList<LineDataSet> dataSet = Lists.newArrayList();
        LineColor lineColor = new LineColor();
        for (Map.Entry<Pair<String, String>, List<Entry>> elem : plotData.getData().entrySet()) {
            //generate a random color. Hope that it would not clash,if things are truely random.
            int color = lineColor.getNextColor();
            Pair<String, String> id = elem.getKey();
            //Note that sensor name for display has to be unique and can be used as a key.
            String sensorName = getFormattedSensorName(id.first, id.second);

            LineDataSet lineDataSet = new LineDataSet(elem.getValue(), sensorName);
            lineDataSet.setDrawValues(false);   //removes the values adjacent to point in graph.
            lineDataSet.setColor(color);
            lineDataSet.setCircleColor(color);
            lineDataSet.setCircleSize(2f);

            dataSet.add(lineDataSet);
        }

        LineData data = new LineData(plotData.getXValues(), dataSet);
        lineChart.setData(data);
        lineChart.setDescription(getResources().getString(R.string.graph_label));
        lineChart.setDescriptionColor(Color.WHITE);
        lineChart.animateXY(1000, 1000);
    }

    private void setChartAxisProperties(LineChart lineChart) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.BLACK);
        xAxis.setGridColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setGridColor(Color.BLACK);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    public void setChartColorProperties(LineChart lineChart) {
        lineChart.setDescriptionColor(Color.BLUE);
    }

    private String getFormattedSensorName(String deviceId, String sensorId) {
        return sensorId + "(" + deviceId + ")";
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
/*          UNCOMMENT WHEN YOU ADD MULTIPLE SCALE VIEWS.
            case R.id.one_month_preset_button:
                setOneMonthAxisView(v);
                break;
            case R.id.one_week_preset_button:
                setOneWeekAxisView(v);
                break;
            case R.id.default_view_preset_button:
                setOneMonthAxisView(v);
                break;
*/            case R.id.add_new_device_btn:
                parentActivity.loadFragment(Fragments.ADD_DEVICE, null, false);
                break;
            case R.id.add_new_sensor_btn:
                parentActivity.loadFragment(Fragments.ADD_SENSOR, null, false);
                break;
            default:
                break;
        }
    }

    @Data
    private class CustomArrayAdapter extends ArrayAdapter<SensorType> {

        private List<SensorType> sensorTypes;
        //private Context context;

        public CustomArrayAdapter(Context context, int resourceId,
                                   List<SensorType> sensorTypes) {
            super(context, resourceId, sensorTypes);
            this.sensorTypes = sensorTypes;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent, true);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent, false);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent, boolean isDropDownView) {
            //TODO: use convert view.
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final int resid = isDropDownView ? android.R.layout.simple_spinner_dropdown_item : android.R.layout.simple_spinner_item;
            TextView row = (TextView) inflater.inflate(resid, parent, false);
            row.setText(sensorTypes.get(position).getSensorTypeLabel());

            //Hack: Cannot set the dialogs color from XML and text color from XML
            row.setTextColor(ContextCompat.getColor(parentActivity, R.color.black));
            parent.setBackgroundColor(ContextCompat.getColor(parentActivity, R.color.colorSecondary));
            return row;
        }
    }
}
