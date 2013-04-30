package com.example.driverrecognition;

import java.io.BufferedReader;
import java.io.FileReader;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.content.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
	
	String result ="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button b1 = (Button)findViewById(R.id.button1);
		b1.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, SecondActivity.class);
				startActivity(intent);
				MainActivity.this.finish();
			}
		});
	
		Button b2 = (Button)findViewById(R.id.button2);
		b2.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				try {
		    		BufferedReader reader = new BufferedReader(new FileReader("/sdcard/Result/result.csv"));
		            String firstline = reader.readLine();
		            String[] Item = firstline.split(",");
		            result = Item[1]; // get the result from the result excel file 
		                              // item[1] Driverfile, file1, item[3] Passengerfile, file2.
		            
		    	} catch (Exception e) {
		            e.printStackTrace();
		        }
				
				if(result.equals("driver")){
				   Intent intent = new Intent();
				   intent.setClass(MainActivity.this, DriverActivity.class);
				   startActivity(intent);
				   MainActivity.this.finish();
				}
				else if(result.equals("passenger")){
				   Intent intent2 = new Intent();
				   intent2.setClass(MainActivity.this, PassengerActivity.class);
				   startActivity(intent2);
				   MainActivity.this.finish();	
				}
			}
		});	
	
	}

}
