
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
