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
package stencil.modules;

import stencil.module.SpecializationException;
import stencil.module.operator.StencilOperator;
import stencil.module.operator.util.BasicProject;
import stencil.module.operator.util.Range;
import stencil.module.operator.wrappers.RangeHelper;
import stencil.module.util.*;
import stencil.parser.tree.Specializer;
import stencil.tuple.ArrayTuple;
import stencil.tuple.Tuple;
import stencil.types.Converter;
import static stencil.module.operator.StencilOperator.QUERY_FACET;


public class Numerics extends BasicModule {

	/**Sum of full range of values.
	 * TODO: Modify to handle fixed-start range
	 **/
	public static final class FullSum extends BasicProject {
		private static final String NAME = "Sum";
 		private double sum = 0;
 		
 		public FullSum(OperatorData opData) {super(opData);}
		
 		protected static double sum(double... values) {
 			double sum = 0;
 			for (double v: values) {
				sum = sum + v;
			}
 			return sum;
 		}
 		
		public Tuple map(double... args) {
			sum += sum(args);
			return new ArrayTuple(sum);
		}

		/**Arguments are ignored.*/
		public Tuple query(Object... args) {
			return new ArrayTuple(sum);
		}


 		public String getName() {return NAME;}
 		public FullSum duplicate() {return new FullSum(operatorData);}
	}

	/**Minimum of full range of values.
	 * TODO: Modify to handle fixed-start range
	 */
	public static final class FullMin extends BasicProject {
 		private static final String NAME = "Min";
 		private double min = Double.MAX_VALUE;

 		public FullMin(OperatorData opData) {super(opData);}

 		
 		protected static double min(double... values) {
			double min = Double.MAX_VALUE;
 			for (double v: values) {
				min = Math.min(min, v);
			}
			return min;
 		}
 		public Tuple map(double... values) {
 			min = Math.min(min, min(values));
			return new ArrayTuple(min);
		}

		/**Arguments are ignored.*/
		public Tuple query(Object... args) {
			return new ArrayTuple(min);
		}

		public String getName() {return NAME;}
		public FullMin duplicate() {return new FullMin(operatorData);}
	}

	/**Maximum of full range of values.
	 * TODO: Modify to handle fixed-start range
	 */
	public static class FullMax extends BasicProject {
 		private static final String NAME = "Max";
 		private double max = -Double.MAX_VALUE;	

 		public FullMax(OperatorData opData) {super(opData);}
 		
 		protected static double max(double... values) {
 			double max = -Double.MAX_VALUE;
 			for (double v: values) {
				max = Math.max(max, v);
			}
			return max;
 		}
 		
		public Tuple map(double... values) {
 			max = Math.max(max, max(values));
 			return new ArrayTuple(max);
		}
		
		/**Arguments are ignored.*/
		public Tuple query(Object... args) {
			return new ArrayTuple(max);
		}

		public String getName() {return NAME;}
		public FullMax duplicate() {return new FullMax(operatorData);}

	}

	public static double abs(double d) {return Math.abs(d);}//Included here to ensure that the floating point version is grabbed...
	public static double add1(double d) {return d+1;}
	public static double add(double d, double d2) {return d+d2;}
	public static double div(double d1, double d2) {return Math.round(d1)/Math.round(d2);}
	public static double divide(double d1, double d2) {return d1/d2;}
	public static double mult(double d1, double d2) {return d1*d2;}
	public static double negate(double d) {return -1 * d;}
	public static double mod(double d1, double d2) {return Math.round(d1)%Math.round(d2);}

	public static double max(double... ds) {return FullMax.max(ds);}
	public static double min(double... ds) {return FullMin.min(ds);}
	public static double sub(double d, double d2) {return d-d2;}
	public static double sub1(double d) {return d-1;}
	public static double sum(double...ds) {return FullSum.sum(ds);}

	public static double Cosine(double deg) {return Math.cos(Math.toRadians(deg));}
	public static double Sine(double deg) {return Math.sin(Math.toRadians(deg));}
	public static double Tangent(double deg) {return Math.tan(Math.toRadians(deg));}

	
	public static double nearest(long m, long n) {
 		//Round m to the nearest multiple of n (per http://mindprod.com/jgloss/round.html)
 		long near = ( m + n/2 ) / n * n;
 		return near;
 	}
 	
 	public static Number asNumber(Object v) {return Converter.toNumber(v);}
 	
 	public Numerics(ModuleData md) {super(md);}
 	
 	
	public StencilOperator instance(String name, Specializer specializer) throws SpecializationException {
		OperatorData operatorData = getModuleData().getOperator(name);
		Range range = new Range(specializer.get(Specializer.RANGE));

		validate(name, specializer);
		StencilOperator target = null;
		
		try {
			
			target = Modules.instance(this.getClass(), operatorData);
			if (specializer.isLowMem()) {
				return target;
			} else if (name.equals("Sum") && !range.isFullRange()) {
				target = RangeHelper.makeOperator(range, target, QUERY_FACET);
			} else if (name.equals("Sum")) {
				target = new FullSum(operatorData);
			} else if (name.equals("Max") && !range.isFullRange()) {
				target = RangeHelper.makeOperator(range, target, QUERY_FACET);
			} else if (name.equals("Max")) {
				target = new FullMax(operatorData);
			} else if (name.equals("Min") && !range.isFullRange()) {
				target = RangeHelper.makeOperator(range, target, QUERY_FACET);}
			else if (name.equals("Min") ) {
				target = new FullMin(operatorData);
			}else {throw new IllegalArgumentException(String.format("Unknown method/specializer combination requested: name = %1$s; specializer = %2$s.", name, specializer.toStringTree()));}

		} catch (Exception e) {throw new Error(String.format("Error locating %1$s operator in Numerics package.", name), e);}

		return target;
	}
}