package slib.sglib.io.conf;

import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;

import slib.sglib.algo.utils.GAction;
import slib.utils.i.CheckableValidity;
import slib.utils.impl.ParametrableImpl;

public class GraphConf  extends ParametrableImpl implements CheckableValidity{

	URI uri;
	
	List<GDataConf> data;
	List<GAction> actions;

	public GraphConf(){
		data    = new LinkedList<GDataConf>();
		actions = new LinkedList<GAction>();
	}
	
	public GraphConf(URI uri){
		this.uri = uri;
	}
	


	public boolean isValid() {
		
		if(uri == null) return false;

		for(GDataConf dataFile : data){
			if(!dataFile.isValid()) return false;
		}

		for(GAction action : actions){
			if(!action.isValid()) return false;
		}
		return true;
	}
	
	public void addGDataConf(GDataConf conf){
		data.add(conf);
	}
	
	public void addGAction(GAction conf){
		actions.add(conf);
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public List<GDataConf> getData() {
		return data;
	}

	public void setData(List<GDataConf> data) {
		this.data = data;
	}

	public List<GAction> getActions() {
		return actions;
	}

	public void setActions(List<GAction> actions) {
		this.actions = actions;
	}


}
