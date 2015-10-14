package edu.unm.twin_cities.graphit.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import edu.unm.twin_cities.graphit.R;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Base class for list view with checked boxes.
 */
@Data
public abstract class CheckBoxInListViewActivity extends AppCompatActivity {

    private CheckBoxInListViewAdapter checkBoxInListViewAdapter;

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    /**
     * Class contains the logic to convert data items to view items.
     */
    @Data
    protected class CheckBoxInListViewAdapter extends ArrayAdapter<BluetoothDevice> {
        /**
         * List of items.
         */
        private List<BluetoothDevice> rows;

        public CheckBoxInListViewAdapter(Context context, int resourceId,
                                         List<BluetoothDevice> rows) {
            super(context, resourceId, rows);
            this.rows = rows;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // The child views in each row.
            ViewHolder viewHolder;
            TextView deviceNameTextView;
            TextView deviceAddressTextView;

            // Creates a new row view.
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.paired_device_info, parent, false);

                //Find the child views.
                deviceNameTextView = (TextView) convertView.findViewById(R.id.name_device);
                deviceAddressTextView = (TextView) convertView.findViewById(R.id.address_device);

                viewHolder = new ViewHolder(deviceNameTextView, deviceAddressTextView);
                // Tag the row with it's child view for future use.
                convertView.setTag(viewHolder);

            } else { // reuse the existing row. Hence optimization by tagging the object.
                viewHolder = (ViewHolder) convertView.getTag();
                deviceNameTextView = viewHolder.getDeviceName();
                deviceAddressTextView = viewHolder.getDeviceAddress();
            }

            // Retrieve the appropriate  item to display from data source.
            BluetoothDevice bluetoothDevice = rows.get(position);

            // Get data for display.
            deviceNameTextView.setText(bluetoothDevice.getName());
            deviceAddressTextView.setText(bluetoothDevice.getAddress());
            return convertView;
        }
    }
}
