package edu.unm.twin_cities.graphit.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.activity.DrawerActivity;

public class AddSensorFragment extends Fragment implements View.OnClickListener {

    public AddSensorFragment() {
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
        View fragmentView = inflater.inflate(R.layout.fragment_add_sensor, container, false);
        Button saveSensorButton = (Button) fragmentView.findViewById(R.id.save_sensor_button);
        saveSensorButton.setOnClickListener(this);

        return fragmentView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_sensor_button:
                //execute db queries.
                ((DrawerActivity)getActivity()).loadFragment(DrawerActivity.Fragments.PLOT_FRAGMENT, null);
                break;
            default:
                break;
        }
    }
}
