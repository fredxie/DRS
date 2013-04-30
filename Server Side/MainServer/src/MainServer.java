import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.swing.JOptionPane;

//using accelerometer and gyroscope to do the analysis
public class MainServer {
    
	final int SIZE = 8000; //only read the top 12000 lines of the gyrofile
	
	public Queue<Integer> ReadDataFile(FileReader gyrofile){ // process gyrofile
		
		int counter = 0;
		Queue<Integer> TimeLine = new LinkedList<Integer>(); 
		int StartTurnTime = 0;
		int EndTurnTime   = 0;
		double benchvalue  = 0;
		
		try{
			BufferedReader reader = new BufferedReader(gyrofile);	
			String line = null;
			while((line=reader.readLine())!=null){			  
				  counter++;				  
				  String[] element = line.split(",");
				  
				  if(element.length != 5){ //if the data is incomplete then drop the data.
					  continue;
				  }				  
				  if(counter>SIZE){ 
					  break;  
				  }			  
				  float  Zvalue = Float.parseFloat(element[4]);
				  String CurrentTimeStr = element[1].substring(6);
				  int    CurrentTime    = Integer.parseInt(CurrentTimeStr);
				  
				  if(Zvalue > 1.0 || Zvalue < -1.0){
					  //drop the value when gyrometer > 1.0 and < -1.0 
					  continue;
				  }
				  
				  //threshold Zvalue is 0.22 or -0.22, and assume when make a turn all the value will be above -0.18 and 0.18.
				  else if(StartTurnTime == 0 && EndTurnTime == 0 && (Zvalue < -0.22 || Zvalue > 0.22)){ 					  
					  
					  StartTurnTime = CurrentTime;
					  if(Zvalue < -0.22){
						  benchvalue = -0.22;
					  }else{
						  benchvalue = 0.22;
					  }	  					  
				  }
				  //finish the turn.
				  else if(StartTurnTime !=0 && EndTurnTime == 0 && (Zvalue>-0.18) && (benchvalue == -0.22)){ 
					  EndTurnTime = CurrentTime;
				  }		
				  
				  else if(StartTurnTime !=0 && EndTurnTime == 0 && (Zvalue<0.18) && (benchvalue == 0.22)){
					  EndTurnTime = CurrentTime;
				  }
				  
				  else if(StartTurnTime !=0 && EndTurnTime == 0){
					  continue; // in a turn.
				  }
				  //assume that a turn's period will not less than 2 second and will not above 8 second.
				  //otherwise drop the data
				  else if(StartTurnTime !=0 && EndTurnTime !=0){
					   if(EndTurnTime - StartTurnTime > 9000 || EndTurnTime - StartTurnTime < 2000){
						   StartTurnTime = 0;
						   EndTurnTime   = 0;
						   benchvalue    = 0;
					   }
					   
					   else{
						   TimeLine.add(StartTurnTime);
						   TimeLine.add(EndTurnTime);
						   StartTurnTime = 0;
						   EndTurnTime   = 0;
						   benchvalue    = 0;
					   }
				  }	
			}
		}catch(Exception e){
            e.printStackTrace();
        }			
		//System.out.println(TimeLine.size());		
		return TimeLine;
	}
	
	public ArrayList<Queue<Integer>> ValidTimeLine(ArrayList<Queue<Integer>> list){
		ArrayList<Queue<Integer>> newlist = new ArrayList<Queue<Integer>>();
		Queue<Integer> newtimeline1 = new LinkedList<Integer>(); //new timeline1
		Queue<Integer> newtimeline2 = new LinkedList<Integer>(); //new timeline2
		
		Queue<Integer> timeline1 = list.get(0);
		Queue<Integer> timeline2 = list.get(1);
	    	
		while(timeline1.isEmpty()!= true && timeline2.isEmpty()!= true){
			int start1 = timeline1.poll();
			int end1   = timeline1.poll();
			int start2 = timeline2.poll();
			int end2   = timeline2.poll();
			
			//System.out.println(end1);
			//System.out.println(end2);
			
			// when the time difference bigger than 1000 drop
			int startDifference = Math.abs(start2 - start1); 
			int endDifference   = Math.abs(end2 - end1);
			
			if(startDifference < 1500 && endDifference < 1500){ //2s is also OK
				newtimeline1.add(start1);
				newtimeline1.add(end1);
				newtimeline2.add(start2);
				newtimeline2.add(end2);
			}	
		}
		
		newlist.add(newtimeline1);
		newlist.add(newtimeline2);		
		return newlist;
	}
	
	
	int starttime = 0;
	int endtime = 0;
	
	//deal with the accelerometer file based on the timeline.  (run the whole file)
	public ArrayList<Float> ReadAccFile(FileReader filename, Queue<Integer> timeline){
				
		starttime = timeline.poll();
		endtime   = timeline.poll();
			
		ArrayList<Float> store = new ArrayList<Float>();
		ArrayList<Float> resultlist = new ArrayList<Float>();
		
		try{
			BufferedReader reader = new BufferedReader(filename);	
			String line = null;
			
			while((line=reader.readLine())!=null){	
				String[] element = line.split(",");
				if(element.length != 5){
				   continue;
				}		
				float  Xvalue = Float.parseFloat(element[2]);
				String CurrentTimeStr = element[1].substring(6);
				int    CurrentTime    = Integer.parseInt(CurrentTimeStr);
				
				if(CurrentTime > starttime && CurrentTime < endtime){
				      store.add(Xvalue);
				}
				
				else if(CurrentTime > endtime){	
					
					  if(store.isEmpty()!=true){
						  float result = Average(store); 
						  //deal with the store's data get average.
						  resultlist.add(result);
					  }
					  
					  if(timeline.isEmpty()!=true){
					     starttime = timeline.poll();
					     endtime   = timeline.poll();	
					  }else{
						 break; // break the file scanning if there is no time-line data.
					  }
				}	

			}
		}catch(Exception e){
            e.printStackTrace();
        }	
		
		return resultlist;
	}
	
	// process the value we get from specific interval.
	public float Average(ArrayList<Float> store){
		 float total = 0;
		 int   size  = 0;
		 ArrayList<Float> positive = new ArrayList<Float>();
		 ArrayList<Float> negative = new ArrayList<Float>();
		 
		 for(int i=0; i<store.size(); i++){
			 float value = store.get(i);
			 if(value<=0){
		       negative.add(value);		
			 }else{
			   positive.add(value);	 
			 }
		 }
		 
		 if(positive.size() > negative.size()){
			 for(int i=0; i<positive.size(); i++){
				 total+= positive.get(i);
			 }
			  size = positive.size();
		 }else{
			 for(int i=0; i<negative.size(); i++){
				 total+= negative.get(i);
			 }
			  size = negative.size();
		 }
		 
		 float result = total/size;
		 //print the value.
		 System.out.println(result);
		 
		 return result;
	}
	
	String result1f = "";
	String result2f = "";
	
	public void FindResult(ArrayList<Float> file1, ArrayList<Float> file2){
		
		float L1 = 0; // negative left turn
	    float L2 = 0; 
	    float R1 = 0; // positive right turn.
	    float R2 = 0;
    	    
		for(int i=0; i<file1.size(); i++){
			if(file1.get(i)<0.3 && L1 == 0){ // if value < (0.0) 0.3 will be treated as left turn
				
				L1 = file1.get(i);
				L2 = file2.get(i);		
				L1 = Math.abs(L1);
				L2 = Math.abs(L2);
			}			
			else if(file1.get(i)>0.3 && R1 == 0){ // if value > (0.0) 0.3 will be treated as right turn,
				R1 = file1.get(i);
				R2 = file2.get(i);
			}	
			//find one left turn and one right turn.
			else if(L1!=0 && R1!=0){ 
				break;
			}
		}
		
		float result1 = L1/R1;
 		float result2 = L2/R2;
 		 		
 		if(result1 > result2){
 			result1f = "passenger";
 			result2f = "driver";
 		}else if (result1 < result2){
 			result1f = "driver";
 			result2f = "passenger";
 		}else{
 			System.out.println("Data Invalid, need a left turn or a right turn");
 		}
		
 		//result1f = "passenger";
 		//result2f = "driver";
 		
		System.out.println(result1f);
		System.out.println(result2f);
	}
	
	public void ExportFile(){ // export Processed data into a new file, and save in the mobile phone.  
	    String CreatNewFile = new String("G:/Result/result.csv");	        
        try{
        	 FileWriter Writer = new FileWriter(CreatNewFile); 
	         Writer.append("File1");
	         Writer.append(",");
	         Writer.append(result1f);
	         Writer.append(",");
	         Writer.append("File2");
	         Writer.append(",");
	         Writer.append(result2f);
	         Writer.append("\n"); 
        	 Writer.flush();
        	 Writer.close();
        }catch (IOException e) {
     	    e.printStackTrace();
        }
        
        String CreatNewFile2 = new String("H:/Result/result.csv");	        
        try{
        	 FileWriter Writer = new FileWriter(CreatNewFile2); 
	         Writer.append("File1");
	         Writer.append(",");
	         Writer.append(result1f);
	         Writer.append(",");
	         Writer.append("File2");
	         Writer.append(",");
	         Writer.append(result2f);
	         Writer.append("\n"); 
        	 Writer.flush();
        	 Writer.close();
        }catch (IOException e) {
     	    e.printStackTrace();
        }        
    }
	
	public static void main(String[] args) {
		MainServer server = new MainServer();
		
		 FileReader gyrofile1=null;
    	 try {
			gyrofile1 = new FileReader("E:/Android/Data/dataset6/DriverGYROdata.csv"); 
		 }catch (FileNotFoundException e) {
			e.printStackTrace();
		 }
    	 
    	 FileReader gyrofile2=null;
    	 try {
			gyrofile2 = new FileReader("E:/Android/Data/dataset6/PassengerGYROdata.csv");
		 }catch (FileNotFoundException e) {
			e.printStackTrace();
		 }
    	
    	 // store the timeline generated by the specific gyrometerfile.
    	 Queue<Integer> file1timeline = server.ReadDataFile(gyrofile1);
  
    	 Queue<Integer> file2timeline = server.ReadDataFile(gyrofile2);
    	 
    	 ArrayList<Queue<Integer>> list = new ArrayList<Queue<Integer>>();
    	 list.add(file1timeline);
    	 list.add(file2timeline);
    	 
    	 ArrayList<Queue<Integer>> newlist = server.ValidTimeLine(list);
    	 Queue<Integer> newfile1timeline = newlist.get(0); 
    	 Queue<Integer> newfile2timeline = newlist.get(1);
		 
    	 
    	 FileReader accfile1=null;
    	 try {
			accfile1 = new FileReader("E:/Android/Data/dataset6/DriverACCdata.csv"); // the faster more sensitive one
			//file1 = new FileReader("H:/DriverACCdata.csv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	 FileReader accfile2=null;
    	 try {
			accfile2 = new FileReader("E:/Android/Data/dataset6/PassengerACCdata.csv");
			//file2 = new FileReader("G:/PassengerACCdata.csv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	 
    	// make sure the number of turns is bigger than 2. 
    	 if(newfile1timeline.size()>=2 && newfile2timeline.size()>=2){     	 
    	    ArrayList<Float> resultfile1 = server.ReadAccFile(accfile1, newfile1timeline); 
    	    System.out.println();
    	    ArrayList<Float> resultfile2 = server.ReadAccFile(accfile2, newfile2timeline);
    	    server.FindResult(resultfile1, resultfile2); 
    	    //successfully finish analysis
    	    JOptionPane.showMessageDialog(null,"Analysis Finished","Notice",JOptionPane.INFORMATION_MESSAGE);
    	    server.ExportFile();
    	}else{
    		//analysis failed.
    		System.out.println("Less than two turns, continue data gathering");
    	}
    	
    	
	}

}
