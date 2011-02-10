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
import stencil.module.operator.util.AbstractOperator;
import stencil.modules.stencilUtil.Range;
import stencil.modules.stencilUtil.StencilUtil;
import stencil.module.util.*;
import stencil.module.util.ann.*;
import stencil.interpreter.tree.Freezer;
import stencil.interpreter.tree.Specializer;
import stencil.types.Converter;
import static stencil.module.util.ModuleDataParser.operatorData;
import stencil.parser.string.StencilParser;
import stencil.parser.string.util.Context;

@Description("Math functions; includes many simple re-directions to java.lang.Math.")
@Module()
public class Numerics extends BasicModule {

	/**Parse an integer from a string.*/
	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double value)", alias={"map","query"})
	public static int parseInt(String string, int radix) {
		return Integer.parseInt(string, radix);
	}	

	@Operator(name="Log", spec="[base: NULL]")
	public static final class LogFixed extends AbstractOperator {
		private final double base;
		public LogFixed(OperatorData od, double base) {
			super(od);
			this.base = base;
		}

		@Facet(memUse="FUNCTION", prototype="(double log)", alias={"map","query"})
		public Double query(double value) {
			return Math.log(value)/Math.log(base);
		}		
		
		//Alternative implementation, for two-argument log 
		public static final Double logParam(double base, double value) {return Math.log(value)/Math.log(base);}
		
		public static StencilOperator instance(OperatorData od, Specializer spec) {
			Object base = spec.get("base");
			if (base instanceof String && ((String) base).toLowerCase().equals("e")) {
				od = new OperatorData(od);
				od.setTarget("log");
				return Modules.instance(java.lang.Math.class, od);
			} else if (base instanceof Number){
				double bs = Converter.toDouble(spec.get("base"));
				if (bs == 10) {
					od = new OperatorData(od);
					od.setTarget("log10");
					return Modules.instance(java.lang.Math.class, od);					
				} else {
					return new LogFixed(od, bs);
				}
			} else {
				od = new OperatorData(od);
				od.setTarget("logParam");
				return Modules.instance(Numerics.LogFixed.class, od);
			}
		}
	}
	
	
	/**Sum of full range of values.
	 * TODO: Modify to handle fixed-start range
	 **/
	@Suppress 
	@Operator(name="Sum", tags=stencil.modules.stencilUtil.StencilUtil.RANGE_OPTIMIZED_TAG)
	public static final class FullSum extends AbstractOperator.Statefull {
 		private double sum = 0;
 		
 		public FullSum(OperatorData opData) {super(opData);}
		
 		protected static double sum(double... values) {
 			double sum = 0;
 			for (double v: values) {
				sum = sum + v;
			}
 			return sum;
 		}

 		@Facet(memUse="WRITER", prototype="(double sum)")
		public double map(double... args) {
			sum += sum(args);
			stateID++;
			return sum;
		}

		/**Arguments are ignored.*/
 		@Facet(memUse="READER", prototype="(double sum)")
		public double query(Object... args) {
			return sum;
		}

 		public FullSum duplicate() {return new FullSum(operatorData);}
	}

	/**Minimum of full range of values.
	 * TODO: Modify to handle fixed-start range
	 */
	@Suppress
	@Operator(name="Min", tags=stencil.modules.stencilUtil.StencilUtil.RANGE_OPTIMIZED_TAG)
	public static final class FullMin extends AbstractOperator.Statefull {
 		private double min = Double.MAX_VALUE;

 		public FullMin(OperatorData opData) {super(opData);}

 		
 		protected static double min(double... values) {
			double min = Double.MAX_VALUE;
 			for (double v: values) {
				min = Math.min(min, v);
			}
			return min;
 		}
 		
 		@Facet(memUse="WRITER", prototype="(double min)")
 		public double map(double... values) {
 			double newMin = Math.min(min, min(values));
 			if (newMin != min) {
 				stateID++;
 				min = newMin;
 			}
			return min;
		}

		/**Arguments are ignored.*/
 		@Facet(memUse="READER", prototype="(double min)")
		public double query(Object... args) {
			return min;
		}

		public FullMin duplicate() {return new FullMin(operatorData);}
	}

	/**Maximum of full range of values.
	 * TODO: Modify to handle fixed-start range
	 */
	@Suppress
	@Operator(name="Max", tags=StencilUtil.RANGE_OPTIMIZED_TAG)
	public static class FullMax extends AbstractOperator.Statefull {
 		private double max = -Double.MAX_VALUE;	

 		public FullMax(OperatorData opData) {super(opData);}
 		
 		protected static double max(double... values) {
 			double max = -Double.MAX_VALUE;
 			for (double v: values) {
				max = Math.max(max, v);
			}
			return max;
 		}

 		@Facet(memUse="WRITER", prototype="(double max)")
		public double map(double... values) {
 			double newMax = Math.max(max, max(values));
 			if (newMax != max) {
 				max = newMax;
 				stateID++;
 			}
 			return max;
		}
		
		/**Arguments are ignored.*/
 		@Facet(memUse="READER", prototype="(double max)")
		public double query(Object... args) {
			return max;
		}
		
		public FullMax duplicate() {return new FullMax(operatorData);}
	}
	

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double abs)", alias={"map","query"})
	public static double abs(double d) {return Math.abs(d);}
	
	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double sum)", alias={"map","query"})	
	public static double add1(double d) {return d+1;}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double sum)", alias={"map","query"})	
	public static double add(double d, double d2) {return d+d2;}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double quotient)", alias={"map","query"})	
	public static long div(double d1, double d2) {return Math.round(d1)/Math.round(d2);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double quotient)", alias={"map","query"})	
	public static double divide(double d1, double d2) {return d1/d2;}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double product)", alias={"map","query"})	
	public static double mult(double d1, double d2) {return d1*d2;}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double value)", alias={"map","query"})	
	public static double negate(double d) {return -1 * d;}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double mod)", alias={"map","query"})	
	public static double mod(double d1, double d2) {return Math.round(d1)%Math.round(d2);}

	@Operator(tags=StencilUtil.RANGE_FLATTEN_TAG)
	@Facet(memUse="FUNCTION", prototype="(double max)", alias={"map","query"})	
	public static double max(double... ds) {return FullMax.max(ds);}

	@Operator(tags=StencilUtil.RANGE_FLATTEN_TAG)
	@Facet(memUse="FUNCTION", prototype="(double min)", alias={"map","query"})	
	public static double min(double... ds) {return FullMin.min(ds);}
	
	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double diff)", alias={"map","query"})
	public static double sub(double d, double d2) {return d-d2;}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double diff)", alias={"map","query"})
	public static double sub1(double d) {return d-1;}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double sum)", alias={"map","query"})
	public static double sum(double...ds) {return FullSum.sum(ds);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double cos)", alias={"map","query"})
	public static double cosine(double deg) {return Math.cos(Math.toRadians(deg));}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double sin)", alias={"map","query"})
	public static double sine(double deg) {return Math.sin(Math.toRadians(deg));}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double tan)", alias={"map","query"})
	public static double tangent(double deg) {return Math.tan(Math.toRadians(deg));}

	@Operator(spec="[range: LAST, split: 0]", name="ACos")
	@Facet(memUse="FUNCTION", prototype="(double acos)", alias={"map","query"})
	public static double acos(double a) {return Math.acos(a);}

	@Operator(spec="[range: LAST, split: 0]", name="ASin")
	@Facet(memUse="FUNCTION", prototype="(double asin)", alias={"map","query"})
	public static double asin(double a) {return Math.asin(a);}
	
	@Operator(spec="[range: LAST, split: 0]", name="ATan")
	@Facet(memUse="FUNCTION", prototype="(double atan)", alias={"map","query"})
	public static double atan(double a) {return Math.atan(a);}
	
	@Operator(spec="[range: LAST, split: 0]", name="ATan2")
	@Facet(memUse="FUNCTION", prototype="(double atan)", alias={"map","query"})
	public static double atan2(double x, double y) {return Math.atan2(x,y);}
	
	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double cbrt)", alias={"map","query"})
	public static double cbrt(double a) {return Math.cbrt(a);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double ceil)", alias={"map","query"})
	public static double ceil(double a) {return Math.ceil(a);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double cos)", alias={"map","query"})
	public static double cos(double a) {return Math.cos(a);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double cosh)", alias={"map","query"})
	public static double cosh(double a) {return Math.cosh(a);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double exp)", alias={"map","query"})
	public static double exp(double a) {return Math.exp(a);}
		
	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double expm1)", alias={"map","query"})
	public static double expm1(double a) {return Math.expm1(a);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double floor)", alias={"map","query"})
	public static double floor(double a) {return Math.floor(a);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double hypot)", alias={"map","query"})
	public static double hypot(double x, double y) {return Math.hypot(x,y);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double ieee)", alias={"map","query"})
	public static double IEEERemainder(double f1, double f2) {return Math.IEEEremainder(f1, f2);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double log)", alias={"map","query"})
	public static double Log1p(double x) {return Math.log1p(x);}
	
	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double product)", alias={"map","query"})
	public static double pow(double a, double b) {return Math.pow(a, b);}

	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double val)", alias={"map","query"})
	public static double round(double a) {return Math.round(a);}
	
	@Operator()
	@Facet(memUse="OPAQUE", prototype="(double val)", alias={"map","query"})
	public static double random() {return Math.random();}

	@Operator()
	@Facet(memUse="OPAQUE", prototype="(double sign)", alias={"map","query"})
	public static double signum(double d) {return Math.signum(d);}
	
	@Operator()
	@Facet(memUse="OPAQUE", prototype="(double sin)", alias={"map","query"})
	public static double sin(double d) {return Math.sin(d);}
	
	@Operator()
	@Facet(memUse="OPAQUE", prototype="(double sinh)", alias={"map","query"})
	public static double sinh(double d) {return Math.sinh(d);}

	@Operator()
	@Facet(memUse="OPAQUE", prototype="(double sqrt)", alias={"map","query"})
	public static double sqrt(double d) {return Math.sqrt(d);}

	@Operator()
	@Facet(memUse="OPAQUE", prototype="(double tan)", alias={"map","query"})
	public static double tan(double d) {return Math.tan(d);}
	
	@Operator()
	@Facet(memUse="OPAQUE", prototype="(double tanh)", alias={"map","query"})
	public static double tanh(double d) {return Math.tanh(d);}

	@Operator()
	@Facet(memUse="OPAQUE", prototype="(double radians)", alias={"map","query"})
	public static double toRadians(double degrees) {return Math.toRadians(degrees);}

	@Operator()
	@Facet(memUse="OPAQUE", prototype="(double degrees)", alias={"map","query"})
	public static double toDegrees(double radians) {return Math.toRadians(radians);}

	@Operator()
	@Facet(memUse="OPAQUE", prototype="(double ulp)", alias={"map","query"})
	public static double ulp(double d) {return Math.ulp(d);}
	
	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double value)", alias={"map","query"})
	public static double nearest(long m, long n) {
 		//Round m to the nearest multiple of n (per http://mindprod.com/jgloss/round.html)
 		long near = ( m + n/2 ) / n * n;
 		return near;
 	}
 	
	@Operator()
	@Facet(memUse="FUNCTION", prototype="(double value)", alias={"map","query"})
	//TODO: Remove when converter has its own module/operator
 	public static Number asNumber(Object v) {return Converter.toNumber(v);}
 	
	public StencilOperator instance(String name, Context context, Specializer specializer) throws SpecializationException {
		OperatorData operatorData = getModuleData().getOperator(name);

		validate(name, specializer);
		
		try {
			if (name.equals("Log")) {return LogFixed.instance(operatorData, specializer);}

			if (context == null || context.highOrderUses("Range").size() ==0) {return Modules.instance(this.getClass(), operatorData);}

			Specializer spec = Freezer.specializer(context.highOrderUses("Range").get(0).find(StencilParser.SPECIALIZER));
			Range range = new Range(spec.get(Range.RANGE_KEY));
			
			if (range.isFullRange()) {
				if (name.equals("Sum")) {return new FullSum(operatorData(FullSum.class, getName()));}
				if (name.equals("Max")) {return new FullMax(operatorData(FullMax.class, getName()));}
				if (name.equals("Min")) {return new FullMin(operatorData(FullMin.class, getName()));}
			} 


		} catch (Exception e) {throw new Error(String.format("Error locating %1$s operator in Numerics package.", name), e);}
		throw new Error("Unnanticipated argument set encountered");
	}
}