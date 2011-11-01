package stencil;

import java.util.Properties;
import java.net.URL;


/**Methods to configure the Stencil system based
 * upon properties files specified on the system.
 * The loadProperties method should be invoked at least
 * once per application instance before using the Stencil library.
 *
 * (Multiple invocations are allowed, but results are cumulative...
 * there is no guarantee that you will get a 'clean' environment).
 *
 */
public class Configure {
	private static final String DEFAULT_STENCIL_CONFIGURATION_FILE = "Stencil.properties";
	public static String stencilConfig = DEFAULT_STENCIL_CONFIGURATION_FILE;

	public static final String THREAD_POOL_SIZE_KEY = "threadPoolSize";
	public static int threadPoolSize = 2;

	public static final String QUEUED_LOADER_SIZE_KEY = "queuedLoader_size";
	public static final String QUEUED_LOADER_THREAD_KEY = "queuedLoader_thread";
	public static final String QUEUED_LOADER_DELAY_KEY = "queuedLoader_delay";
	
	private Configure() {/*Utility, non-instantiable class.*/}

	public static void loadProperties(Properties props) {
		//Setup database driver map
		try {stencil.util.streams.sql.DriverManager.addDrivers(props);}
		catch (Exception e) {
			System.err.println("Error loading database drivers.");
			e.printStackTrace();
		}

		stencil.module.ModuleCache.registerModules(props);
		stencil.types.Converter.registerWrappers(props);
		
		threadPoolSize = Integer.parseInt(props.getProperty(THREAD_POOL_SIZE_KEY,"-1")); 
		if (threadPoolSize <1) {
			threadPoolSize = (int) (Runtime.getRuntime().availableProcessors()/2f)+1;
		}
		
		
		if (props.containsKey(QUEUED_LOADER_SIZE_KEY)) {
			stencil.util.streams.QueuedStream.DEFAULT_QUEUE_SIZE=Integer.parseInt(props.getProperty(QUEUED_LOADER_SIZE_KEY));
		}
		
		if (props.containsKey(QUEUED_LOADER_THREAD_KEY)) {
			stencil.util.streams.QueuedStream.THREAD=Boolean.parseBoolean(props.getProperty(QUEUED_LOADER_THREAD_KEY));
		}
		
		if (props.containsKey(QUEUED_LOADER_DELAY_KEY)) {
			stencil.util.streams.QueuedStream.DELAY=Integer.parseInt(props.getProperty(QUEUED_LOADER_DELAY_KEY));
		}		
	}

	
	/**Property loading, independent of a host application.  Used extensively in testing.**/
	public static void loadProperties(String... urls) throws Exception {
		Properties p = new Properties();
		URL base;
		
		try {base = new URL("file://" + System.getProperty("user.dir")+"/");}
		catch (Exception e) {throw new Error("Error initailizing context.");}
		
		for (String url:urls) {p.loadFromXML(new URL(base, url).openStream());}

		loadProperties(p);
	}
}
