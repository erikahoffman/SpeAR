package com.rockwellcollins.spear.translate.transformations;

import com.rockwellcollins.spear.translate.intermediate.PatternDocument;
import com.rockwellcollins.spear.translate.intermediate.SpearDocument;
import com.rockwellcollins.spear.translate.intermediate.TypeDocument;

public class PerformTransforms {

	public static void apply(SpearDocument doc) throws Exception {
		ReplaceAbstractTypes.transform(doc);
		ReplaceVariableArrayDefs.transform(doc);
		PropagatePredicates.transform(doc); 
		CreateUserNamespace.transform(doc); //this must come after the previous 3
		RemoveCompositeReferences.transform(doc);
		ReplaceShortHandRecords.transform(doc);
		NormalizeOperators.transform(doc);
		RemoveSugar.transform(doc);
		ReplaceSpecificationCalls.transform(doc);
		UniquifyNormalizedCalls.transform(doc);
	}
	
	public static void apply(PatternDocument doc) throws Exception {
		ReplaceAbstractTypes.transform(doc);
		ReplaceVariableArrayDefs.transform(doc);
		PropagatePredicates.transform(doc);
		CreateUserNamespace.transform(doc); //this must come after the previous 3
		ReplaceShortHandRecords.transform(doc);
		NormalizeOperators.transform(doc);
		RemoveSugar.transform(doc);

	}
	
	public static void apply(TypeDocument doc) throws Exception {
		ReplaceAbstractTypes.transform(doc);
		ReplaceVariableArrayDefs.transform(doc);
		CreateUserNamespace.transform(doc); //this must come after the previous 2
		NormalizeOperators.transform(doc);
		RemoveSugar.transform(doc);
	}
}
