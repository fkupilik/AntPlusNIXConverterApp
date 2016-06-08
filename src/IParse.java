import java.util.ArrayList;

/**
 * Interface defines methods, which ANT+ profiles must implement.
 * @author Filip Kupil�k, Petr Tobi�, V�clav Janoch
 *
 */
public interface IParse {

	void parseLine(String line);
	
	void parseList(ArrayList<String> data);
	
}
