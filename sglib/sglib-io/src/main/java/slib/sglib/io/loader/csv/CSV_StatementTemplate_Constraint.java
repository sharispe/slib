package slib.sglib.io.loader.csv;

public class CSV_StatementTemplate_Constraint {


	public StatementTemplate_Constraint_Type type;
	public StatementTemplateElement onElement;
	
	public CSV_StatementTemplate_Constraint(StatementTemplateElement elem,
			StatementTemplate_Constraint_Type type) {
		
		this.type = type;
		this.onElement = elem;
	}
	
}
