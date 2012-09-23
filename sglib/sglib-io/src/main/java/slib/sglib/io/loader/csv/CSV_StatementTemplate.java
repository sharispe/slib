package slib.sglib.io.loader.csv;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

public class CSV_StatementTemplate{

	int src_id;
	int target_id;
	URI predicate ;
	
	Set<CSV_StatementTemplate_Constraint> constraints;

	public CSV_StatementTemplate(int src_id, int target_id, URI predicate){
		this.src_id     = src_id;
		this.target_id  = target_id;
		this.predicate  = predicate;
		
		constraints = new HashSet<CSV_StatementTemplate_Constraint>();
	}
	
	public void addConstraint(CSV_StatementTemplate_Constraint c){
		constraints.add(c);
	}
}