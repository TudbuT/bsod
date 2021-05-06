package untitled;

import de.tudbut.io.StreamReader;
import tudbut.parsing.TCN;

import java.io.IOException;

public class Config {

    public static TCN config;
    
    static {
        try {
            config = TCN.read(new StreamReader(ClassLoader.getSystemResourceAsStream("config.txt")).readAllAsString());
        }
        catch (TCN.TCNException | IOException e) {
            e.printStackTrace();
        }
    }
}
