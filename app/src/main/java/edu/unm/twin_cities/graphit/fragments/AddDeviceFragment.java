package edu.unm.twin_cities.graphit.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.BaseExpandableListAdapter;

import com.google.common.collect.Sets;

import edu.unm.twin_cities.graphit.activity.DrawerActivity.Fragments;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.activity.DrawerActivity;
import edu.unm.twin_cities.graphit.application.GraphItApplication;
import edu.unm.twin_cities.graphit.util.CommonUtils;
import edu.unm.twin_cities.graphit.util.ConnectionResouceBundle;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResouceManager;
import edu.unm.twin_cities.graphit.util.ServerActionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class AddDeviceFragment extends Fragment implements View.OnClickListener {

    final int REQUEST_ENABLE_BT = 1;
    public static final String PARAM_BLUETOOTH_DEVICE_ADDRESS = "bluetooth_device";

    private final String TAG = this.getClass().getSimpleName();

    private CheckBoxInListViewAdapter checkBoxInListViewAdapter;

    private DrawerActivity parentActivity;

    // Create a BroadcastReceiver for ACTION_FOUND
    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            switch (action) {
                case BluetoothDevice.ACTION_FOUND: {
                    // Get the BluetoothDevice object from the Intent
                    Log.i(TAG, "Found a bluetooth device");
                    BluetoothDevice foundBtDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    checkBoxInListViewAdapter.add(foundBtDevice);
                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
                    Button refresh_list_btn = (Button) fragmentView.findViewById(R.id.refresh_list_btn);
                    if (refresh_list_btn.getVisibility() == View.GONE) {
                        refresh_list_btn.setOnClickListener(AddDeviceFragment.this);
                        refresh_list_btn.setVisibility(View.VISIBLE);
                    }
                    refresh_list_btn.setEnabled(true);
                    parentActivity.setTitle(R.string.add_device_fragment_available_title);
                    Log.i(TAG, "Finished device discovery");
                    break;
                }
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                        Log.i(TAG, "Paired state: " + state);
                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                        Log.i(TAG, "Unpaired state: " + state);
                    }
                    //TODO: Start new activity.
                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED: {
                    Button refresh_list_btn = (Button) fragmentView.findViewById(R.id.refresh_list_btn);
                    parentActivity.setTitle(R.string.add_device_fragment_scanning_title);
                    refresh_list_btn.setEnabled(false);
                    Log.i(TAG, "Starting device discovery");
                    break;
                }
                default:
                    throw new IllegalArgumentException("Action was not expected.");
            }
        }
    };
    private View fragmentView;
    private RemoteConnectionResouceManager connectionManager;

    public AddDeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_add_device, container, false);

        parentActivity = (DrawerActivity) getActivity();
        parentActivity.setTitle(R.string.add_device_fragment_scanning_title);

        GraphItApplication application = ((GraphItApplication)parentActivity.getApplicationContext());
        connectionManager = application.getConnectionManager();

        ListView listView = (ListView) fragmentView.findViewById(R.id.new_discovered_device_list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item,
                                    int position, long id) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) parent.getItemAtPosition(position);

                //Code for pairing the device with Raspberry Pi.
                //if (BluetoothDevice.BOND_BONDED != bluetoothDevice.getBondState()) {
                //    Class clazz = Class.forName("android.bluetooth.BluetoothDevice");
                //    Method createBondMethod = clazz.getMethod("createBond");
                //    createBondMethod.invoke(bluetoothDevice);
                //}

                try {
                    String address = bluetoothDevice.getAddress();

                    Bundle bundle = new Bundle();
                    bundle.putString(PARAM_BLUETOOTH_DEVICE_ADDRESS, address);
                    parentActivity.loadFragment(Fragments.ADD_DEVICE_FORM, bundle);
                } catch (Exception e) { //application should not crash in such a case. Log and ignore the exception.
                    Log.e(TAG, "We go an exception", e);
                }
            }
        });
        checkBoxInListViewAdapter = new CheckBoxInListViewAdapter(parentActivity, R.layout.available_device_info, new ArrayList<BluetoothDevice>());
        checkBoxInListViewAdapter.setNotifyOnChange(true);
        listView.setAdapter(checkBoxInListViewAdapter);

        BluetoothAdapter bluetoothAdapter = CommonUtils.getBluetoothAdapter(parentActivity, REQUEST_ENABLE_BT);
        scanForAvailableDevice(bluetoothAdapter);

        return fragmentView;
    }

    private void scanForAvailableDevice(BluetoothAdapter bluetoothAdapter) {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED); //TODO: Change this to appropriate place, which is onclick listener.
        parentActivity.registerReceiver(broadcastReceiver, filter); // TODO: Don't forget to unregister during onDestroy
        bluetoothAdapter.startDiscovery();
    }

    @Override
    public void onDestroy() {
        try {
            parentActivity.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException iae) {
            /* DO NOTHING. As per:
            http://stackoverflow.com/questions/6165070/receiver-not-registered-exception-error */
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh_list_btn:
                //execute db queries.
                BluetoothAdapter bluetoothAdapter = CommonUtils.getBluetoothAdapter(parentActivity, REQUEST_ENABLE_BT);
                scanForAvailableDevice(bluetoothAdapter);
                break;
            default:
                break;
        }
    }

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView pairedInfo;
    }

    /**
     * Class contains the logic to convert data items to view items.
     */
    @Data
    protected class CheckBoxInListViewAdapter extends BaseExpandableListAdapter {
        /**
         * List of items.
         */
        private List<BluetoothDevice> rows;

        private Set<BluetoothDevice> ineligibleDevices;

        public ExpandableListAdapter(String[] groups, String[][] children) {
            this.groups = groups;
            this.children = children;
            inf = LayoutInflater.from(getActivity());
        }


        public CheckBoxInListViewAdapter(Context context, int resourceId,
                                         List<BluetoothDevice> rows) {
            super(context, resourceId, rows);
            this.rows = rows;
            ineligibleDevices = Sets.newHashSet();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 0;
        }


        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent){
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {

        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        @Override
        public int getGroupCount() {
            return 0;
        }

        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // The child views in each row.
            ViewHolder viewHolder;
            TextView deviceNameTextView;
            TextView deviceAddressTextView;
            TextView pairedInfoTextView;

            // Creates a new row view.
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) parentActivity.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.available_device_info, parent, false);

                //Find the child views.
                deviceNameTextView = (TextView) convertView.findViewById(R.id.name_available_device);
                deviceAddressTextView = (TextView) convertView.findViewById(R.id.address_available_device);
                pairedInfoTextView = (TextView) convertView.findViewById(R.id.paired_info);

                viewHolder = new ViewHolder(deviceNameTextView, deviceAddressTextView, pairedInfoTextView);
                // Tag the row with it's child view for future use.
                convertView.setTag(viewHolder);

            } else { // reuse the existing row. Hence optimization by tagging the object.
                viewHolder = (ViewHolder) convertView.getTag();
                deviceNameTextView = viewHolder.getDeviceName();
                deviceAddressTextView = viewHolder.getDeviceAddress();
                pairedInfoTextView = viewHolder.getPairedInfo();
            }

            // Retrieve the appropriate  item to display from data source.
            BluetoothDevice bluetoothDevice = rows.get(position);

            // Get data for display.
            deviceNameTextView.setText(bluetoothDevice.getName());
            deviceAddressTextView.setText(bluetoothDevice.getAddress());
            pairedInfoTextView.setText(getBondState(bluetoothDevice));
            return convertView;
        }

        /**
         * Add the device when it can communicate with the application and is not already
         * added to the list of devices.
         * @param bluetoothDevice
         */
        @Override
        public void add(BluetoothDevice bluetoothDevice) {
            boolean existing  = false;
            boolean toAdd = false;
            // Devices would be very less in number, scan through the list is better than
            // maintaining a data structure for the purpose.
            if (!ineligibleDevices.contains(bluetoothDevice)) {
                for (BluetoothDevice elem : rows) {
                    if (bluetoothDevice.getAddress().equals(elem.getAddress())) {
                        existing = true;
                        break;
                    }
                }
                if (!existing) {
                    try {
                        ConnectionResouceBundle connectionResouceBundle = connectionManager
                                .getConnectionResource(bluetoothDevice, parentActivity.getApplicationContext());
                        ServerActionUtil serverActionUtil = new ServerActionUtil(connectionResouceBundle);
                        if (serverActionUtil.ping()) {
                            toAdd = true;
                        }
                    } catch (IOException ioe) {
                        /* The device is not THE ONE we are looking for.**/
                    }
                }
            }
            if (!existing && toAdd) {
                super.add(bluetoothDevice);
            }
        }
    }

    public String getBondState(BluetoothDevice bluetoothDevice) {
        int bondedState = bluetoothDevice.getBondState();
        if(bondedState == BluetoothDevice.BOND_BONDED) {
            return getResources().getString(R.string.paired);
        } else if (bondedState == BluetoothDevice.BOND_BONDING) {
            return getResources().getString(R.string.pairing);
        } else if (bondedState == BluetoothDevice.BOND_NONE) {
            return getResources().getString(R.string.unpaired);
        }
        throw new IllegalStateException("Unknown/Illegal connection state of the remote device.");
    }
}
