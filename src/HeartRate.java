import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.g_node.nix.File;
import org.g_node.nix.FileMode;

import cz.zcu.AntPlus2NIXConverter.Data.OdMLData;
import cz.zcu.AntPlus2NIXConverter.Profiles.AntHeartRate;

/**
 * Class gets heart rate data from output of the heart rate demo.
 * @author Filip Kupilík, Petr Tobiáš, Václav Janoch
 *
 */

public class HeartRate implements IParse{
	
	private ArrayList<Integer> computedHeartRate, heartBeatCount;
	private ArrayList<Double> heartBeatEventTime;
	private int deviceNumber;
	int index = 0;
	
	/**
	 * Overriden method from IParse interface.
	 * Gets computed heart rate, heart beat count and time of previous heart beat 
	 * from output line of the demo.
	 * @param line output line of the demo
	 */
	@Override
	public void parseLine(String line) {
		// TODO Auto-generated method stub
		
		String[] splitComma = line.split(" , ");

		String[] splitDash = splitComma[0].split(" ");
		
		String deviceNumberStr = splitDash[1];
		deviceNumber = Integer.parseInt(deviceNumberStr.substring(3, deviceNumberStr.indexOf("/")));

		splitDash[1].substring(3, splitDash[1].length() - 1);
		int beatCount = Integer.parseInt(splitComma[1].split(": ")[1]);
		if(index == 0 || heartBeatCount.get(index-1) != beatCount){			
			computedHeartRate.add(Integer.parseInt(splitComma[0].split(": ")[2]));			
			heartBeatCount.add(beatCount);			
			heartBeatEventTime.add(Double.parseDouble(splitComma[2].split(": ")[1]));
			index++;
		}
	}

	/**
	 * Overriden method from IParse intefrace.
	 * Parses lines from the loaded data from sensor.
	 * @param data loaded data from heart rate sensor
	 */
	@Override
	public void parseList(ArrayList<String> data) {
		// TODO Auto-generated method stub
		computedHeartRate = new ArrayList<>();
		heartBeatCount = new ArrayList<>();
		heartBeatEventTime = new ArrayList<>();
		
		int manufacturerID = 0;
		
		for (String line : data) {
			
			if (line.startsWith("Chan ID")) {
				this.parseLine(line);
			}
			if(line.startsWith("Transmitter manufacturer")){
				manufacturerID = this.getManufacturerID(line);
			}
		
		}
		if (computedHeartRate.size() == 0 || heartBeatCount.size() == 0 || heartBeatEventTime.size() == 0) {
			System.out.println("No data were collected!");
		} else {
			int[] computedHeartRateArray = convertToArrayInt(computedHeartRate);
			int[] heartBeatCountArray = convertToArrayInt(heartBeatCount);
			double[] heartBeatEventTimeArray = convertToArrayDouble(heartBeatEventTime);
			
			OdMLData metaData = new OdMLData("", "", new String[1], deviceNumber, 0, 0, manufacturerID, new int[1], 0);
			AntHeartRate heartRate = new AntHeartRate(heartBeatCountArray, computedHeartRateArray,
					heartBeatEventTimeArray, metaData);
			System.out.println("Type the name of the output file (without the .h5 ending)");
			BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
			File nixFile;
			try {
				nixFile = File.open(consoleReader.readLine() + ".h5", FileMode.Overwrite);
				heartRate.fillNixFile(nixFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Converts list of doubles to array of doubles
	 * @param list list
	 * @return array 
	 */
    public double[] convertToArrayDouble(ArrayList<Double> list){
        double[] array = new double[list.size()];
        for(int i = 0; i < array.length; i++){
            array[i] = list.get(i);
        }
        return array;
    }
    
	/**
	 * Converts list of integers to array of integers
	 * @param list list
	 * @return array 
	 */
    public int[] convertToArrayInt(ArrayList<Integer> list){
        int[] array = new int[list.size()];
        for(int i = 0; i < array.length; i++){
            array[i] = list.get(i);
        }

        return array;
    }
	
    /**
     * Gets manufacturerID from output line from the demo
     * @param line output line from the demo
     * @return manufacturerID
     */
	private int getManufacturerID(String line){
		String[] split1 = line.split(" : ");
		String[] split2 = split1[1].split(" , ");
		return Integer.parseInt(split2[0]);
	}
	

}
