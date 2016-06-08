<<<<<<< HEAD

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;



/**
 * Class, which starts the subprocess and reads data from the sensor.
 * Then give the loaded data to the relevant ANT+ profile choosen by the user.
 * @author Filip Kupilík, Petr Tobiáš, Václav Janoch
 *
 */
public class AntPlus2NIXConverterApp {
	
	/**
	 * Read the settings needed for start reading data from sensor.
	 * @param subProcessInputReader Input reader from the subprocess
	 * @param consoleReader Console reader
	 * @param subProcessOutputWriter Output reader into the subprocess
	 * @return last line of the header, which says to us, if the initialisation was successful or not
	 */
	private String getSettings(BufferedReader subProcessInputReader, BufferedReader consoleReader, BufferedWriter subProcessOutputWriter){
		try {
			String line = subProcessInputReader.readLine();
			while (line.contains("successful") != true && line.startsWith("Failed") != true) {
				System.out.println(line);
				subProcessOutputWriter.write(consoleReader.readLine() + "\n");
				subProcessOutputWriter.flush();
				System.out.println(subProcessInputReader.readLine());
				line = subProcessInputReader.readLine();
			}
				System.out.println(line);
				return line;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Starts the subprocess and give loaded data to relevant ANT+ profile
	 * @param fileName name of the program
	 * @param profile ANT+ profile from the console
	 */
	private void initProcess(String fileName, String profile){
		try {
			Process p = Runtime.getRuntime().exec(fileName);
			BufferedWriter subProcessOutputWriter = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			BufferedReader subProcessInputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
			
			String line = subProcessInputReader.readLine();
			System.out.println(line);
			while(line.contains("USB") != true){
				line = subProcessInputReader.readLine();
				System.out.println(line);
			}
			subProcessOutputWriter.write(consoleReader.readLine() + "\n");				
			subProcessOutputWriter.flush();

			System.out.println(subProcessInputReader.readLine());
			String error = this.getSettings(subProcessInputReader, consoleReader, subProcessOutputWriter);

			if (error.startsWith("Failed")) {
				System.out.println(subProcessInputReader.readLine());
				subProcessOutputWriter.write(consoleReader.readLine() + "\n");
				subProcessOutputWriter.flush();
				System.out.println("Application finished");
				System.exit(0);
			}
			System.out.println("Type any word or press enter to end collecting data.");
			ArrayList<String> data = readData(subProcessInputReader, consoleReader, subProcessOutputWriter);
			chooseProfile(data, profile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("File does not exist!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Recognize the ANT+ profile
	 * @param data loaded data from the sensor
	 * @param profile ANT+ profile from console
	 */
	private void chooseProfile(ArrayList<String> data, String profile){
		switch (profile) {
		case "HR":
			HeartRate hr = new HeartRate();
			hr.parseList(data);	
			break;
		default:
			System.out.println("Unknown profile!");
			break;
		}
	}
	
	/**
	 * Adds lines from console to the list.
	 * @param subProcessInputReader Input reader from the subprocess
	 * @param consoleReader Console reader
	 * @param subProcessOutputWriter Output reader into the subprocess
	 * @return list with loaded data from sensor
	 */
	private ArrayList<String> readData(BufferedReader subProcessInputReader, BufferedReader consoleReader, BufferedWriter subProcessOutputWriter){
		ArrayList<String> data = new ArrayList<>();
		try {
			while (true) {
				if (consoleReader.ready()) {
					subProcessOutputWriter.write("Q\n");
					subProcessOutputWriter.flush();
					consoleReader.readLine();
					System.out.println("Reading finished");
					break;
				}
				String line = subProcessInputReader.readLine();
				System.out.println(line);
				data.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Type the name of demo without .exe ending");
		String demo = consoleReader.readLine();
		System.out.println("Type the name of ANT+ profile. For example: \n HR \n for Heart Rate Monitor");
		String profile = consoleReader.readLine();
		AntPlus2NIXConverterApp app = new AntPlus2NIXConverterApp();
		app.initProcess(demo + ".exe", profile);
	}
		

}
=======

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.g_node.nix.File;
import org.g_node.nix.FileMode;

import cz.zcu.AntPlus2NIXConverter.Data.OdMLData;
import cz.zcu.AntPlus2NIXConverter.Profiles.AntHeartRate;




public class AntPlus2NIXConverterApp {
		
	private ArrayList<Integer> computedHeartRate, heartBeatCount;
	private ArrayList<Double> heartBeatEventTime;
	private int deviceNumber;
	int index = 0;

	private void parser(String line) {

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
	
	private void getSettings(BufferedReader subProcessInputReader, BufferedReader consoleReader, BufferedWriter buffWriter){
		try {
			for (int i = 0; i < 2; i++) {
				System.out.println(subProcessInputReader.readLine());
			}
			buffWriter.write(consoleReader.readLine() + "\n");
			buffWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int getManufacturerID(String line){
		String[] split1 = line.split(" : ");
		String[] split2 = split1[1].split(" , ");
		return Integer.parseInt(split2[0]);
	}
	
	private void initProcess(String fileName){
		try {
			Process p = Runtime.getRuntime().exec(fileName);
			BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			BufferedReader subProcessInputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
			
			for(int i = 0; i < 4; i++){
				System.out.println(subProcessInputReader.readLine());
			}
			buffWriter.write(consoleReader.readLine() + "\n");
			buffWriter.flush();
			for(int i = 0; i < 5; i++){
				this.getSettings(subProcessInputReader, consoleReader, buffWriter);
			}
			
			String error = "";
			for(int i = 0; i < 2; i++){
				error = subProcessInputReader.readLine();
				if(error.startsWith("Failed")){
					System.out.println(error);
					System.out.println(subProcessInputReader.readLine());
					buffWriter.write(consoleReader.readLine() + "\n");
					buffWriter.flush();
					System.out.println("Application finished");
					System.exit(0);
				}else{
					System.out.println(error);
				}
			}
			System.out.println("Type any word to end collecting data.");
			readData(subProcessInputReader, consoleReader, buffWriter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("File does not exist!");
			e.printStackTrace();
		}
	}
	
	public void readData(BufferedReader subProcessInputReader, BufferedReader consoleReader, BufferedWriter buffWriter) throws IOException{
		computedHeartRate = new ArrayList<>();
		heartBeatCount = new ArrayList<>();
		heartBeatEventTime = new ArrayList<>();
		
		int manufacturerID = 0;
		
		while (true) {
			if (consoleReader.ready()) {
				buffWriter.write("Q\n");
				buffWriter.flush();
				consoleReader.readLine();
				System.out.println("Reading finished");
				break;
			}
			String line = subProcessInputReader.readLine();
			if (line.startsWith("Chan ID")) {
				System.out.println(line);
				this.parser(line);
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
			File nixFile = File.open(consoleReader.readLine() + ".h5", FileMode.Overwrite);
			heartRate.fillNixFile(nixFile);
		}
	}

    public double[] convertToArrayDouble(ArrayList<Double> list){
        double[] array = new double[list.size()];
        for(int i = 0; i < array.length; i++){
            array[i] = list.get(i);
        }

        return array;
    }
    
    public int[] convertToArrayInt(ArrayList<Integer> list){
        int[] array = new int[list.size()];
        for(int i = 0; i < array.length; i++){
            array[i] = list.get(i);
        }

        return array;
    }

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		AntPlus2NIXConverterApp app = new AntPlus2NIXConverterApp();
		app.initProcess("DEMO_HR_RECEIVER.exe");
	}
		

}
>>>>>>> refs/remotes/origin/master
