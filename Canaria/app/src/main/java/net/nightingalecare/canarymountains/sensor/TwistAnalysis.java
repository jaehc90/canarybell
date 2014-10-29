package net.nightingalecare.canarymountains.sensor;

import java.util.ArrayList;

public class TwistAnalysis
{
	static int sizeOfData;
	
 	/*
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analysis);
        
        if(PedoSensorActivity.xData.size() == PedoSensorActivity.yData.size() && PedoSensorActivity.yData.size() == PedoSensorActivity.zData.size())
        	sizeOfData = PedoSensorActivity.xData.size();

        TextView text = (TextView)findViewById(R.id.textview);
        
        if(checkDrugTwist(PedoSensorActivity.xData, PedoSensorActivity.yData, PedoSensorActivity.zData))
        {
        	Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE); 
            vibrator.vibrate(100);
            text.setText("������ �����̽��ϴ�!"); 
            //
        }
        else
        	text.setText("�ٸ����� �ϼ̽��ϴ�!"); 
    }
 
	public void onClickMethod(View v)
	{	

	    this.finish();
	}
	*/
    public TwistAnalysis(){

    }

	public static boolean checkDrugTwist(ArrayList<Double> xData, ArrayList<Double> yData, ArrayList<Double> zData)
	{
        boolean condition1;
        boolean condition2;
				boolean condition3;
				boolean condition4;
		
		int numForCond1 = 0;
		int xNumForCond2 = 0;
		int yNumForCond2 = 0;
		double[] stds = new double[3];

        if(PedoSensorActivity.xData.size() == PedoSensorActivity.yData.size() && PedoSensorActivity.yData.size() == PedoSensorActivity.zData.size()) {
            sizeOfData = PedoSensorActivity.xData.size();
        } else {
            return false;
        }

		for(int index = 0; index < sizeOfData; index++)
		{
			if(zData.get(index) < -5)
				numForCond1 += 1;
			if(xData.get(index) < -5)
				xNumForCond2 += 1;
			if(yData.get(index) < -5)
				yNumForCond2 += 1;
		}
		
		stds[0] = standardDeviation(xData, 1);
		stds[1] = standardDeviation(yData, 1);
		stds[2] = standardDeviation(zData, 1);
		
		if(numForCond1 > 5)
			condition1 = true;
		else
			condition1 = false;
		
		if(xNumForCond2 < 5 && yNumForCond2 < 5)
			condition2 = true;
		else
			condition2 = false;
		
		if(stds[2] > 1 && stds[2] < 3)
			condition3 = true;
		else
			condition3 = false;
		if(stds[0] < 1 && stds[1] < 1)
			condition4 = true;
		else
			condition4 = false;

		if(condition1 && condition2 && condition3 && condition4)
			return true;
		else
			return false;
	}
	 
	public static double mean(ArrayList<Double> array) {

	    double sum = 0.0;
	
	    for (int i = 0; i < sizeOfData; i++)
	      sum += array.get(i);
	
	    return sum / array.size();
	}

	public static double standardDeviation(ArrayList<Double> array, int option) 
	{
	    if (array.size() < 2) 
	    	return Double.NaN;
	
	    double sum = 0.0;
	    double sd = 0.0;
	    double diff;
	    double meanValue = mean(array);
	
	    for (int i = 0; i < sizeOfData; i++) 
	    {
	      diff = array.get(i) - meanValue;
	      sum += diff * diff;
	    }
	    sd = Math.sqrt(sum / (sizeOfData - option));
	
	    return sd;
	} 
}