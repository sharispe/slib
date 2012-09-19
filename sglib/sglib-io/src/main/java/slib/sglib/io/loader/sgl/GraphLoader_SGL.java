/*

Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

This software is a computer program whose purpose is to 
process semantic graphs.

This software is governed by the CeCILL  license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.

 */
 
 
package slib.sglib.io.loader.sgl;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.loader.IGraphLoader;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.impl.VertexTyped;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.impl.memory.GraphMemory;
import slib.sglib.model.repo.impl.DataRepository;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.ex.SGL_Exception;

/**
 * TODO Comment DO not Support transitivity and inverse definitions anymore 
 * @author Sebastien Harispe
 *
 */
public class GraphLoader_SGL implements IGraphLoader{

	private static final String EDGE_TYPE_FLAG	 = "EDGE_TYPE";
	private static final String VERTEX_TYPE_FLAG = "VERTEX_TYPE";
	private static final String VERTICES_FLAG 	 = "VERTICES";
	private static final String EDGES_FLAG 	 	 = "EDGES";

	private static final String ETYPE_ISA 	 	 = "is_a";
	private static final String VTYPE_CLASS 	 = "class";
	private static final String VTYPE_INSTANCE 	 = "instance";

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	DataRepository data;

	String filepath;

	G g;

	private int nbEdgeType;
	private int nbVertexType;
	private int nbVertices;
	private int nbEdges;

	URI[] edgeTypes;

	private VType[] vertexTypes;
	private V[] 	  vertices;
	private int[] 	  eTypeInverseMap;

	boolean onEdgeType 	 = false;
	boolean onVertexType = false;
	boolean onVertices 	 = false;
	boolean onEdges 	 = false;

	int nbEdgeTypesLoaded 	= 0;
	int nbVertexTypesLoaded = 0;
	int nbVerticesLoaded 	= 0;
	int nbEdgesLoaded 		= 0;

	public G load(GraphConf conf) throws SGL_Exception{
		return GraphLoaderGeneric.load(conf);
	}
	
	public void populate(GDataConf conf, G g) throws SGL_Exception {
		process(conf, g);
	}
	
	public void process(GDataConf conf, G graph) throws SGL_Exception{
		
		this.g = graph;
		data = DataRepository.getSingleton();
		
		
		this.filepath = conf.getLoc();
		logger.info("Loading SGTK spec from "+filepath);

		try {

			FileInputStream fstream = new FileInputStream(filepath);
			DataInputStream in 		= new DataInputStream(fstream);
			BufferedReader br 		= new BufferedReader(new InputStreamReader(in));

			String line;

			boolean init = false;



			while ((line = br.readLine()) != null)   {

				line = line.trim();
				if(line.isEmpty() || line.startsWith("#"))
					continue;

				if(!init){
					initSearch(line);
					init = true;
				}
				else if(onEdgeType){
					loadEdgeType(line);
				}
				else if(onVertexType){
					loadVertexType(line);
				}
				else if(onVertices){
					loadVertex(line);
				}
				else if(onEdges){
					loadEdge(line);
				}
				else{
					checkFlag(line);
				}

			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new SGL_Ex_Critic(e.getMessage());
		}
	}


	private void loadEdge(String line) throws SGL_Ex_Critic {
		String[] data = line.split("\t");

		if(data.length == 3){


			int idVsrc = Integer.parseInt(data[0]);
			int idVtgt = Integer.parseInt(data[1]);
			int idType = Integer.parseInt(data[2]);

			URI type 	  = edgeTypes[idType];

			g.addE(vertices[idVsrc],vertices[idVtgt],type);

			nbEdgesLoaded++;
		}
		else if(nbEdgesLoaded == nbEdges){
			onEdges= false;
			logger.debug("Edges Loaded");
			checkFlag(line);
		}
	}


	private void loadVertex(String line) throws SGL_Exception {
		String[] data = line.split("\t");

		if(data.length == 3){

			int id 	   = Integer.parseInt(data[0]);
			int idType = Integer.parseInt(data[2]);

			URI u = this.data.createURI(data[1]);
			
			V v;
			
			if(vertexTypes[idType] == VType.CLASS)
				v = new VertexTyped(g, u,VType.CLASS);
			
			else if(vertexTypes[idType] == VType.INSTANCE)
				v = new VertexTyped(g, u,VType.INSTANCE);
			else
				throw new SGL_Exception("Vertex type not supported "+vertexTypes[idType]);

			if(id >= vertices.length)
				throw new SGL_Ex_Critic("Incoherent vType id : "+id+" max "+(nbVertexType-1)+" ...");

			vertices[id] = v;
			g.addV(v);

			nbVerticesLoaded++;
		}
		else if(nbVerticesLoaded == nbVertices){
			onVertices= false;
			logger.debug("Vertices Loaded");
			checkFlag(line);
		}
	}


	private void loadEdgeType(String line) throws SGL_Exception {
		
		String[] data = line.split("\t");

		if(data.length == 4){

			int id 		   = Integer.parseInt(data[0]);
			int idInverse  = Integer.parseInt(data[2]);

			URI eType;

			if(data[1].equals(ETYPE_ISA))
				eType = RDFS.SUBCLASSOF;
			
			else{
				eType = this.data.eTypes.createPURI( data[1] );
						
				if(id >= edgeTypes.length)
					throw new SGL_Ex_Critic("Incoherent eType id : "+id+" max "+(nbEdgeType-1)+" ...");
			}
			edgeTypes[id] 		= eType;
			eTypeInverseMap[id] = idInverse;

			nbEdgeTypesLoaded++;
		}
		else if(nbEdgeTypesLoaded == nbEdgeType){
			onEdgeType = false;
			logger.debug("EdgeTypes Loaded");
			checkFlag(line);
		}
	}



	private void loadVertexType(String line) throws SGL_Ex_Critic {
		String[] data = line.split("\t");

		if(data.length == 2){

			VType vType;

			if(data[1].equals(VTYPE_CLASS))
				vType = VType.CLASS;
			else if(data[1].equals(VTYPE_INSTANCE))
				vType = VType.INSTANCE;
			else
				throw new SGL_Ex_Critic("Unsupported type "+data[1]);

			int id = Integer.parseInt(data[0]);

			if(id >= vertexTypes.length)
				throw new SGL_Ex_Critic("Incoherent vType id : "+id+" max "+(nbVertexType-1)+" ...");

			vertexTypes[id] = vType;
			
			nbVertexTypesLoaded++;
		}
		else if(nbVertexTypesLoaded == nbVertexType){
			onVertexType = false;
			logger.debug("VertexTypes Loaded");
			checkFlag(line);
		}
	}


	private void checkFlag(String line) {

		if(line.equals(EDGE_TYPE_FLAG)){
			onEdgeType = true;
			logger.debug("Loading EdgeTypes ");
			edgeTypes = new URI[nbEdgeType];
		}
		else if(line.equals(VERTEX_TYPE_FLAG)){
			onVertexType = true;
			logger.debug("Loading vertexTypes ");
			vertexTypes = new VType[nbEdgeType];
		}
		else if(line.equals(VERTICES_FLAG)){
			onVertices = true;
			logger.debug("Loading Vertices ");
			vertices   = new V[nbVertices];
		}
		else if(line.equals(EDGES_FLAG)){
			logger.debug("Loading Edges ");
			onEdges    = true;
		}
	}


	private void initSearch(String line) throws SGL_Ex_Critic {
		logger.debug("Header: "+line);
		String[] expS = line.split("\t");

		if(expS.length != 5){
			throw new SGL_Ex_Critic("Incorrect Header require: GraphURI,EdgeTypeNB,VertexTypesNB,VerticesNB,EdgesNB(separate by tabs)");
		}
		else{
			try{
				if(g == null){
					logger.info("Create graph "+expS[0]);
					g = new GraphMemory(data.createURI(expS[0]));
				}
				else{
					logger.info("Populate graph "+g.getURI());
				}
				nbEdgeType 	 	= Integer.parseInt(expS[1]);
				eTypeInverseMap = new int[nbEdgeType];
				nbVertexType 	= Integer.parseInt(expS[2]);
				nbVertices 		= Integer.parseInt(expS[3]);
				nbEdges    		= Integer.parseInt(expS[4]);
			}
			catch(Exception e){
				throw new SGL_Ex_Critic("Invalide value specified in header "+line+"\n"+e.getMessage());
			}
		}
	}


//	public static void main(String[] args) throws SGL_Exception, InterruptedException {
//
//		String path = System.getProperty("user.dir")+"/data/test/graph/sgl/";
//
//		String go = path+"g.sgl";
//
//
//		try {
//			GraphLoader_SGL loader = new GraphLoader_SGL();
//			GraphConf conf = new GraphConf(GFormat.SGL, go);
//			G g = loader.load(conf);
//
//			System.out.println(g.toString());
//
//			String gv = GraphPlotter_Graphviz.plot(g,null, false);
//			System.out.println(gv);
//		} catch (SGL_Ex_Critic e) {
//			e.printStackTrace();
//			System.err.println(e.getMessage());
//		}
//		
//	}



}