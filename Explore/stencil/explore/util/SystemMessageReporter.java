package stencil.explore.util;

import java.io.PrintStream;

/**Basic wrapper of System.out and System.err*/
public class SystemMessageReporter implements MessageReporter {
	/**Echo a message to System.err.*/
	@Override
	public void addError(String message, Object...args) {
		if (message == null) {return;}
		
		System.err.println(String.format(message, args));}
	
	/**Echo a message to System.out*/
	@Override
	public void addMessage(String message, Object...args) {
		if (message ==null) {return;}
		System.out.println(String.format(message, args));
	}
	
	@Override
	public PrintStream errorAsStream() {return System.out;}
	
	/**Does nothing.*/
	@Override
	public void clear() {/*Provided for interface compatibility.*/}
}
