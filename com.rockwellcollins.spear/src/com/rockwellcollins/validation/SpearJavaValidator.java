/*
 * generated by Xtext
 */
package com.rockwellcollins.validation;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.util.SimpleAttributeResolver;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.ComposedChecks;

import com.rockwellcollins.spear.Constant;
import com.rockwellcollins.spear.Constraint;
import com.rockwellcollins.spear.EnumType;
import com.rockwellcollins.spear.EnumValue;
import com.rockwellcollins.spear.FormalConstraint;
import com.rockwellcollins.spear.IdExpr;
import com.rockwellcollins.spear.Macro;
import com.rockwellcollins.spear.NamedType;
import com.rockwellcollins.spear.SpearPackage;
import com.rockwellcollins.spear.Specification;
import com.rockwellcollins.spear.Variable;
import com.rockwellcollins.spear.typing.SpearTypeChecker;
import com.rockwellcollins.spear.typing.TypesAcyclicValidator;
import com.rockwellcollins.spear.units.UnitChecker;
import com.rockwellcollins.spear.utilities.ConstantChecker;

/**
 * This class contains custom validation rules.
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#
 * validation
 */

@ComposedChecks(validators = {TypesAcyclicValidator.class})
public class SpearJavaValidator extends com.rockwellcollins.validation.AbstractSpearJavaValidator {

	public static final String INVALID_ASSUMPTION_REF = "INVALID_ASSUMPTION_REF";

	/**
	 * @param s
	 *            - a Spear specification
	 * 
	 *            This validation will check that assumptions only reference
	 *            input variables and not computed variables.
	 */
	@Check
	public void checkAssumptions(Specification s) {
		Set<String> valids = new HashSet<>();
		for (Variable input : s.getInputs()) {
			valids.add(input.getName());
		}

		Set<String> invalids = new HashSet<>();
		for (Variable output : s.getOutputs()) {
			invalids.add(output.getName());
		}

		for (Variable local : s.getState()) {
			invalids.add(local.getName());
		}

		for (Constraint assumption : s.getAssumptions()) {
			for (IdExpr ide : EcoreUtil2.getAllContentsOfType(assumption, IdExpr.class)) {
				if (ide.getId() instanceof Variable) {
					Variable var = (Variable) ide.getId();
					if (invalids.contains(var.getName())) {
						error("Assumptions may not reference computed (locals or outputs) variables.", ide,
								SpearPackage.Literals.ID_EXPR__ID, INVALID_ASSUMPTION_REF,
								valids.toArray(new String[valids.size()]));
					}
				}
			}
		}
	}
	
	@Check
	public void checkConstantsAreConstant(Constant c) {
		if(!ConstantChecker.isConstant(c)) {
			error("Constant " + c.getName() + " is defined by a non-constant expression.", c.getExpr(), null);
		}
	}
	
	//TODO: add checking for EnumValues over multiple files 
	
	/***********************************************************************************************/
	//Type Checking
	/***********************************************************************************************/
	@Check
	public void typeCheckNamedType(NamedType nt) {
		new SpearTypeChecker(getMessageAcceptor()).checkNamedType(nt);
		new UnitChecker(getMessageAcceptor()).checkNamedType(nt);
	}
	
	@Check
	public void typeCheckConstant(Constant c) {
		new SpearTypeChecker(getMessageAcceptor()).checkConstant(c);
		new UnitChecker(getMessageAcceptor()).checkConstant(c);
	}
	
	@Check
	public void typeCheckMacro(Macro m) {
		new SpearTypeChecker(getMessageAcceptor()).checkMacro(m);
		new UnitChecker(getMessageAcceptor()).checkMacro(m);
	}
	
	@Check
	public void typeCheckFormalConstraint(FormalConstraint fc) {
		new SpearTypeChecker(getMessageAcceptor()).checkFormalConstraint(fc);
		new UnitChecker(getMessageAcceptor()).checkFormalConstraint(fc);
	}


}
