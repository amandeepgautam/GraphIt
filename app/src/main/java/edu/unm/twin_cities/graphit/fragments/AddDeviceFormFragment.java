package edu.unm.twin_cities.graphit.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.activity.DrawerActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddDeviceFormFragment extends Fragment implements View.OnClickListener{

    public AddDeviceFormFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_add_device_form, container, false);

        Bundle bundle = getArguments();
        String address = bundle.getString(AddDeviceFragment.PARAM_BLUETOOTH_DEVICE_ADDRESS);

        if (address != null) {
            EditText deviceIdEditTextView = (EditText) fragmentView.findViewById(R.id.device_id_value);
            deviceIdEditTextView.setText(address);
            deviceIdEditTextView.setFocusable(false);
        }

        Button saveDeviceAddSensorButton = (Button) fragmentView.findViewById(R.id.save_device_and_add_sensor_info_button);
        saveDeviceAddSensorButton.setOnClickListener(this);

        Button saveDeviceButton = (Button) fragmentView.findViewById(R.id.save_device_info_button);
        saveDeviceButton.setOnClickListener(this);

        return fragmentView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_device_and_add_sensor_info_button:
                //execute db queries.
                ((DrawerActivity)getActivity()).loadFragment(DrawerActivity.Fragments.ADD_SENSOR, null);
                break;
            case R.id.save_device_info_button:
                ((DrawerActivity)getActivity()).loadFragment(DrawerActivity.Fragments.PLOT_FRAGMENT, null);
                break;
            default:
                break;
        }
    }
}
