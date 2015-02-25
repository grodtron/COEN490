package ca.dreamteam.logrunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;

import ca.dreamteam.logrunner.shoetag.AccelerationReading;
import ca.dreamteam.logrunner.shoetag.ForceReading;
import ca.dreamteam.logrunner.shoetag.ShoetagListener;

public class GraphTesting implements ShoetagListener {
    private XYMultipleSeriesRenderer renderers = new XYMultipleSeriesRenderer();
    private XYMultipleSeriesDataset  dataset   = new XYMultipleSeriesDataset();
    private Map<ForceReading.Location, XYSeries> seriesByLocation
            = new HashMap<ForceReading.Location, XYSeries>(5);

    private GraphicalView graphicalView;
    private int n;

    public GraphicalView getGraphView(Context context) {

        float hue = 0;
        int n = 0;
        for(ForceReading.Location location: ForceReading.Location.values()){
            XYSeries series           = new XYSeries(location.name());
            XYSeriesRenderer renderer = new XYSeriesRenderer();
/*
            for(int i = 0; i < 10; ++i){
                series.add(i, i + n);
            }
            ++n;
*/
            // Set up the renderer color and point style
            renderer.setColor(Color.HSVToColor(255, new float[]{hue, 1.0f, 1.0f}));
            hue += 15;
            renderer.setFillPoints(false);

            // Store the series object so we can retrieve it after
            seriesByLocation.put(location, series);

            renderers.addSeriesRenderer(renderer);
            dataset.addSeries(series);
        }

        setChartSettings(renderers, "Ground Strike Force", "Time",
                "Force", 0.5, 12.5, 0, 2000, Color.LTGRAY, Color.LTGRAY);
        renderers.setXLabels(12);
        renderers.setYLabels(10);
        renderers.setShowGrid(true);
        renderers.setXLabelsAlign(Align.RIGHT);
        renderers.setYLabelsAlign(Align.RIGHT);
        renderers.setZoomButtonsVisible(false);
        renderers.setAxisTitleTextSize(16);
        renderers.setChartTitleTextSize(20);
        renderers.setLabelsTextSize(15);
        renderers.setLegendTextSize(15);
        renderers.setPointSize(5f);
        renderers.setMargins(new int[] { 20, 30, 15, 20 });

        graphicalView = ChartFactory.getLineChartView(
                context, dataset, renderers);

        return graphicalView;
    }



    private void setChartSettings(XYMultipleSeriesRenderer renderer,
                                  String title, String xTitle, String yTitle, double xMin,
                                  double xMax, double yMin, double yMax, int axesColor,
                                  int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
    }

    @Override
    public void updateForce(ForceReading reading) {
        for(ForceReading.Location location : ForceReading.Location.values()){
            seriesByLocation.get(location).add(n, reading.getReading(location));
            if (n > 40) seriesByLocation.get(location).remove(0);
        }

        renderers.setXAxisMin(n - 40);
        renderers.setXAxisMax(n);
        graphicalView.repaint();
        ++n;
    }

    @Override
    public void updateAcceleration(AccelerationReading reading) {

    }
}