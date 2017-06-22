package go.chilan.mapmatch;

import com.graphhopper.chilango.network.Constants;
import com.graphhopper.chilango.network.MapMatchingClient;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {	
    	while(true){
    		try{
    			MapMatch.init();
        Server server=new Server(Constants.PORT_MAP_MATCH);
    		}catch(Exception e){
    			System.out.println("error "+e.getMessage());
    		}
    	}
    }
}
