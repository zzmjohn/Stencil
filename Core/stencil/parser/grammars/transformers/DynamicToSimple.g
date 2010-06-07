tree grammar DynamicToSimple;
options {
	tokenVocab = Stencil;
	ASTLabelType = CommonTree;	
	output = AST;
	filter = true;
}

@header{
  /** Ensures that each dynamic binding also has a simple
   * binding in the result list.  If a simple binding is 
   * explicitly provided, the dynamic binding is simply removed
   * from the simple-binding results.  If a simple binding is
   * not provided, the dynamic marker is removed.
   **/

  package stencil.parser.string;
	
  import org.antlr.runtime.tree.*;
  import stencil.parser.tree.Rule;
  import stencil.tuple.prototype.TuplePrototypes;
  import java.util.Arrays;
}

topdown: ^(CONSUMES . . . ^(LIST rule*) .*);
      
rule: ^(RULE t=. cc=. b=.) -> ^(RULE $t $cc DEFINE[":"]);




