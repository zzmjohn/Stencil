tree grammar NoOperatorReferences;
options {
  tokenVocab = Stencil;
  ASTLabelType = StencilTree;  
  filter = true;
  superClass = TreeFilterSequence;
}

@header {
/* Verifies that all operator references have been removed.*/
  package stencil.parser.string.validators;
  
  import stencil.parser.tree.StencilTree;
  import stencil.parser.string.ValidationException;
  import stencil.parser.string.TreeFilterSequence;
}

@members {
  private static final class ReferenceNotRemovedException extends ValidationException {
  	public ReferenceNotRemovedException(String name, String base) {
  		super(String.format("Operator \%1\$s not instantiated (base \%2\$s).", name, base));
  	}
  }

  public static void apply (Tree t) {TreeFilterSequence.apply(t);}
}

topdown
 : ^(r=OPERATOR_REFERENCE base=. spec=.) {throw new ReferenceNotRemovedException($r.getText(), $base.getText());};
  
  