package stencil.operator.util;

import java.lang.reflect.*;

import stencil.tuple.Tuple;
import stencil.types.Converter;


/**Combines a method and a target object (null for static items)
 * for later invoking.  Also includes the proper logic for performing the
 * invocation from an array of values.*/

//final because it is immutable
public final class ReflectiveInvokeable<T, R> implements Invokeable<R> {
	/**Method to be invoked.*/
	private Method method;
	/**Object to invoke method on, null for static methods.*/
	private T target;
	
	public ReflectiveInvokeable(Method method) {this(method, null);}
	public ReflectiveInvokeable(Method method, T target) {initialize(method, target);}
	public ReflectiveInvokeable(String method, Class target) {initialize(findMethod(method, target), null);}
	public ReflectiveInvokeable(String method, T target) {initialize(findMethod(method, target.getClass()), target);}

	
	private void initialize(Method method, T target) {
		if (Modifier.isStatic(method.getModifiers()) && target != null) {
			throw new IllegalArgumentException("Cannot supply a target for static methods.");
		}

		if (target != null && !method.getDeclaringClass().isAssignableFrom(target.getClass())) {
			throw new IllegalArgumentException("Method and target object are not type compatible.");
		}
		
		this.method = method;
		this.target =target;
	}

	//Find the method amidst the class
	private Method findMethod(String methodName, Class clss) {
		for (Method m: clss.getMethods()) {
			if (m.getName().equals(methodName)) {return m;}
		}
		
		throw new IllegalArgumentException(String.format("Could not find method named %1$s in class %2$s.", methodName, clss.getName()));
	}
	
	/**Is the underlying method static?*/
	public boolean isStatic() {return target==null;}
	public T getTarget() {return target;}
	public Method getMethod() {return method;}
	
	
	public Tuple tupleInvoke(Object[] arguments) throws MethodInvokeFailedException {
		R result = invoke(arguments);
		Tuple t=null;
		if (result != null) {t=Converter.toTuple(result);}
		return t;
	}
	
	/* (non-Javadoc)
	 * @see stencil.operator.util.Invokeable#invoke(java.lang.Object[])
	 */
	public R invoke(Object[] arguments) throws MethodInvokeFailedException {
		int expectedNumArgs = method.getParameterTypes().length;
		R result;

		if ((arguments.length != expectedNumArgs && !method.isVarArgs()) ||
			(arguments.length < expectedNumArgs &&
					(arguments.length == expectedNumArgs -1 && !method.isVarArgs()))) {
			throw new MethodInvokeFailedException(String.format("Incorrect number of arguments for method specified invoking %1$s (expected %2$s; received: %3$s).", method.getName(), expectedNumArgs, arguments.length));
		}

		Object[] args = null;
		try {
			if (method.isVarArgs()) {
				args = new Object[method.getParameterTypes().length];

				//Copy over fixed arguments
				validateTypes(arguments, method.getParameterTypes(), 0, args.length-1);

				//Prepare variable argument for last position of arguments array
				Class type = method.getParameterTypes()[method.getParameterTypes().length-1].getComponentType();
				Object varArgs = Array.newInstance(type, (arguments.length-expectedNumArgs)+1);
				System.arraycopy(arguments, args.length-1, varArgs, 0, Array.getLength(varArgs));
				args[args.length-1] = varArgs;
				result = (R) method.invoke(target, args);
			} else {
				args = validateTypes(arguments, method.getParameterTypes(), 0, arguments.length);
			}
		} catch (Exception e) {
			throw new MethodInvokeFailedException(String.format("Exception thrown peparing arguments to invoke '%1$s' with arguments %2$s.", method.getName(), java.util.Arrays.deepToString(arguments)),e);
		}
			
		try {
			result = (R) method.invoke(target, args);
		} catch (Exception e) {
		 	throw new MethodInvokeFailedException(String.format("Exception thrown invoking '%1$s' with arguments %2$s.", method.getName(), java.util.Arrays.deepToString(args)), e);
		}
		return result;
	}
	
	/**Copies values from the arguments array to the result array, converting them per the type along the way.
	 * It is assumed that arguments[i] will be converted to type[i] and put in results[i]
	 * 
	 * @param arguments  List of values to put into the results
	 * @param types Types to convert to along the way. 
	 * @param start Index to start converting at
	 * @param end Index to end converting at
	 * @param result Place to store results.
	 */
	private void validateTypes(Object[] arguments, Class<?>[] types, int start, int end, Object result) {
		for (int i=start; i< end; i++) {
			if (arguments.getClass().isAssignableFrom(types[i])) {
				Array.set(result, i, arguments[i]);
			} else {
				Array.set(result, i, Converter.convert(arguments[i], types[i]));
			}
		}
	}
	
}
