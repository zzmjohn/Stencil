package stencil.interpreter.tree;

import java.util.Arrays;

import stencil.parser.tree.StencilTree;
import stencil.tuple.Tuples;
import stencil.tuple.instances.PrototypedArrayTuple;
import stencil.tuple.prototype.TuplePrototypes;

public final class Specializer extends PrototypedArrayTuple {
	private final StencilTree source;
	
	public Specializer(String[] keys, Object[] vals) {this(keys, vals, null);}
	public Specializer(String[] keys, Object[] values, StencilTree source) {
		super(keys, values);
		this.source = source;
	}
	
	public boolean containsKey(String key) {return prototype().contains(key);}
	public Iterable<String> keySet() {return Arrays.asList(TuplePrototypes.getNames(prototype));}
	
	public StencilTree getSource() {return source;}
	
	public String toString() {return "Specializer -- " + Tuples.toString(this);}

	public Object get(String key, Object defVal) {return this.containsKey(key) ? get(key) : defVal;}
}
