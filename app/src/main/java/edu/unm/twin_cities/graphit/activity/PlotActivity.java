package edu.unm.twin_cities.graphit.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.processor.model.PlotData;
import edu.unm.twin_cities.graphit.rest.DataProvider;
import edu.unm.twin_cities.graphit.rest.SensorDataProvider;

public class PlotActivity extends AppCompatActivity {

    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        lineChart = (LineChart) findViewById(R.id.chart);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setBackgroundColor(Color.WHITE);
        //lineChart.setVisibleXRange(1);
        //lineChart.moveViewToX();

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(Color.BLACK);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        //DataProvider dataProvider = new RandomDataProvider(this);
        DataProvider dataProvider = new SensorDataProvider(this);
        PlotData plotData = dataProvider.getData();

        ArrayList<LineDataSet> dataSet = Lists.newArrayList();
        for (Map.Entry<String, List<Entry>> elem : plotData.getData().entrySet()) {
            //generate a random color. Hope that it would not clash,if things are truely random.
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

            LineDataSet lineDataSet = new LineDataSet(elem.getValue(), elem.getKey());
            lineDataSet.setColor(color);
            lineDataSet.setCircleColor(color);
            lineDataSet.setCircleSize(2f);

            dataSet.add(lineDataSet);
        }

        LineData data = new LineData(plotData.getXValues(), dataSet);
        lineChart.setData(data);
        lineChart.setDescription("Sensor Data");
        lineChart.animateXY(1000, 1000);
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

    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xValues = Lists.newArrayList();

        return xValues;
    }
}
