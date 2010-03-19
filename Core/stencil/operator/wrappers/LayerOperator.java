package stencil.operator.wrappers;

import static java.lang.String.format;

import stencil.display.DisplayLayer;
import stencil.operator.StencilOperator;
import stencil.operator.module.util.Modules;
import stencil.operator.module.util.FacetData;
import stencil.operator.module.util.OperatorData;
import static stencil.operator.module.util.OperatorData.TYPE_NA;
import stencil.operator.util.Invokeable;
import stencil.operator.util.ReflectiveInvokeable;
import stencil.tuple.prototype.TuplePrototype;

/**Wraps a layer as an operator. 
 * Exposes find, makeOrFind, make and remove.
 * 
 * MakeOrFind is used for Map.
 * Find is used for Query.
 */

//Marked final because it is immutable (however, it has mutable components....)
public final class LayerOperator implements StencilOperator {
	public static final String FIND_FACET = "find";
	private static final String FIND_METHOD = "find";
	
	public static final String REMOVE_FACET = "remove";
	private static final String REMOVE_METHOD = "remove";
	
	protected final DisplayLayer layer;
	protected final OperatorData operatorData;
	
	public LayerOperator(String module, DisplayLayer layer) {
		this.layer = layer;

		TuplePrototype prototype = layer.getPrototype();
		operatorData = Modules.basicLegendData(module, getName());
		operatorData.addFacet(new FacetData(FIND_FACET, TYPE_NA, false, prototype));
		operatorData.addFacet(new FacetData(MAP_FACET, TYPE_NA, false, prototype));
		operatorData.addFacet(new FacetData(QUERY_FACET, TYPE_NA, false, prototype));
		operatorData.addFacet(new FacetData(REMOVE_FACET, TYPE_NA, false, prototype));
	}
	
	public String getName() {return layer.getName();}

	public Invokeable getFacet(String facet) {
		if (StencilOperator.MAP_FACET.equals(facet) 
			|| StencilOperator.QUERY_FACET.equals(facet)
			|| FIND_FACET.equals(facet)) {
			return new ReflectiveInvokeable(FIND_METHOD, layer);
		} else if (REMOVE_FACET.equals(facet)) {
			return new ReflectiveInvokeable(REMOVE_METHOD, layer);
		}
		throw new IllegalArgumentException(format("Could not create facet for requested name '%1$s'.", facet));
	}

	public OperatorData getOperatorData() {return operatorData;}
	public LayerOperator duplicate() {throw new UnsupportedOperationException();}
}