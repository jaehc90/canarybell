package net.nightingalecare.canarymountains.utilities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Point;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class LineGraphView {

	//TimeSeries will hold the data in x,y format for single chart  
	private TimeSeries mSeries = new TimeSeries("Pedometer");
	//XYSeriesRenderer is used to set the properties like chart color, style of each point, etc. of single chart
	private XYSeriesRenderer mRenderer = new XYSeriesRenderer();
	//XYMultipleSeriesDataset will contain all the TimeSeries 
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	//XYMultipleSeriesRenderer will contain all XYSeriesRenderer and it can be used to set the properties of whole Graph
	private XYMultipleSeriesRenderer mMultiRenderer = new XYMultipleSeriesRenderer();

	private static LineGraphView mInstance = null;

	/**
	 * singleton implementation of LineGraphView class
	 */
	public static synchronized LineGraphView getLineGraphView() {
		if (mInstance == null) {
			mInstance = new LineGraphView();
		}
		return mInstance;
	}

	public LineGraphView() {
		//add single line chart mSeries   
		mDataset.addSeries(mSeries);
		//set line chart color to Black
		mRenderer.setColor(Color.BLACK);
		//set line chart style to square points
		mRenderer.setPointStyle(PointStyle.SQUARE);
		mRenderer.setFillPoints(true);

		final XYMultipleSeriesRenderer renderer = mMultiRenderer;
		//set whole graph background color to transparent color  
		renderer.setBackgroundColor(Color.TRANSPARENT);
		renderer.setMargins(new int[] { 50, 65, 40, 5 }); // top, left, bottom, right
		renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));		
		renderer.setAxesColor(Color.BLACK);
		renderer.setAxisTitleTextSize(24);
		renderer.setShowGrid(true);
		renderer.setGridColor(Color.LTGRAY);
		renderer.setLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.DKGRAY);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setYLabelsPadding(4.0f);
		renderer.setXLabelsColor(Color.DKGRAY);
		renderer.setLabelsTextSize(20);
		renderer.setLegendTextSize(20);
		//Disable zoom
		renderer.setPanEnabled(false, false);
		renderer.setZoomEnabled(false, false);
		//set title to x-axis and y-axis
		renderer.setXTitle("Time");
		renderer.setYTitle("STEP");
		
		renderer.addSeriesRenderer(mRenderer);
	}

	/**
	 * return graph view to activity
	 */
	public GraphicalView getView(Context context) {
		final GraphicalView graphView = ChartFactory.getLineChartView(context, mDataset, mMultiRenderer);
		return graphView;
	}

	/**
	 * add new x,y value to chart
	 */
	public void addValue(Point p) {
		mSeries.add(p.x, p.y);
	}

	/**
	 * clear all previous values of chart
	 */
	public void clearGraph() {
		mSeries.clear();
	}
	
}
