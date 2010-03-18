package stencil.operator.module.util;

import stencil.operator.StencilOperator;
import stencil.operator.module.*;
import stencil.parser.tree.Specializer;

import java.util.Map;
import java.util.HashMap;

/**Simple MutableModule.  Does not allow specialization of the Legends.*/
public class MutableModule implements Module {
	protected Map<String, StencilOperator> operators;
	protected MutableModuleData moduleData;

	public MutableModule(String name) {
		operators = new HashMap<String, StencilOperator>();
		moduleData = new MutableModuleData(this, name);
	}

	public ModuleData getModuleData() {return moduleData;}

	public OperatorData getOperatorData(String name, Specializer specializer) throws SpecializationException {
		Specializer defaultSpecializer = getModuleData().getDefaultSpecializer(name);
		if (!specializer.equals(defaultSpecializer)) {throw new SpecializationException(getName(),name, specializer);}
		return moduleData.getOperatorData(name);		
	}

	public StencilOperator instance(String name, Specializer specializer) throws SpecializationException {
		StencilOperator op = operators.get(name);
		Specializer defSpec = op.getOperatorData().getDefaultSpecializer();
		if (op instanceof StencilOperator
				&& !defSpec.equals(specializer)) {
			throw new SpecializationException(getName(),name, specializer);
		}
		
		return operators.get(name);
	}

	public void addOperator(StencilOperator target) {
		try {
			addOperator(target, target.getOperatorData());
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error adding jython legend %1$s to module %2$s", target.getName(), getModuleData().getName()), e);
		}
	}
	
	public void addOperator(StencilOperator target, OperatorData opData) {
		operators.put(target.getName(), target);
		moduleData.addOperator(opData);
	}
	
	public void addOperator(String name, StencilOperator op, OperatorData opData) {
		operators.put(name, op);
		moduleData.addOperator(opData);
	}
	
	public String getName() {return moduleData.getName();}
}
