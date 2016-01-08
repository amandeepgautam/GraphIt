package edu.unm.twin_cities.graphit.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.processor.model.PlotData;

public class TestActivity extends DrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
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

    public void startPairedDeviceSelectionActivity(View view) {
        Intent intent = new Intent(this, PairedDeviceSelection.class);
        startActivity(intent);
    }

    public void startBluethoothScannerActivity(View view) {
        Intent intent = new Intent(this, BluetoothScanner.class);
        startActivity(intent);
    }

    public void startPlotActivity(View view) {
        Intent intent = new Intent(this, PlotActivity.class);
        startActivity(intent);
    }
}
