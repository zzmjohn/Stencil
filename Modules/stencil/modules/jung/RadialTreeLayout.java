package stencil.modules.jung;

import edu.uci.ics.jung.graph.DelegateForest;
import stencil.module.util.OperatorData;
import stencil.module.util.ann.Operator;
import stencil.interpreter.tree.Specializer;

@Operator(spec="[width: 500, height: 500, steps: 50]")
public final class RadialTreeLayout extends GraphOperator.SizedOperator {
	public RadialTreeLayout(OperatorData opData, Specializer spec) {
		super(opData, spec); 
	}


	protected void resetLayout() {
		edu.uci.ics.jung.algorithms.layout.RadialTreeLayout cLayout = new edu.uci.ics.jung.algorithms.layout.RadialTreeLayout(new DelegateForest(graph));
		cLayout.setSize(size);
		cLayout.reset();
		setLayout(cLayout);
	}
}
