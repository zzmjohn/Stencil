package stencil.interpreter.tree;

import stencil.display.DisplayLayer;
import stencil.interpreter.TupleStore;
import stencil.tuple.PrototypedTuple;
import stencil.tuple.Tuple;

//TODO: Merge with StreamDef
public class Layer implements TupleStore {
	private final DisplayLayer impl;
	private final String name;
	private final Specializer spec;
	private final Consumes[] groups;
	
	public Layer(DisplayLayer impl, String name, Specializer spec, Consumes[] groups) {
		super();
		this.impl = impl;
		this.name = name;
		this.groups = groups;
		this.spec = spec;
	}
	
	@Override
	public String getName() {return name;}
	public Consumes[] getGroups() {return groups;}
	public DisplayLayer implementation() {return impl;}
	public Specializer specializer() {return spec;}
	
	@Override
	public void store(Tuple t) {impl.update((PrototypedTuple) t);}
	
	/**Can the object be stored in the underlying layer?
	 * Minimum requirements are (1) t is not null and (2) t has an identifier field.
	 */
	@Override
	public boolean canStore(Tuple t) {
		return
			t != null
			&& t instanceof PrototypedTuple;
	}
}
