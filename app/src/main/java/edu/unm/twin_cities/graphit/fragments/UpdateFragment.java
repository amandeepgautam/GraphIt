package edu.unm.twin_cities.graphit.fragments;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.AvoidXfermode;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dd.morphingbutton.MorphingButton;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hanks.library.AnimateCheckBox;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umn.twin_cities.ServerAction;
import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.activity.DrawerActivity;
import edu.unm.twin_cities.graphit.application.GraphItApplication;
import edu.unm.twin_cities.graphit.processor.dao.DeviceDao;
import edu.unm.twin_cities.graphit.processor.dao.SensorDao;
import edu.unm.twin_cities.graphit.processor.model.Device;
import edu.unm.twin_cities.graphit.processor.model.DeviceSensorMap;
import edu.unm.twin_cities.graphit.processor.model.Sensor;
import edu.unm.twin_cities.graphit.service.DataService;
import edu.unm.twin_cities.graphit.service.ServerActionsService;
import edu.unm.twin_cities.graphit.util.CommonUtils;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResourceManager;
import lombok.AllArgsConstructor;
import lombok.Data;

public class UpdateFragment extends Fragment implements View.OnClickListener {

    public static final String PARAM_DEVICE = "sensor_device_map";
    public static final String PARAM_FILENAME_SENSOR_INFO = "filename_sensor_info";
    public static final String PARAM_FILENAME = "file_name";
    public static final String PARAM_OPERATION_SUCCESSFUL = "operation_successful";

    private final int REQUEST_BT_ENABLE = 5;

    // Create a BroadcastReceiver for ACTION_FOUND
    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        private int found = 0;  // keeps track of devices which are found and processed,
        private int processed = 0;     //keeps track of items which are pinged
        private boolean isDiscoveryRunning = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            switch (action) {
                case ServerActionsService.ACTION_TRANSFER_FILE_AND_INSERT_DATA_INITIATED: {
                    break;
                }
                case ServerActionsService.ACTION_TRANSFER_FILE_AND_INSERT_DATA_STARTED: {
                    BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getExtras()
                            .get(ServerActionsService.PARAM_BLUETOOTH_DEVICE);
                    //dialog.setTitle(R.string.connection_msg);
                    break;
                }
                case ServerActionsService.ACTION_TRANSFER_FILE_AND_INSERT_DATA_FINISHED: {
                    System.out.print("here");
                    //todo:
                    break;
                }
                case ServerActionsService.ACTION_TRANSFER_FILE_AND_INSERT_DATA_ENDED: {
                    parentActivity.loadFragment(DrawerActivity.Fragments.PLOT_FRAGMENT, null, false);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Action was not expected.");
            }
        }
    };

    private View fragmentView;

    private DrawerActivity parentActivity;
    private ListView deviceListView;

    public UpdateFragment() {
        setArguments(new Bundle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.fragment_update, container, false);

            parentActivity = (DrawerActivity) getActivity();
            parentActivity.setTitle(R.string.update_graph_fragment_title);

            deviceListView = (ListView) fragmentView.findViewById(R.id.update_devices_list);
            List<Model> models = Lists.newArrayList();
            for (Device device : getDevices()) {
                models.add(new Model(device, false));
            }

            deviceListView.setAdapter(new CustomAdapter(parentActivity, R.layout.select_devices_item, models));
            deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                    Model model = (Model) parent.getItemAtPosition(position);
                    boolean isChecked = model.isChecked();
                    model.setChecked(!isChecked);

                    ViewHolder viewHolder = (ViewHolder) view.getTag();
                    viewHolder.getCheckBox().setChecked(!isChecked);
                }
            });


            MorphingButton button = (MorphingButton) fragmentView.findViewById(R.id.update_selected_devices);
            button.setOnClickListener(this);
        }
        return fragmentView;
    }

    private List<Device> getDevices() {
        DeviceDao deviceDao = new DeviceDao(parentActivity);
        return deviceDao.fetchAll();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_selected_devices:
                updateData();
                break;
            default:
                throw new IllegalStateException("Unknown action.");
        }
    }

    public void updateData() {
        //Get checked devices from the list.
        List<Device> devices = Lists.newArrayList();
        CustomAdapter customAdapter = (CustomAdapter) deviceListView.getAdapter();
        for (int i = 0; i < customAdapter.getCount(); i++) {
            Model model = customAdapter.getItem(i);
            if (model.isChecked()) {
                devices.add(model.getDevice());
            }
        }

        SensorDao sensorDao = new SensorDao(parentActivity);
        Map<Device, List<Sensor>> sensorDeviceMap = sensorDao.fetchDeviceSpecificSensors(devices);

        // Aggregate the information about sensor and devices based on unique
        // Map<BluetoothDevice, Map<FileName, List<SensorLabels>>>
        Map<BluetoothDevice, Map<String, List<String>>> rearrangedSensorDeviceInfo = Maps.newHashMap();
        for (Map.Entry<Device, List<Sensor>> elem : sensorDeviceMap.entrySet()) {
            Device device = elem.getKey();
            BluetoothDevice bluetoothDevice = CommonUtils.getBluetoothAdapter(
                    parentActivity, REQUEST_BT_ENABLE).getRemoteDevice(device.getDeviceId());
            Map<String, List<String>> fileNameSensorNameMap = rearrangedSensorDeviceInfo.get(bluetoothDevice);
            List<Sensor> sensors = elem.getValue();

            if (fileNameSensorNameMap == null) {
                fileNameSensorNameMap = Maps.newHashMap();
                rearrangedSensorDeviceInfo.put(bluetoothDevice, fileNameSensorNameMap);
            }

            for(Sensor sensor : sensors) {
                String dataFilePath = sensor.getSensorDataFilePath();
                String sensorName = sensor.getSensorLabel();
                List<String> sensorNames = fileNameSensorNameMap.get(dataFilePath);
                if (sensorNames == null) {
                    fileNameSensorNameMap.put(dataFilePath, Lists.newArrayList(sensorName));
                } else {
                    sensorNames.add(sensorName);
                }
            }
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServerActionsService.ACTION_TRANSFER_FILE_AND_INSERT_DATA_STARTED);
        intentFilter.addAction(ServerActionsService.ACTION_TRANSFER_FILE_AND_INSERT_DATA_FINISHED);
        intentFilter.addAction(ServerActionsService.ACTION_TRANSFER_FILE_AND_INSERT_DATA_ENDED);
        intentFilter.addAction(ServerActionsService.ACTION_TRANSFER_FILE_AND_INSERT_DATA_INITIATED);
        parentActivity.registerReceiver(broadcastReceiver, intentFilter);

        //Start service for every device.
        for (Map.Entry<BluetoothDevice, Map<String, List<String>>> elem : rearrangedSensorDeviceInfo.entrySet()) {
            BluetoothDevice device = elem.getKey();
            Map<String, List<String>> fileNameSensorsMap = elem.getValue();
            if (fileNameSensorsMap.size() > 0) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(ServerActionsService.PARAM_BLUETOOTH_DEVICE, device);
                bundle.putSerializable(PARAM_FILENAME_SENSOR_INFO, (Serializable) fileNameSensorsMap);

                Intent intent = new Intent(parentActivity, ServerActionsService.class);
                intent.setAction(ServerActionsService.ACTION_TRANSFER_FILE_AND_INSERT_DATA);
                intent.putExtras(bundle);

                parentActivity.startService(intent);
            }
        }
    }

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    public class Model {
        private Device device;
        private boolean checked;
    }

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    private static class ViewHolder {
        private TextView device;
        private AnimateCheckBox checkBox;
    }

    @Data
    public class CustomAdapter extends ArrayAdapter<Model> {
        List<Model> rows = null;
        Context context;
        int resourceId;

        public CustomAdapter(Context context, int resourceId, List<Model> rows) {
            super(context, resourceId, rows);
            this.context = context;
            this.rows = rows;
            this.resourceId = resourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // The child views in each row.
            ViewHolder viewHolder;
            TextView textView;
            AnimateCheckBox checkBox;

            // Creates a new row view.
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) parentActivity.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(resourceId, parent, false);

                //Find the child views.
                textView = (TextView) convertView.findViewById(R.id.device);
                checkBox = (AnimateCheckBox) convertView.findViewById(R.id.item_selected);

                checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        AnimateCheckBox cb = (AnimateCheckBox) v;
                        boolean isChecked = cb.isChecked();
                        cb.setChecked(!isChecked);
                        Model model = (Model) cb.getTag();
                        model.setChecked(!isChecked);
                    }
                });

                viewHolder = new ViewHolder(textView, checkBox);
                // Tag the row with it's child view for future use.
                convertView.setTag(viewHolder);

            } else { // reuse the existing row. Hence optimization by tagging the object.
                viewHolder = (ViewHolder) convertView.getTag();
                textView = viewHolder.getDevice();
                checkBox = viewHolder.getCheckBox();
            }

            // Retrieve the appropriate  item to display from data source.
            Model model = rows.get(position);
            checkBox.setTag(model);

            // Get data for display.
            textView.setText(model.getDevice().getDeviceLabel());
            checkBox.setChecked(model.isChecked());
            return convertView;
        }
    }
}
