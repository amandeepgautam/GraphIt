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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.BaseExpandableListAdapter;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.activity.DrawerActivity;
import edu.unm.twin_cities.graphit.application.GraphItApplication;
import edu.unm.twin_cities.graphit.processor.dao.DeviceDao;
import edu.unm.twin_cities.graphit.processor.model.Device;
import edu.unm.twin_cities.graphit.service.ServerActionsService;
import edu.unm.twin_cities.graphit.util.CommonUtils;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResourceManager;
import lombok.AllArgsConstructor;
import lombok.Data;

public class AddDeviceFragment extends Fragment {

    final int REQUEST_ENABLE_BT = 1;
    public static final String PARAM_BLUETOOTH_DEVICE_ADDRESS = "bluetooth_device";

    private final String TAG = this.getClass().getSimpleName();

    private CheckBoxInListViewAdapter checkBoxInListViewAdapter;

    private DrawerActivity parentActivity;

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
                case BluetoothDevice.ACTION_FOUND: {
                    // Get the BluetoothDevice object from the Intent
                    Log.i(TAG, "Found a bluetooth device");
                    BluetoothDevice foundBtDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    ++found;
                    checkBoxInListViewAdapter.isCompatibleDevice(foundBtDevice);
                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
                    isDiscoveryRunning = false;
                    if(found == processed) {
                        afterAvailableDevicesUpdatedAction();
                    }
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
                    isDiscoveryRunning = true;
                    AddDeviceFragment.this.setRefreshActionButtonState(true);
                    parentActivity.setTitle(R.string.add_device_fragment_scanning_title);
                    Log.i(TAG, "Starting device discovery");
                    break;
                }
                case ServerActionsService.ACTION_PING: {
                    boolean pingResponse = intent.getBooleanExtra(ServerActionsService.PARAM_IS_PING_SUCCESSFUL, false);
                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(ServerActionsService.PARAM_BLUETOOTH_DEVICE);

                    if (bluetoothDevice == null) {
                        throw new IllegalStateException("Bluetooth device should be passed.");
                    }

                    boolean isPresent = checkBoxInListViewAdapter.isDevicePresent(bluetoothDevice);
                    if (pingResponse && !isPresent) {
                        checkBoxInListViewAdapter.addDevice(bluetoothDevice);
                    } else if(!pingResponse && isPresent) {
                        checkBoxInListViewAdapter.removeDevice(bluetoothDevice);
                    }

                    ++processed;
                    if(!isDiscoveryRunning && found == processed) {
                        afterAvailableDevicesUpdatedAction();
                    }

                    break;
                }
                default:
                    throw new IllegalArgumentException("Action was not expected.");
            }
        }

        public void afterAvailableDevicesUpdatedAction() {
            AddDeviceFragment.this.setRefreshActionButtonState(false);
            parentActivity.setTitle(R.string.add_device_fragment_available_title);
        }
    };

    private Menu optionsMenu;
    private View fragmentView;
    private RemoteConnectionResourceManager connectionManager;
    private ExpandableListView listView;

    public AddDeviceFragment() {
        setArguments(new Bundle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_add_device, container, false);

        parentActivity = (DrawerActivity) getActivity();
        parentActivity.setTitle(R.string.add_device_fragment_scanning_title);


        GraphItApplication application = ((GraphItApplication) parentActivity.getApplicationContext());
        connectionManager = application.getConnectionManager();

        listView = (ExpandableListView) fragmentView.findViewById(R.id.new_discovered_device_list);

/*        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });*/

        checkBoxInListViewAdapter = new CheckBoxInListViewAdapter(new ArrayList<BluetoothDevice>(),
                new HashMap<Integer, String>());
        listView.setAdapter(checkBoxInListViewAdapter);
        int right = (int) (getResources().getDisplayMetrics().widthPixels - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()));
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            listView.setIndicatorBounds(right - 40, right);
        } else {
            listView.setIndicatorBoundsRelative(right - -40, right);
        }

        listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != previousGroup)
                    listView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });

        //checkBoxInListViewAdapter = new CheckBoxInListViewAdapter(parentActivity, R.layout.available_device_info, new ArrayList<BluetoothDevice>());
        //checkBoxInListViewAdapter.setNotifyOnChange(true);
        //listView.setAdapter(checkBoxInListViewAdapter);

        BluetoothAdapter bluetoothAdapter = CommonUtils.getBluetoothAdapter(parentActivity, REQUEST_ENABLE_BT);
        //parentActivity.registerReceiver(broadcastReceiver, CommonUtils.getBluetoothIntentFilter());
        registerIntents();
        bluetoothAdapter.startDiscovery();
        //CommonUtils.startBluetoothDiscovery(parentActivity, REQUEST_ENABLE_BT, broadcastReceiver);
        //scanForAvailableDevice(bluetoothAdapter);

        return fragmentView;
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu
                    .findItem(R.id.custom_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.progress_bar);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.optionsMenu = menu;
        inflater.inflate(R.menu.menu_add_device_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.custom_refresh:
                BluetoothAdapter bluetoothAdapter = CommonUtils.getBluetoothAdapter(parentActivity, REQUEST_ENABLE_BT);
                boolean startDiscovery = bluetoothAdapter.startDiscovery();
                setRefreshActionButtonState(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        // When the activity is called for the first time, intents would be registered twice and
        // generally this is not an issue.
        registerIntents();
        super.onResume();
    }

    private void registerIntents() {
        IntentFilter blutoothIntentFilter = CommonUtils.getBluetoothIntentFilter();
        parentActivity.registerReceiver(broadcastReceiver, blutoothIntentFilter);

        IntentFilter pingIntentFilter = new IntentFilter();
        pingIntentFilter.addAction(ServerActionsService.ACTION_PING);
        parentActivity.registerReceiver(broadcastReceiver, pingIntentFilter);
    }

    private void unregisterIntents() {
        try {
            parentActivity.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException iae) {
            /* DO NOTHING. As per:
            http://stackoverflow.com/questions/6165070/receiver-not-registered-exception-error */
        }
    }

    @Override
    public void onPause() {
        unregisterIntents();
        super.onPause();
    }

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    private static class GroupViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    private static class ItemViewHolder {
        MaterialEditText deviceNameValue;
        Button addDevice;
    }

    private class ViewHolder {
        TextView text;
    }

    /**
     * Class contains the logic to convert data items to view items.
     */
    @Data
    protected class CheckBoxInListViewAdapter extends BaseExpandableListAdapter {
        /**
         * List of items.
         */
        private List<BluetoothDevice> groups;
        private Map<Integer, String> children;  //Map of index in group to string.

        public CheckBoxInListViewAdapter(ArrayList<BluetoothDevice> groups, Map<Integer, String> children) {
            this.groups = groups;
            this.children = children;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groups.get(groupPosition);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupViewHolder groupViewHolder;
            final TextView deviceNameTextView;
            final TextView deviceAddressTextView;

            // Creates a new row view.
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) parentActivity.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_group, parent, false);

                //Find the child views.
                deviceNameTextView = (TextView) convertView.findViewById(R.id.name_available_device);
                deviceAddressTextView = (TextView) convertView.findViewById(R.id.address_available_device);

                groupViewHolder = new GroupViewHolder(deviceNameTextView, deviceAddressTextView);
                // Tag the row with it's child view for future use.
                convertView.setTag(groupViewHolder);

            } else { // reuse the existing row. Hence optimization by tagging the object.
                groupViewHolder = (GroupViewHolder) convertView.getTag();
                deviceNameTextView = groupViewHolder.getDeviceName();
                deviceAddressTextView = groupViewHolder.getDeviceAddress();
            }

            // Retrieve the appropriate  item to display from data source.
            BluetoothDevice bluetoothDevice = groups.get(groupPosition);

            // Get data for display.
            deviceNameTextView.setText(bluetoothDevice.getName());
            String deviceAddressHelpText = getString(R.string.device_address_help_text);
            deviceAddressTextView.setText(deviceAddressHelpText + bluetoothDevice.getAddress());
            return convertView;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return children.get(groupPosition);
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition,final boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            ItemViewHolder holder;
            final MaterialEditText deviceNameEditTextView;
            final Button saveInfoButton;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) parentActivity.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item, parent, false);
                //Find the child views.
                deviceNameEditTextView = (MaterialEditText) convertView.findViewById(R.id.device_name_text);
                saveInfoButton = (Button) convertView.findViewById(R.id.save_device_info_button);

                holder = new ItemViewHolder(deviceNameEditTextView, saveInfoButton);

                convertView.setTag(holder);
            } else {
                holder = (ItemViewHolder) convertView.getTag();
                deviceNameEditTextView = holder.getDeviceNameValue();
                saveInfoButton = holder.getAddDevice();
            }

            // Retrieve the appropriate  item to display from data source.
            final String childName = (String) getChild(groupPosition, childPosition);
            deviceNameEditTextView.setText(childName);

            saveInfoButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    BluetoothDevice bluetoothDevice = (BluetoothDevice) getGroup(groupPosition);
                    String deviceId = bluetoothDevice.getAddress();

                    String deviceLabel = deviceNameEditTextView.getText().toString();
                    if (deviceLabel == null || deviceLabel.isEmpty()) {
                        deviceLabel = deviceNameEditTextView.getHint().toString();
                    }

                    Device device = new Device(deviceId, deviceLabel);
                    DeviceDao deviceDao = new DeviceDao(parentActivity);
                    deviceDao.insert(device);

                    listView.collapseGroup(groupPosition);
                }
            });
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public int getGroupCount() {
            return groups.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public boolean isDevicePresent(BluetoothDevice bluetoothDevice) {
            // Devices would be very less in number, scan through the list is better than
            // maintaining a data structure for the purpose.
            for (BluetoothDevice elem : groups) {
                if (bluetoothDevice.getAddress().equals(elem.getAddress())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Add the device when it can communicate with the application and is not already
         * added to the list of devices.
         * @param bluetoothDevice
         */
        public void isCompatibleDevice(BluetoothDevice bluetoothDevice) {
            Intent intent = new Intent(parentActivity, ServerActionsService.class);
            intent.setAction(ServerActionsService.ACTION_PING);
            intent.putExtra(ServerActionsService.PARAM_BLUETOOTH_DEVICE, bluetoothDevice);
            parentActivity.startService(intent);
        }

        public void addDevice(BluetoothDevice bluetoothDevice) {
            groups.add(bluetoothDevice);
            children.put(groups.size() - 1, bluetoothDevice.getName());
            this.notifyDataSetChanged();
        }

        public void removeDevice(BluetoothDevice bluetoothDevice) {
            return ;
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
