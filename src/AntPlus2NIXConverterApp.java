
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
		
		int[] computedHeartRateArray = convertToArrayInt(computedHeartRate);
		int[] heartBeatCountArray = convertToArrayInt(heartBeatCount);
		double[] heartBeatEventTimeArray = convertToArrayDouble(heartBeatEventTime);
		
		OdMLData metaData = new OdMLData("", "", new String[1], deviceNumber, 0, 0, manufacturerID, new int[1], 0);
		AntHeartRate heartRate = new AntHeartRate(heartBeatCountArray, computedHeartRateArray, heartBeatEventTimeArray,	metaData);
		File nixFile = File.open("nixFileExample.h5", FileMode.Overwrite);
		heartRate.fillNixFile(nixFile);
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
