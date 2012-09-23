package slib.sglib.io.loader.csv;

import slib.sglib.model.graph.elements.type.VType;

public class CSV_Mapping{

	int id;
	String prefix;
	VType type;

	public CSV_Mapping(int id, VType type, String prefix){
		this.id     = id;
		this.type   = type;
		this.prefix = prefix;
	}
}