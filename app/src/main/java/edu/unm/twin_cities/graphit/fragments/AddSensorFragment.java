package edu.unm.twin_cities.graphit.fragments;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dd.morphingbutton.MorphingButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.activity.DrawerActivity;
import edu.unm.twin_cities.graphit.application.GraphItApplication;
import edu.unm.twin_cities.graphit.processor.dao.DeviceDao;
import edu.unm.twin_cities.graphit.processor.dao.SensorDao;
import edu.unm.twin_cities.graphit.processor.dao.SensorTypeDao;
import edu.unm.twin_cities.graphit.processor.model.Device;
import edu.unm.twin_cities.graphit.processor.model.Sensor;
import edu.unm.twin_cities.graphit.processor.model.SensorType;
import edu.unm.twin_cities.graphit.service.DataService;
import edu.unm.twin_cities.graphit.util.CommonUtils;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResourceManager;
import fr.ganfra.materialspinner.MaterialSpinner;
import lombok.Data;

public class AddSensorFragment extends Fragment implements View.OnClickListener {


    public static final String PARAM_BLUETOOTH_DEVICE = "bluetooth_device";

    private static final String SENSOR_TYPE_VALUES = "sensor_type_values";
    private static final String REGISTERED_DEVICES_VALUES = "registered_devices_values";
    private static final String SENSOR_TYPE_SELECTED_INDEX = "sensor_type_selected_index";
    private static final String REGISTERED_DEVICES_SELECTED_INDEX = "registered_devices_selected_index";
    private static final String SENSOR_NAME = "sensor_name";
    private static final String FILE_PATH = "file_path";


    private final String TAG = this.getClass().getSimpleName();
    private final int REQUEST_BT_ENABLE = 4;

    private View fragmentView;
    private MorphingButton saveSensorButton;
    private MaterialSpinner registeredDevicesDropdown;
    private MaterialSpinner sensorTypeDropdown;
    private MaterialEditText sensorNameValue;
    private MaterialEditText filePathValue;

    private String dummyVal;

    private DrawerActivity parentActivity;

    private RemoteConnectionResourceManager connectionManager;
    private CustomArrayAdapter customArrayAdapter = null;
    private CustomArrayAdapter1 customArrayAdapter1 = null;

    public AddSensorFragment() {
        setArguments(new Bundle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // If the class has not been created, initialize the members but if created before
        // then it is assumed that all members were correctly initialized and hence only one
        // member is check for nullability.
        if (parentActivity == null) {
            parentActivity = (DrawerActivity) getActivity();
            parentActivity.setTitle(R.string.add_sensor_fragment_title);

            GraphItApplication application = ((GraphItApplication) parentActivity.getApplicationContext());
            connectionManager = application.getConnectionManager();

            fragmentView = inflater.inflate(R.layout.fragment_add_sensor, container, false);
            saveSensorButton = (MorphingButton) fragmentView.findViewById(R.id.save_sensor_button);
            saveSensorButton.setOnClickListener(this);

            registeredDevicesDropdown = (MaterialSpinner) fragmentView.findViewById(R.id.attached_device_value_dropdown);

            sensorTypeDropdown = (MaterialSpinner) fragmentView.findViewById(R.id.sensor_type_value_dropdown);

            sensorNameValue = (MaterialEditText) fragmentView.findViewById(R.id.sensor_name_value);

            filePathValue = (MaterialEditText) fragmentView.findViewById(R.id.device_file_location_value);
            filePathValue.setOnClickListener(this);
        } else {
            // If instance is not being created see if the values of existing elements need to be
            // updated.
            Bundle bundle = getArguments();
            final String filePath = bundle.getString(FileBrowserFragment.PARAM_FILE_PATH);
            if (filePath != null) {
                filePathValue.post(new Runnable() {
                    @Override
                    public void run() {
                        filePathValue.setText(filePath);
                    }
                });
                bundle.remove(FileBrowserFragment.PARAM_FILE_PATH);
                dummyVal = filePath;
            }
        }
        return fragmentView;
    }

    private List<Device> getRegisteredDevices() {
        DeviceDao deviceDao = new DeviceDao(parentActivity);
        return deviceDao.fetchAll();
    }

    private List<SensorType> getSensorTypes() {
        SensorTypeDao sensorTypeDao = new SensorTypeDao(parentActivity);
        return sensorTypeDao.fetchAll();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_sensor_button: {
                //execute db queries.
                String sensorName = sensorNameValue.getText().toString();
                if (sensorName.isEmpty()) {
                    sensorNameValue.setError(getResources()
                            .getString(R.string.error_msg_no_sensor_name));
                }

                Object obj = registeredDevicesDropdown.getSelectedItem();
                String deviceId = null;
                if (obj instanceof Device) {
                    deviceId = ((Device) obj).getDeviceId();
                } else {
                    registeredDevicesDropdown.setError(R.string.error_msg_valid_device_selection);
                }

                obj = sensorTypeDropdown.getSelectedItem();
                SensorType.SensorTypeID sensorTypeId = null;
                if (obj instanceof SensorType) {
                    sensorTypeId = ((SensorType) obj).getSensorTypeId();
                } else {
                    sensorTypeDropdown.setError(R.string.error_msg_no_sensor_type);
                }

                String filePath = filePathValue.getText().toString();
                if (filePath.isEmpty()) {
                    filePathValue.setError(getResources().getString(R.string.error_msg_no_file_path));
                }

                if (!sensorName.isEmpty() && !filePath.isEmpty() &&
                        sensorTypeId != null && deviceId != null) {
                    Sensor sensor = new Sensor(deviceId, sensorName, sensorName, null,
                            filePath, sensorTypeId, System.currentTimeMillis());

                    SensorDao sensorDao = new SensorDao(parentActivity);
                    sensorDao.insert(sensor);
                    parentActivity.loadFragment(DrawerActivity.Fragments.PLOT_FRAGMENT, null, false);
                }
                break;
            }
            case R.id.device_file_location_value: {
                Object obj = registeredDevicesDropdown.getSelectedItem();
                if (obj instanceof Device) {
                    Device device = (Device) obj;
                    BluetoothDevice bluetoothDevice = CommonUtils
                            .getBluetoothAdapter(parentActivity, REQUEST_BT_ENABLE)
                            .getRemoteDevice(device.getDeviceId());

                    GetConnectionResourceTask task = new GetConnectionResourceTask(connectionManager,
                            bluetoothDevice, parentActivity);
                    task.execute();

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(PARAM_BLUETOOTH_DEVICE, bluetoothDevice);
                    parentActivity.loadFragment(DrawerActivity.Fragments.FILE_BROWSER, bundle, true);
                } else if (obj instanceof String) {
                    registeredDevicesDropdown.setError(R.string.error_msg_valid_device_selection);
                    Log.i(TAG, "Trying to access file browsing without device selection.");
                }
                break;
            }
            default:
                //do nothing.
                break;
        }
    }

    private class GetConnectionResourceTask extends AsyncTask<Void, Void, Boolean> {

        private final BluetoothDevice bluetoothDevice;
        private final RemoteConnectionResourceManager remoteConnectionResourceManager;
        private final DrawerActivity parentActivity;
        private Dialog dialog;

        public GetConnectionResourceTask(RemoteConnectionResourceManager remoteConnectionResourceManager,
                                         BluetoothDevice bluetoothDevice, DrawerActivity parentActivity) {
            this.bluetoothDevice = bluetoothDevice;
            this.remoteConnectionResourceManager = remoteConnectionResourceManager;
            this.parentActivity = parentActivity;
        }

        @Override
        protected void onPreExecute() {
            dialog = new MaterialDialog.Builder(parentActivity)
                    .title(R.string.connection_msg)
                    .content(R.string.connection_detail_msg)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            try {
                remoteConnectionResourceManager.getConnectionResource(bluetoothDevice, parentActivity);
                return true;
            } catch (IOException ioe) {
                //TODO: set up notification.
                Log.e(TAG, "The device is unavailable currently.", ioe);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            dialog.dismiss();
        }
    }

    /**
     * Save the instance state as another fragment is loaded from this fragment.
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<SensorType> sensorTypes = customArrayAdapter1.getSensorTypes();
        outState.putParcelableArray(SENSOR_TYPE_VALUES,
                sensorTypes.toArray(new SensorType[sensorTypes.size()]));
        List<Device> deivces = customArrayAdapter.getDevices();
        outState.putParcelableArray(REGISTERED_DEVICES_VALUES,
                deivces.toArray(new Device[deivces.size()]));

        outState.putInt(REGISTERED_DEVICES_SELECTED_INDEX, registeredDevicesDropdown.getSelectedItemPosition());
        outState.putInt(SENSOR_TYPE_SELECTED_INDEX, sensorTypeDropdown.getSelectedItemPosition());

        outState.putString(SENSOR_NAME, sensorNameValue.getText().toString());
        outState.putString(FILE_PATH, filePathValue.getText().toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<SensorType> sensorTypes = null;
        List<Device> devices = null;
        if (savedInstanceState != null) {

            SensorType[] sensorTypeArray = (SensorType[]) savedInstanceState.getParcelableArray(SENSOR_TYPE_VALUES);
            sensorTypes = Arrays.asList(sensorTypeArray);   //sensorTypeArray will never be null.

            Device[] deviceArray = (Device []) savedInstanceState.getParcelableArray(REGISTERED_DEVICES_VALUES);
            devices = Arrays.asList(deviceArray);   //deviceArray will never be null.

            sensorNameValue.setText(savedInstanceState.getString(SENSOR_NAME));
            filePathValue.setText(savedInstanceState.getString(FILE_PATH));

            registeredDevicesDropdown.setSelection(
                    savedInstanceState.getInt(REGISTERED_DEVICES_SELECTED_INDEX));

            sensorTypeDropdown.setSelection(
                    savedInstanceState.getInt(SENSOR_TYPE_SELECTED_INDEX));
        }

        if (customArrayAdapter == null || savedInstanceState != null) {
            devices = getRegisteredDevices();
            customArrayAdapter = new CustomArrayAdapter(parentActivity, android.R.layout.simple_spinner_item, devices);
            customArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            registeredDevicesDropdown.setAdapter(customArrayAdapter);
        }

        if (customArrayAdapter1 == null || savedInstanceState != null) {
            sensorTypes = getSensorTypes();
            customArrayAdapter1 = new CustomArrayAdapter1(parentActivity, android.R.layout.simple_spinner_item,
                    sensorTypes);
            customArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sensorTypeDropdown.setAdapter(customArrayAdapter1);
        }
    }

    @Data
    public class CustomArrayAdapter extends ArrayAdapter<Device> {

        private List<Device> devices;
        //private Context context;

        public CustomArrayAdapter(Context context, int resourceId,
                                  List<Device> devices) {
            super(context, resourceId, devices);
            this.devices = devices;
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
            row.setText(devices.get(position).getDeviceLabel());

            //Hack: Cannot set the dialogs color from XML and text color from XML
            row.setTextColor(ContextCompat.getColor(parentActivity, R.color.black));
            parent.setBackgroundColor(ContextCompat.getColor(parentActivity, R.color.colorSecondary));
            return row;
        }
    }

    @Data
    public class CustomArrayAdapter1 extends ArrayAdapter<SensorType> {

        private List<SensorType> sensorTypes;
        //private Context context;

        public CustomArrayAdapter1(Context context, int resourceId,
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
