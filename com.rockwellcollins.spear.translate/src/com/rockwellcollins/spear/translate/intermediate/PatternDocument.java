package com.rockwellcollins.spear.translate.intermediate;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import com.rockwellcollins.spear.Constant;
import com.rockwellcollins.spear.File;
import com.rockwellcollins.spear.Pattern;
import com.rockwellcollins.spear.TypeDef;
import com.rockwellcollins.spear.translate.transformations.PerformTransforms;

public class PatternDocument extends Document {

	public Map<String,Pattern> patterns = new HashMap<>();
	
	public Pattern getMain() {
		return patterns.get(mainName);
	}
	
	public PatternDocument(Pattern main) {
		this.mainName = main.getName();
		for(EObject o : FindPatternDependencies.instance(main).getObjects()) {
			if (o instanceof TypeDef) {
				TypeDef td = (TypeDef) o;
				typedefs.put(td.getName(),td);
			}

			if (o instanceof Constant) {
				Constant c = (Constant) o;
				constants.put(c.getName(),c);
			}
			
			if (o instanceof Pattern) {
				Pattern p = (Pattern) o;
				patterns.put(p.getName(),p);
			}
			
			if (o instanceof File) {
				File f = (File) o;
				files.put(f.getName(), f);
			}
		}
	}
	
	public PatternDocument(TypeDocument td) {
		this.mainName = td.pattern.getName();
		this.typedefs = new HashMap<>(td.typedefs);
		this.constants = new HashMap<>(td.constants);
		//add the singular pattern
		this.patterns.put(td.pattern.getName(), td.pattern);
	}
	
	public void transform() {
		try {
			this.renamed = PerformTransforms.apply(this);
		} catch (Exception e) {
			System.err.println("Error performing transformations.");
			e.printStackTrace();
			System.exit(-1);
		}		
	}
	
	public Map<String,String> mergeMaps() {
		Map<String,String> merged = new HashMap<>();
		merged.putAll(renamed.get(this.getMain()));
		
		typedefs.forEach((s,td) -> merged.putAll(renamed.get(td)));
		constants.forEach((s,c) -> merged.putAll(renamed.get(c)));
		return merged;
	}
}
