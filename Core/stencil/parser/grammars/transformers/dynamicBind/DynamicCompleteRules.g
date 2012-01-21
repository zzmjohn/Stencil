tree grammar DynamicCompleteRules;
options {
	tokenVocab = Stencil;
	ASTLabelType = StencilTree;	
	superClass = TreeRewriteSequence;
	output = AST;
	filter = true;
}

@header{
  /** Modifies dynamic binding query and update rules to use the correct operator facets. **/
  package stencil.parser.string;
	
  import stencil.parser.tree.AstInvokeable;
  import stencil.parser.tree.StencilTree;
  import static stencil.parser.string.util.Utilities.stateQueryList;
  import static stencil.parser.string.util.Utilities.counterpart;
}

@members {
  public static StencilTree apply (Tree t) {return (StencilTree) TreeRewriteSequence.apply(t);}
  
  public StencilTree downup(Object t) {
    t=downup(t, this, "changeType");
    t=downup(t, this, "convert");
    t=downup(t, this, "toCounterpart");
    return (StencilTree) t;
  }  
  

}

changeType: ^(CONSUMES f=. pf=. l=. r=. ^(RULES_DYNAMIC toDynamic*));
toDynamic:  ^(r=RULE rest+=.*) -> ^(DYNAMIC_RULE {adaptor.dupTree($r)} {adaptor.dupTree($r)});
            
convert: ^(DYNAMIC_RULE r=. sq=.) -> ^(DYNAMIC_RULE $r {stateQueryList(adaptor, $sq)});  

toCounterpart 
  @after{((AstInvokeable) inv).changeFacet(counterpart((AstInvokeable) inv, facet.getText()));}
  : ^(FUNCTION inv=. ^(OP_NAME pre=. base=. facet=.) rest+=.*) 
          {$inv.getAncestor(DYNAMIC_RULE) != null}? ->
          ^(FUNCTION $inv ^(OP_NAME $pre $base ID[counterpart((AstInvokeable) inv, facet.getText())]) $rest*);
