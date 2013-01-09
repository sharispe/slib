package slib.sglib.io.loader.csv;

/**
 *
 * @author seb
 */
public class CSV_StatementTemplate_Constraint {


	/**
     *
     */
    public StatementTemplate_Constraint_Type type;
	/**
     *
     */
    public StatementTemplateElement onElement;
	
	/**
     *
     * @param elem
     * @param type
     */
    public CSV_StatementTemplate_Constraint(StatementTemplateElement elem,
			StatementTemplate_Constraint_Type type) {
		
		this.type = type;
		this.onElement = elem;
	}
	
}
