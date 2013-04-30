package com.example.driverrecognition;

import android.view.Menu;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;



public class SecondActivity extends Activity implements SensorEventListener {
    
	String strFormat = "yyyy-MM-dd HH:mm:ss:ms";
	SimpleDateFormat format1= new SimpleDateFormat(strFormat);
	TextView time;
	
	
	private SensorManager sensorManager;	
	TextView xCoor; // declare X axis object
	TextView yCoor; // declare Y axis object
	TextView zCoor; // declare Z axis object	
	TextView Accelerometer; 
			
	TextView xCoor1; // declare X axis object
	TextView yCoor1; // declare Y axis object
	TextView zCoor1; // declare Z axis object	
	TextView Gyroscope;
	
	long starttime; // current time
	long endtime;
	//External data storage start
	private PrintWriter mCurrentFile1;
	private PrintWriter mCurrentFile2;
	String comma = new String(","); 
	
	public SecondActivity(){ 		
	    //store accelerometer data
		starttime = System.currentTimeMillis();
				
    	//String ACCnameStr = new String("/sdcard/PassengerACCdata.csv");
    	String ACCnameStr = new String("/sdcard/DriverACCdata.csv");
        File outputFile1 = new File(ACCnameStr);
        mCurrentFile1 = null;
        try{
     	    mCurrentFile1 = new PrintWriter(new FileOutputStream(outputFile1));
        }catch (FileNotFoundException e) {
     	e.printStackTrace();
        } 
        
        //store gyroscope data
        //String GYROnameStr =  new String("/sdcard/PassengerGYROdata.csv");
        String GYROnameStr =  new String("/sdcard/DriverGYROdata.csv");
        File outputFile2 = new File(GYROnameStr);
        mCurrentFile2 = null;
        try{
     	    mCurrentFile2 = new PrintWriter(new FileOutputStream(outputFile2));
        }catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
     	e.printStackTrace();
        } 
        
	}
	//data storage end
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.secondlayout);
			
		time = (TextView) findViewById(R.id.time);
				
		xCoor=(TextView)findViewById(R.id.xcoor); // create X axis object
		yCoor=(TextView)findViewById(R.id.ycoor); // create Y axis object
		zCoor=(TextView)findViewById(R.id.zcoor); // create Z axis object	
		Accelerometer = (TextView)findViewById(R.id.accelerometer); // create accelerometer statement
				
		xCoor1=(TextView)findViewById(R.id.xcoor1); // create X axis object
		yCoor1=(TextView)findViewById(R.id.ycoor1); // create Y axis object
		zCoor1=(TextView)findViewById(R.id.zcoor1); // create Z axis object	
		Gyroscope     = (TextView)findViewById(R.id.gyroscope); 
		
		
		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		//Add accelerometer listener.
		sensorManager.registerListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);	
		
		//Add gyroscope listener.
		sensorManager.registerListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

    public void onAccuracyChanged(Sensor sensor,int accuracy){		
	}
	    
	public void onSensorChanged(SensorEvent event){
		
		// check sensor type
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){					
			// assign directions			
			float x=event.values[0]; //x axis
			float y=event.values[1]; //y axis
			float z=event.values[2]; //z axis
			
			//store data into the arraylist
			float[] store = new float[3];
			store[0] = x;
			store[1] = y;
			store[2] = z;			
						
			Accelerometer.setText("Accelerometer real-time data");				
			xCoor.setText("X: "+x);
			yCoor.setText("Y: "+y);
			zCoor.setText("Z: "+z);		
			
			long currentTime1 = System.currentTimeMillis();
			Calendar c = Calendar.getInstance();
			Date d = c.getTime();
			String str="";
			str = format1.format(d);
			
			time.setText(str);
			
			//data storage start  buff.append(String.valueOf(event.timestamp));
			StringBuffer buff = new StringBuffer();
			buff.append(String.valueOf(str));
	        buff.append(comma);
			buff.append(String.valueOf(currentTime1));
	        buff.append(comma);
	        buff.append(String.valueOf(x));
	        buff.append(comma);
	        buff.append(String.valueOf(y));
	        buff.append(comma);
	        buff.append(String.valueOf(z));
	        mCurrentFile1.println(buff.toString());
	        	        
		}	
		
		
	if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE){					
			
		   float x1=event.values[0]; //x axis
		   float y1=event.values[1]; //y axis
		   float z1=event.values[2]; //z axis
						
			Gyroscope.setText("Gyroscope real-time data");				
			xCoor1.setText("X1: "+x1);
			yCoor1.setText("Y1: "+y1);
			zCoor1.setText("Z1: "+z1);	
					
			long currentTime2 = System.currentTimeMillis();
			
			Calendar c = Calendar.getInstance();
			Date d = c.getTime();
			String str="";
			str = format1.format(d);
			
			time.setText(str);		
			//data storage start
			StringBuffer buff2 = new StringBuffer(); 
			buff2.append(String.valueOf(str));
	        buff2.append(comma);
	        buff2.append(String.valueOf(currentTime2));
	        buff2.append(comma);
	        buff2.append(String.valueOf(event.values[0])); // rad/s 
	        buff2.append(comma);
	        buff2.append(String.valueOf(event.values[1]));
	        buff2.append(comma);
	        buff2.append(String.valueOf(event.values[2]));
	        mCurrentFile2.println(buff2.toString());
	        //data storage end
		}
			
		//end program.
	    /*
		    endtime = System.currentTimeMillis();
	        
	        if(endtime - starttime > 1000000){ // running 1000 s and finish the data gathering.
	        	Intent intent = new Intent();
				intent.setClass(SecondActivity.this, FinishActivity.class);
				startActivity(intent);
				SecondActivity.this.finish();
	        }
	    */   
	}		
}	


