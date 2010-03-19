package stencil.parser.string;

import stencil.tuple.prototype.TuplePrototype;
import stencil.tuple.prototype.TuplePrototypes;
import stencil.operator.module.Module;
import stencil.operator.module.ModuleCache;
import stencil.operator.module.util.OperatorData;
import stencil.parser.tree.*;
import stencil.parser.tree.util.Environment;
import stencil.tuple.prototype.SimplePrototype;
import stencil.util.MultiPartName;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.CommonTree;

/**The environment proxy is used during compilation to mimic the shape of, 
 * but not the content of, runtime environment.
 * 
 * It carries prototype information instead of values; as such, it also
 * contains some compilation-specified prototype related utilities.
 * 
 * @author jcottam
 *
 */
public abstract class EnvironmentProxy {
	public static final class FrameException extends RuntimeException {
		private String frame;
		private TuplePrototype contents;
		private FrameException prior;

		public FrameException(int idx) {super("Frame reference out of bounds " + idx);}
		public FrameException(String message) {super(message);}

		public FrameException(String name, String frame, TuplePrototype contents) {
			this(name, frame, contents, null);
		}

		public FrameException(String name, String frame, TuplePrototype contents, FrameException prior) {
			super("Could not find field '" + name + "'.\n" + briefMessage(frame, contents, prior));
			this.frame = frame;
			this.contents= contents;
			this.prior = prior;
		}

		private static String briefMessage(String frame, TuplePrototype contents, FrameException prior) {
			StringBuilder b = new StringBuilder();
			b.append(String.format("\tSearched in frame %1$s (fields: %2$s).\n", frame, Arrays.deepToString(TuplePrototypes.getNames(contents))));
			if (prior != null) {
				b.append(briefMessage(prior.frame, prior.contents, prior.prior));
			}
			return b.toString();
		}
	}
	
	public abstract EnvironmentProxy push(String label, TuplePrototype t);
	public abstract boolean isFrameRef(String name);
	public abstract int frameRefFor(String name);
	public abstract int currentIndex();
	public abstract int getFrameIndex(String name);
	public abstract TuplePrototype get(int idx);

	/**Complete proxy environment implementation.*/
	public static class Proxy extends EnvironmentProxy {
		final Proxy parent;
		final TuplePrototype prototype;
		final String label;
	
		public Proxy(String label, TuplePrototype prototype) {this(label, prototype, null);}
		Proxy(String label, TuplePrototype prototype, Proxy parent) {
			this.label = label;
			this.prototype = prototype;
			this.parent = parent;
		}
	
		//Convert the name to a numeric ref (can be either a frame or tuple ref)
		public int getFrameIndex(String name) {
			if (label != null && label.equals(name)) {return this.currentIndex();}
			else if (parent == null) {throw new FrameException("Could not find frame named: " + name);}
			else {return parent.getFrameIndex(name);}
		}
	
		public TuplePrototype get(int idx) {
			if (currentIndex() == idx) {return prototype;}
			else if (parent == null) {throw new FrameException(idx);}
			else {return parent.get(idx);}
		}
	
		public int frameRefFor(String name) {
			if (prototype.contains(name)) {return currentIndex();}
			if (label != null && label.equals(name)) {return currentIndex();}
			if (parent == null) {
				throw new FrameException(name, label, prototype);
			}
			try {return parent.frameRefFor(name);}
			catch (FrameException e) {
				throw new FrameException(name, label, prototype, e);
			}
		}
	
		public boolean isFrameRef(String name) {
			return (label!= null && label.equals(name)) 
			|| (parent != null && parent.isFrameRef(name));
		}
	
		public EnvironmentProxy push(String label, TuplePrototype t) {
			return new Proxy(label, t, this);
		}    
	
		public int currentIndex() {
			int index =0;
			Proxy prior=parent;
			while (prior != null) {
				prior = prior.parent;
				index++;
			}
			return index;
		}
	}

	/**Utility object for calculating, passing and referring 
	 * to potential ancestors of a tree.
	 */
	private static class AncestryPackage {
		final Consumes c;
		final Operator o;
		final Guide g;
		final Layer l;
		final Program program;
		final CommonTree focus;
		final boolean inRuleList;	//Is this operator in a rule list, or just a single rule?
		
		public AncestryPackage(CommonTree t) {
			c = (Consumes) t.getAncestor(StencilParser.CONSUMES);
			o = (Operator) t.getAncestor(StencilParser.OPERATOR);
			g = (Guide) t.getAncestor(StencilParser.GUIDE);
			l= (Layer) t.getAncestor(StencilParser.LAYER);
			
			Rule r= (Rule) t.getAncestor(StencilParser.RULE);
			
			//r can be null if we are in a predicate
			inRuleList = (r !=null && r.getParent().getType() == StencilParser.LIST);
			
			program = (Program) t.getAncestor(StencilParser.PROGRAM);
			focus = t;
		}
	}
	
	/**Create a new Proxy environment from a default environment.
	 * The proxy environment will have the default labels on the initial
	 * frames and empty labels on subsequent frames.
	 * @param streamName  Name applied to the stream frame.
	 * @return
	 */
	public static EnvironmentProxy fromDefault(Environment env, String streamName) {
		Proxy p = null; //root frame has null parent, so this is safe...for now
		for (int i=0; i<Environment.DEFAULT_SIZE; i++) {
			String name;
			if (i==Environment.STREAM_FRAME) {name= streamName;}
			else {name = Environment.DEFAULT_FRAME_NAMES[i];}
			p = new Proxy(name, env.get(i).getPrototype(), p);
		}
		
		for (int i=Environment.DEFAULT_SIZE; i<env.size();i++) {
			p = new Proxy("", env.get(i).getPrototype(),p);
		}
		return p;
	}

	/**Given a list of rules, what is the eventual prototype?*/
	public static TuplePrototype calcPrototype(List<Rule> rules) {
		List<TupleFieldDef> defs = new ArrayList();
		for (Rule r: rules) {
			for (TupleFieldDef def: r.getTarget().getPrototype()) {
				defs.add(def);
			}
		}
		return new SimplePrototype(TuplePrototypes.getNames(defs), TuplePrototypes.getTypes(defs));
	}

	public static EnvironmentProxy extend(EnvironmentProxy env, DirectYield pass, CommonTree callTree, ModuleCache modules) {
		String label = pass.getName();
		Function call = (Function) callTree;
		TuplePrototype prototype = getPrototype(call, modules);
		return env.push(label, prototype);
	} 

	private static TuplePrototype getPrototype(Function call, ModuleCache modules) {
		MultiPartName name= new MultiPartName(call.getName());
		Module m;
		try{
			m = modules.findModuleForOperator(name.prefixedName()).module;
		} catch (Exception e) {
			throw new RuntimeException("Error getting module information for operator " + name, e);
		}

		try {
			OperatorData od = m.getOperatorData(name.getName(), call.getSpecializer());
			return od.getFacet(name.getFacet()).getPrototype();
		} catch (Exception e) {throw new RuntimeException("Error getting operator data for " + name, e);}       
	}

	public static EnvironmentProxy initialEnv(CommonTree t, ModuleCache modules) {
		AncestryPackage ancestry = new AncestryPackage(t);
		TuplePrototype[] prototypes = new TuplePrototype[Environment.DEFAULT_SIZE];
		prototypes[Environment.CANVAS_FRAME] = stencil.display.CanvasTuple.PROTOTYPE;
		prototypes[Environment.VIEW_FRAME] = stencil.display.ViewTuple.PROTOTYPE;
		prototypes[Environment.STREAM_FRAME] = calcStreamProxy(ancestry, modules);
		prototypes[Environment.PREFILTER_FRAME] = calcPrefilterProxy(ancestry);
		prototypes[Environment.LOCAL_FRAME] = calcLocalProxy(ancestry);

		//Construct the actual proxy environment
		EnvironmentProxy proxy = new Proxy(Environment.DEFAULT_FRAME_NAMES[0], prototypes[0]);
		for (int i=1; i< prototypes.length; i++) {
			proxy = proxy.push(Environment.DEFAULT_FRAME_NAMES[i], prototypes[i]);
		}
		return proxy;
	}
	
	private static TuplePrototype calcStreamProxy(AncestryPackage anc, ModuleCache modules) {
		if (anc.c == null && anc.l!= null) {return new SimplePrototype();}//This is the characteristic of the defaults block
		if (anc.c != null) {return External.find(anc.c.getStream(), anc.program.getExternals()).getPrototype();}
		if (anc.o != null) {return anc.o.getYields().getInput();}
		if (anc.g != null) {
			if (anc.inRuleList) {
				return anc.g.getGenerator().getTarget().getPrototype();
			} else {return anc.g.getSeedOperator().getSamplePrototype();}
		}
		
		throw new RuntimeException("Could not calculate stream proxy for tree: " + anc.focus.toStringTree());
	}
	
	private static TuplePrototype calcPrefilterProxy(AncestryPackage anc) {
		if (anc.c != null) {return calcPrototype(anc.c.getPrefilterRules());}
		if (anc.o != null) {return calcPrototype(anc.o.getPrefilterRules());}
		
		return new SimplePrototype();
	}
	
	private static TuplePrototype calcLocalProxy(AncestryPackage anc) {
		if (anc.c != null) {return calcPrototype(anc.c.getLocalRules());}
		return new SimplePrototype();
	}

}
