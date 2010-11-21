/* Copyright (c) 2006-2008 Indiana University Research and Technology Corporation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * - Neither the Indiana University nor the names of its contributors may be used
 *  to endorse or promote products derived from this software without specific
 *  prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package stencil.module.operator.wrappers;

import stencil.module.operator.StencilOperator;
import stencil.module.operator.util.Invokeable;
import stencil.module.operator.util.Range;
import stencil.module.util.OperatorData;
import stencil.parser.tree.Value;
import stencil.tuple.Tuple;
import stencil.types.Converter;
import static stencil.module.util.Utilities.noFunctions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**Wraps an operator with range support.
 * Keeps a copy of every argument set seen within the range
 * and re-invokes the operator with an argument list equal to concatenating
 * all in-range arguments.  
 * 
 * Warnings:  
 * 1) If using for a range with a static start, the memory consumption is linear with the number of tuples seen
 * 2) Assumes that an operator instance is used either in Query or Map context (not both).
 * 
 */

public abstract class RangeHelper implements StencilOperator, Cloneable {
	private static abstract class AbstractRangeTarget implements Invokeable {
		final RangeHelper helper;
		final Invokeable base;
		
		public AbstractRangeTarget(RangeHelper helper, Invokeable base) {
			this.helper = helper;
			this.base = base;
		}
		
		public Object getTarget() {return this;}
		
		public Tuple tupleInvoke(Object[] arguments) {
			return Converter.toTuple(invoke(arguments));
		}
	}
	
	private static final class QueryRangeTarget extends AbstractRangeTarget {
		public QueryRangeTarget(RangeHelper helper, Invokeable base) {
			super(helper, base);
		}
		
		public Object invoke(Object[] args) {
			Object[] formals = helper.getCache();
			return base.invoke(formals);
		}
	}
	
	/**Invokeable object returned by ranging operators.*/
	private static final class RangeTarget extends AbstractRangeTarget {
		public RangeTarget(RangeHelper helper, Invokeable base) {
			super(helper, base);
		}

		public Object invoke(Object[] args) {
			Object[] formals = helper.updateCache(args);
			return base.invoke(formals);
		}
	}
	
	private static final class RelativeHelper extends RangeHelper {
		public RelativeHelper(Range range, StencilOperator operator, String facetName) {
			super(range, operator, facetName);
			
			if (range.getStart() < range.getEnd()) {throw new IllegalArgumentException("Range ends before it starts: " + range.toString());}
			
			values = new ArrayList(range.getStart());
		}
		 
		protected Object[] updateCache(Object... args) {
			//Rotate in the new values
			if (values.size() > range.getStart()) {values.remove(0);} //Range.start indicates the oldest value that needs to be remembered.  In an offset, this is the larger number
			values.add(args);
			return getCache();
		}
		
		protected Object[] getCache() {
			//We can always start at 0, since that is the 'oldest' value, but we may
			//have an end that is not the end of the range we must remember
			//(e.g. the range arg was -10 to -5, just five items are returned but you need to remember 10)
			int endRange = values.size()-1 > range.getEnd() ? values.size()-1 - range.getEnd() : 0;
			Object[] formals = values.subList(0, endRange).toArray();
			return formals;			
		}
	}
	
	private static final class AbsoluteHelper extends RangeHelper {
		private boolean trimmed = false;
		private int offsetCountdown;
		
		public AbsoluteHelper(Range range, StencilOperator operator, String facetName) {
			super(range, operator, facetName);

			if (range.getStart() > range.getEnd()) {throw new IllegalArgumentException("Range ends before it starts: " + range.toString());}

			values = new ArrayList(range.getEnd());
			offsetCountdown = range.getStart();
		}
		
		protected Object[] updateCache(Object... args) {
			if (offsetCountdown >0) {offsetCountdown--; return new Object[0];}
			
			if (values.size() < range.getEnd()) {
				values.add(args);
			} else if (!trimmed) {
				//A range with absolute indices on both start and end will eventually become a constant...
				//so we trim it to that constant value
				values = values.subList(0, range.getEnd()); //The endpoint is exclusive, but range is 1-based, so it all works out!
				trimmed = true;
			}
			return getCache();
		}
		
		protected Object[] getCache() {return values.toArray();}
	}
	
	private static final class HybridHelper extends RangeHelper {
		int offsetCountdown;
			
		public HybridHelper(Range range, StencilOperator operator, String facetName) {
			super(range, operator, facetName);
			
			if (range.relativeStart()) {throw new RuntimeException("Hybrid ranges must have absolute start points.  Recieved range " + range.toString());}
			values = new ArrayList();
			offsetCountdown = range.getStart();
		}
		
		protected Object[] updateCache(Object... args) {
			if (offsetCountdown >0) {offsetCountdown--; return new Object[0];}
			values.add(args);			
			return getCache();
		}
		
		protected Object[] getCache() {
			return values.subList(0, range.getEnd()).toArray();
		}
	}
	

	protected List values;	 			//One list, used by both Query and Map.  Invoking a single instance in both may end up arguments appended multiple times. 
	protected final Range range;
	protected final OperatorData operatorData;
	private final StencilOperator baseOperator; 
	protected final Invokeable baseFacet;		//Actual facet invokable for doing computations
	private final String baseFacetName;				//Facet on the operator to be used in ranged operations.  MUST be a function.
	
	protected RangeHelper(Range range, StencilOperator operator, String facetName) {
		this.baseOperator = operator;
		this.range = range;
		this.baseFacetName = facetName;
		this.baseFacet = operator.getFacet(facetName);
		this.operatorData =  noFunctions(operator.getOperatorData());		
	}

	public OperatorData getOperatorData() {return operatorData;}

	/**Get a facet on this range helper object.
	 * The returned facet will use the operator/facet specified at construction.
	 * If the operator/facet is not a function, then query is not a premited facetName to use here.
	 */
	public Invokeable getFacet(String facetName) {
		try {
			if (QUERY_FACET.equals(facetName)) {
				if (baseOperator.getOperatorData().getFacet(this.baseFacetName).mutative()) {
					throw new IllegalArgumentException("Cannot construct a ranged query facet if base facet is not a function.");
				} else{
					return new QueryRangeTarget(this, baseFacet);
				}
			} else if (MAP_FACET.equals(facetName)) {
				return new RangeTarget(this, baseFacet);
			} else {
				throw new IllegalArgumentException(String.format("Facet %1$s not known in ranged %2$s.", facetName, baseOperator.getName()));
			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error getting range-wrapped facet '%1$s'", baseFacet), e);
		}
	}

	//TODO: Get away from Object[] by doing two sweeps: pre-calculating size, then fill
	public static Object flatten(final Object values, Class convertTo) {
		final Object result = Array.newInstance(convertTo, size(values));
		int offset = 0;
		
		for (int i=0; i< Array.getLength(values); i++) {
			final Object v = Array.get(values, i);
			if (v.getClass().isArray()) {
				final Object rslt = flatten(v, convertTo);
				for (int j=0; j<Array.getLength(rslt); j++) {
					Object value = Converter.convert(Array.get(rslt, j), convertTo);
					Array.set(result, offset, value);
					offset++;
				}
			} else {
				Object value = Converter.convert(Array.get(values, i), convertTo);
				Array.set(result, offset, value);
				offset++;
			}
		}		
		return result;
	}
	
	/**Recursive size of the passed object.
	 * 
	 * A singleton is size 1.
	 * An array is size of its length.
	 * An array of arrays is the total number of elements in all sub arrays.
	 * 
	 * @param values
	 * @return
	 */
	public static int size(final Object values) {
		final int length = Array.getLength(values);
		int size = 0;
		
		for (int i=0; i<length; i++) {
			Object v = Array.get(values, i);
			if (v.getClass().isArray()) {size += size(v);}
			else {size++;}
		}
		return size;
	}
	 

	public String getName() {return baseOperator.getName() + "(Ranged)";}
	
	/**Updates the list storage according to the specified range.
	 * @return List of arguments to actually invoke the underlying operator with.
	 */
	protected abstract Object[] updateCache(Object... args);
	
	/**Gets a list of stored items according to the specified range.
	 * This is the analog of 'updateCache' but without mutating state.
	 * @return
	 */
	protected abstract Object[] getCache();
	
	public StencilOperator duplicate() {
		StencilOperator op = baseOperator.duplicate();
		return makeOperator(range, op, baseFacetName);
	}
	
	public StencilOperator viewPoint() {
		try {return (StencilOperator) this.clone();}
		catch (Exception e) {throw new Error("Error creating viewpoint in range helper.");}
	}

	
	public List guide(List<Value> formalArguments, List<Object[]> sourceArguments,  List<String> prototype) {throw new UnsupportedOperationException(String.format("Range cannot autoguide (wrapping %1$s).", baseOperator.getName()));}
	public boolean refreshGuide() {throw new UnsupportedOperationException(String.format("Range cannot autoguide (wrapping %1$s).", baseOperator.getName()));}
	
	
	/**Produce an operator that works over the requested range specification.
	 * 
	 * @param range  Range to operator over
	 * @param operator The base operator instance (also the default return value, if the range is simple) 
	 * @param baseFacet The name of the facet to be invoked by the range helper (must be a function)
	 * @return
	 */
	public static StencilOperator makeOperator(Range range, StencilOperator operator, String facetName) {
		if (range.isSimple()) {return operator;}
		if (range.relativeStart() && range.relativeEnd()) {return new RelativeHelper(range, operator, facetName);}
		if (!range.relativeStart() && !range.relativeEnd()) {return new AbsoluteHelper(range, operator, facetName);}
		if (!range.relativeStart() && range.relativeEnd()) {return new HybridHelper(range, operator, facetName);}
		
		throw new RuntimeException("Unsupported paramter combinitation in range: " + range.toString());
	}
}
