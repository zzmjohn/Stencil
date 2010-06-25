package stencil.unittests.module;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;

import junit.framework.TestCase;
import stencil.module.MethodInstanceException;
import stencil.module.ModuleCache;
import stencil.module.operator.StencilOperator;
import stencil.parser.ParserConstants;

public class TestModuleCache extends TestCase {
	public static final String PROPERTIES_FILE ="./TestData/Stencil.properties";

	public void setUp() {ModuleCache.clear();}
	public static void initCache() throws Exception {
		Properties props = new Properties();
		props.loadFromXML(new FileInputStream(PROPERTIES_FILE));		
		ModuleCache.registerModules(props);
	}
	
	public void testInit() throws Exception {
		assertEquals("Module cache not empty when expected.", 0, ModuleCache.registeredModules().size());
		
		initCache();
		
		BufferedReader r = new BufferedReader(new FileReader(PROPERTIES_FILE));
		int expected=0;
		while (r.ready()) {
			if (r.readLine().contains(".yml")) {expected++;}
		}
		
		assertFalse("Module cache empty when not expected.", 0==ModuleCache.registeredModules().size());
		assertEquals("Current modules: " + ModuleCache.registeredModules().keySet(), expected, ModuleCache.registeredModules().size());
	}

	public void testClear() throws Exception {
		initCache();
		
		ModuleCache m = new ModuleCache();
		StencilOperator l= null;

		try {l=m.instance("NoMethod", ParserConstants.BASIC_SPECIALIZER);}
		catch (MethodInstanceException e) {/*Exception expected, tested below.*/}
		assertNull("Method found when not expected.",l);

		try {m.instance("Concatenate", ParserConstants.BASIC_SPECIALIZER);}
		catch (MethodInstanceException e) {fail("Method not found when expected.");}
		catch (Exception e) {fail("Unexpected error looking for method.");}
	}
}