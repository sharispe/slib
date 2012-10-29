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
package slib.sml.sm.core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import slib.sml.sm.core.measures.framework.core.measures.impl.Sim_Framework_3W_Jaccard;
import slib.sml.sm.core.measures.framework.core.measures.impl.Sim_Framework_Commonalities;
import slib.sml.sm.core.measures.framework.core.measures.impl.Sim_Framework_Dice_1945;
import slib.sml.sm.core.measures.framework.core.measures.impl.Sim_Framework_Jaccard_1901;
import slib.sml.sm.core.measures.framework.core.measures.impl.Sim_Framework_Nei_Li;
import slib.sml.sm.core.measures.framework.core.measures.impl.Sim_Framework_Sokal_Sneath_I;
import slib.sml.sm.core.measures.framework.core.measures.impl.Sim_Framework_Tversky_1977;
import slib.sml.sm.core.measures.framework.impl.set.GraphRepresentationAsSet;
import slib.sml.sm.core.measures.framework.impl.set.OperatorsSet;
import slib.sml.sm.core.measures.framework.impl.set.OperatorsSet_IC;
import slib.sml.sm.core.measures.framework.impl.set.OperatorsSet_ICA_Grasm;
import slib.sml.sm.core.measures.framework.impl.set.OperatorsSet_ICA_Grasm_IC;
import slib.sml.sm.core.measures.framework.impl.set.OperatorsSet_MAX_IC;
import slib.sml.sm.core.measures.framework.impl.set.OperatorsSet_MICA;
import slib.sml.sm.core.measures.framework.impl.set.OperatorsSet_MICA_IC;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Bader_2003;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Batet_2010;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Braun_Blanquet_1932;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Dice_1945;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Jaccard_1901;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Knappe_2004;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Korbel_2002;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Maryland_Bridge_2003;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Ochiai_1957;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Simpson_1960;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Sokal_Sneath_1963;
import slib.sml.sm.core.measures.graph.framework.dag.Sim_Framework_DAG_Set_Tversky_1977;
import slib.sml.sm.core.measures.graph.groupwise.dag.Sim_groupwise_DAG_Ali_Deane;
import slib.sml.sm.core.measures.graph.groupwise.dag.Sim_groupwise_DAG_GIC;
import slib.sml.sm.core.measures.graph.groupwise.dag.Sim_groupwise_DAG_LP;
import slib.sml.sm.core.measures.graph.groupwise.dag.Sim_groupwise_DAG_Lee_2004;
import slib.sml.sm.core.measures.graph.groupwise.dag.Sim_groupwise_DAG_NTO;
import slib.sml.sm.core.measures.graph.groupwise.dag.Sim_groupwise_DAG_TO;
import slib.sml.sm.core.measures.graph.groupwise.dag.Sim_groupwise_DAG_UI;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_pairwise_DAG_edge_Kyogoku_basic_2011;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_pairwise_DAG_edge_Leacock_Chodorow_1998;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_pairwise_DAG_edge_Li_2003;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_pairwise_DAG_edge_Pekar_Staab_2002;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_pairwise_DAG_edge_Rada_1989;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_pairwise_DAG_edge_Rada_LCA_1989;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_pairwise_DAG_edge_Resnik_1995;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_pairwise_DAG_edge_Slimani_2006;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_pairwise_DAG_edge_Stojanovic_2001;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_pairwise_DAG_edge_Wu_Palmer_1994;
import slib.sml.sm.core.measures.graph.pairwise.dag.hybrid.Sim_pairwise_DAG_hybrid_Ranwez_2006;
import slib.sml.sm.core.measures.graph.pairwise.dag.hybrid.Sim_pairwise_DAG_hybrid_Wang_2007;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_GL;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_GL_GraSM;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Jaccard_3W_IC;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Jaccard_IC;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Jiang_Conrath_1997;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Lin_1998;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Lin_1998_GraSM;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Mazandu_2012;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Resnik_1995;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Resnik_1995_Ancestors;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Resnik_1995_Descendants;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Resnik_1995_GraSM;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Schlicker_2006_SimRel;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Schlicker_3WJaccard_SimRel;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Schlicker_GL_SimRel;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Schlicker_Jaccard_SimRel;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Schlicker_Tversky_SimRel;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Sim_IC_2010;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Tversky_IC;
import slib.sml.sm.core.measures.others.groupwise.add_on.Sim_groupwise_Average;
import slib.sml.sm.core.measures.others.groupwise.add_on.Sim_groupwise_BestMatchAverage;
import slib.sml.sm.core.measures.others.groupwise.add_on.Sim_groupwise_BestMatchMax;
import slib.sml.sm.core.measures.others.groupwise.add_on.Sim_groupwise_MAX_NORMALIZED_GOSIM;
import slib.sml.sm.core.measures.others.groupwise.add_on.Sim_groupwise_Max;
import slib.sml.sm.core.measures.others.groupwise.add_on.Sim_groupwise_Min;
import slib.sml.sm.core.measures.others.groupwise.standalone.Sim_groupwise_Random;
import slib.sml.sm.core.measures.others.groupwise.standalone.vector.VectorSpaceModel;
import slib.sml.sm.core.measures.others.pairwise.Sim_pairwise_Random;
import slib.sml.sm.core.metrics.ic.annot.IC_annot_resnik_1995;
import slib.sml.sm.core.metrics.ic.annot.IC_annot_resnik_1995_Normalized;
import slib.sml.sm.core.metrics.ic.annot.IC_probOccurence_propagatted;
import slib.sml.sm.core.metrics.ic.annot.IDF;
import slib.sml.sm.core.metrics.ic.topo.ICi_depth_max_nonlinear;
import slib.sml.sm.core.metrics.ic.topo.ICi_depth_min_nonlinear;
import slib.sml.sm.core.metrics.ic.topo.ICi_mazandu_2012;
import slib.sml.sm.core.metrics.ic.topo.ICi_probOccurence;
import slib.sml.sm.core.metrics.ic.topo.ICi_probOccurence_propagatted;
import slib.sml.sm.core.metrics.ic.topo.ICi_resnik_1995;
import slib.sml.sm.core.metrics.ic.topo.ICi_resnik_unpropagatted_1995;
import slib.sml.sm.core.metrics.ic.topo.ICi_sanchez_2011_a;
import slib.sml.sm.core.metrics.ic.topo.ICi_sanchez_2011_b;
import slib.sml.sm.core.metrics.ic.topo.ICi_seco_2004;
import slib.sml.sm.core.metrics.ic.topo.ICi_zhou_2008;

/**
 * Class used to defined Semantic Measures constants such as measures flags.
 *
 * @FIXME requires to be split in a smart way.
 *
 * @author Sebastien Harispe
 *
 */
public final class SMConstants {

    //-------------------------------------------------------------------------------
    // Framework
    //-------------------------------------------------------------------------------
    public static final String SIM_FRAMEWORK_COMMONALITIES = Sim_Framework_Commonalities.class.getName();
    public static final String SIM_FRAMEWORK_JACCARD_1901 = Sim_Framework_Jaccard_1901.class.getName();
    public static final String SIM_FRAMEWORK_DICE_1945 = Sim_Framework_Dice_1945.class.getName();
    public static final String SIM_FRAMEWORK_TVERSKY_1977 = Sim_Framework_Tversky_1977.class.getName();
    public static final String SIM_FRAMEWORK_3W_JACCARD_1901 = Sim_Framework_3W_Jaccard.class.getName();
    public static final String SIM_FRAMEWORK_NEI_LI = Sim_Framework_Nei_Li.class.getName();
    public static final String SIM_FRAMEWORK_SOKAL_SNEATH_I = Sim_Framework_Sokal_Sneath_I.class.getName();
    public static final Map<String, String> SIM_FRAMEWORK = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("SIM_FRAMEWORK_COMMONALITIES", SIM_FRAMEWORK_COMMONALITIES);
            put("SIM_FRAMEWORK_JACCARD_1901", SIM_FRAMEWORK_JACCARD_1901);
            put("SIM_FRAMEWORK_DICE_1945", SIM_FRAMEWORK_DICE_1945);
            put("SIM_FRAMEWORK_TVERSKY_1977", SIM_FRAMEWORK_TVERSKY_1977);

            put("SIM_FRAMEWORK_3W_JACCARD_1901", SIM_FRAMEWORK_3W_JACCARD_1901);
            put("SIM_FRAMEWORK_NEI_LI", SIM_FRAMEWORK_NEI_LI);
            put("SIM_FRAMEWORK_SOKAL_SNEATH_I", SIM_FRAMEWORK_SOKAL_SNEATH_I);
        }
    };
    public static final String SET_DAG = GraphRepresentationAsSet.class.getName();
    public static final Map<String, String> representation = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("SET_DAG", SET_DAG);
        }
    };
    public static final String OPERATOR_SET = OperatorsSet.class.getName();
    public static final String OPERATOR_SET_ICA_GRASM = OperatorsSet_ICA_Grasm.class.getName();
    public static final String OPERATOR_SET_MICA = OperatorsSet_MICA.class.getName();
    public static final String OPERATOR_SET_MAX_IC = OperatorsSet_MAX_IC.class.getName();
    public static final String OPERATOR_SET_IC = OperatorsSet_IC.class.getName();
    public static final String OPERATOR_SET_ICA_GRASM_IC = OperatorsSet_ICA_Grasm_IC.class.getName();
    public static final String OPERATOR_SET_MICA_IC = OperatorsSet_MICA_IC.class.getName();
    public static final Map<String, String> operators = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("SET", OPERATOR_SET);
            put("SET_MAX_IC", OPERATOR_SET_MAX_IC);
            put("SET_IC", OPERATOR_SET_IC);
            put("SET_ICA_GRASM", OPERATOR_SET_ICA_GRASM);
            put("SET_ICA_GRASM_IC", OPERATOR_SET_ICA_GRASM_IC);
            put("SET_MICA", OPERATOR_SET_MICA);
            put("SET_MICA_IC", OPERATOR_SET_MICA_IC);
        }
    };
    //-------------------------------------------------------------------------------
    // Framework Set implementation
    //-------------------------------------------------------------------------------
    public static final String SIM_FRAMEWORK_DAG_SET_BADER_2003 = Sim_Framework_DAG_Set_Bader_2003.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_BATET_2010 = Sim_Framework_DAG_Set_Batet_2010.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_BRAUN_BLANQUET_1932 = Sim_Framework_DAG_Set_Braun_Blanquet_1932.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_DICE_1945 = Sim_Framework_DAG_Set_Dice_1945.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_JACCARD_1901 = Sim_Framework_DAG_Set_Jaccard_1901.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_KNAPPE_2004 = Sim_Framework_DAG_Set_Knappe_2004.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_KORBEL_2002 = Sim_Framework_DAG_Set_Korbel_2002.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_MARYLAND_BRIDGE_2003 = Sim_Framework_DAG_Set_Maryland_Bridge_2003.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_OCHIAI_1957 = Sim_Framework_DAG_Set_Ochiai_1957.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_SIMPSON_1960 = Sim_Framework_DAG_Set_Simpson_1960.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_SOKAL_SNEATH_1963 = Sim_Framework_DAG_Set_Sokal_Sneath_1963.class.getName();
    public static final String SIM_FRAMEWORK_DAG_SET_TVERSKY_1977 = Sim_Framework_DAG_Set_Tversky_1977.class.getName();
    public static final Map<String, String> SIM_FRAMEWORK_DAG_SET_BASED = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("SIM_FRAMEWORK_DAG_SET_BADER_2003", SIM_FRAMEWORK_DAG_SET_BADER_2003);
            put("SIM_FRAMEWORK_DAG_SET_BATET_2010", SIM_FRAMEWORK_DAG_SET_BATET_2010);
            put("SIM_FRAMEWORK_DAG_SET_BRAUN_BLANQUET_1932", SIM_FRAMEWORK_DAG_SET_BRAUN_BLANQUET_1932);
            put("SIM_FRAMEWORK_DAG_SET_DICE_1945", SIM_FRAMEWORK_DAG_SET_DICE_1945);
            put("SIM_FRAMEWORK_DAG_SET_JACCARD_1901", SIM_FRAMEWORK_DAG_SET_JACCARD_1901);
            put("SIM_FRAMEWORK_DAG_SET_KNAPPE_2004", SIM_FRAMEWORK_DAG_SET_KNAPPE_2004);
            put("SIM_FRAMEWORK_DAG_SET_KORBEL_2002", SIM_FRAMEWORK_DAG_SET_KORBEL_2002);
            put("SIM_FRAMEWORK_DAG_SET_MARYLAND_BRIDGE_2003", SIM_FRAMEWORK_DAG_SET_MARYLAND_BRIDGE_2003);
            put("SIM_FRAMEWORK_DAG_SET_OCHIAI_1957", SIM_FRAMEWORK_DAG_SET_OCHIAI_1957);
            put("SIM_FRAMEWORK_DAG_SET_SIMPSON_1960", SIM_FRAMEWORK_DAG_SET_SIMPSON_1960);
            put("SIM_FRAMEWORK_DAG_SET_SOKAL_SNEATH_1963", SIM_FRAMEWORK_DAG_SET_SOKAL_SNEATH_1963);
            put("SIM_FRAMEWORK_DAG_SET_TVERSKY_1977", SIM_FRAMEWORK_DAG_SET_TVERSKY_1977);
        }
    };
    //-------------------------------------------------------------------------------
    // Groupwise
    //-------------------------------------------------------------------------------
    public static final String SIM_GROUPWISE_MAX_NORMALIZED_GOSIM = Sim_groupwise_MAX_NORMALIZED_GOSIM.class.getName();
    public static final String SIM_GROUPWISE_AVERAGE_NORMALIZED_GOSIM = Sim_groupwise_MAX_NORMALIZED_GOSIM.class.getName();
    public static final String SIM_GROUPWISE_AVERAGE = Sim_groupwise_Average.class.getName();
    public static final String SIM_GROUPWISE_BEST_MATCH_AVERAGE = Sim_groupwise_BestMatchAverage.class.getName();
    public static final String SIM_GROUPWISE_BEST_MATCH_MAX = Sim_groupwise_BestMatchMax.class.getName();
    public static final String SIM_GROUPWISE_MAX = Sim_groupwise_Max.class.getName();
    public static final String SIM_GROUPWISE_MIN = Sim_groupwise_Min.class.getName();
    public static final String SIM_GROUPWISE_RANDOM = Sim_groupwise_Random.class.getName();
    public static final Map<String, String> SIM_GROUPWISE_ADD_ON = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("SIM_GROUPWISE_MAX", SIM_GROUPWISE_MAX);
            put("SIM_GROUPWISE_MAX_NORMALIZED_GOSIM", SIM_GROUPWISE_MAX_NORMALIZED_GOSIM);

            put("SIM_GROUPWISE_MIN", SIM_GROUPWISE_MIN);
            put("SIM_GROUPWISE_AVERAGE", SIM_GROUPWISE_AVERAGE);
            put("SIM_GROUPWISE_AVERAGE_NORMALIZED_GOSIM", SIM_GROUPWISE_AVERAGE_NORMALIZED_GOSIM);
            put("SIM_GROUPWISE_BMA", SIM_GROUPWISE_BEST_MATCH_AVERAGE);
            put("SIM_GROUPWISE_BMM", SIM_GROUPWISE_BEST_MATCH_MAX);
        }
    };
    public static final String SIM_GROUPWISE_DAG_GIC_FLAG = "SIM_GROUPWISE_DAG_GIC";
    public static final String SIM_GROUPWISE_DAG_GIC = Sim_groupwise_DAG_GIC.class.getName();
    public static final String SIM_GROUPWISE_DAG_LEE_2004 = Sim_groupwise_DAG_Lee_2004.class.getName();
    public static final String SIM_GROUPWISE_DAG_LP = Sim_groupwise_DAG_LP.class.getName();
    public static final String SIM_GROUPWISE_DAG_NTO = Sim_groupwise_DAG_NTO.class.getName();
    public static final String SIM_GROUPWISE_DAG_TO = Sim_groupwise_DAG_TO.class.getName();
    public static final String SIM_GROUPWISE_DAG_UI = Sim_groupwise_DAG_UI.class.getName();
    public static final String SIM_GROUPWISE_DAG_ALI_DEANE = Sim_groupwise_DAG_Ali_Deane.class.getName();
    public static final String SIM_GROUPWISE_SVM = VectorSpaceModel.class.getName();
    public static final Map<String, String> SIM_GROUPWISE_DAG = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(SIM_GROUPWISE_DAG_GIC_FLAG, SIM_GROUPWISE_DAG_GIC);
            put("SIM_GROUPWISE_DAG_LEE_2004", SIM_GROUPWISE_DAG_LEE_2004);
            put("SIM_GROUPWISE_DAG_LP", SIM_GROUPWISE_DAG_LP);
            put("SIM_GROUPWISE_DAG_NTO", SIM_GROUPWISE_DAG_NTO);
            put("SIM_GROUPWISE_DAG_TO", SIM_GROUPWISE_DAG_TO);
            put("SIM_GROUPWISE_DAG_UI", SIM_GROUPWISE_DAG_UI);
            put("SIM_GROUPWISE_DAG_ALI_DEANE", SIM_GROUPWISE_DAG_ALI_DEANE);
            put("SIM_GROUPWISE_SVM", SIM_GROUPWISE_SVM); // TODO move groupwise standalone
            putAll(SIM_FRAMEWORK_DAG_SET_BASED);
        }
    };
    public static final Map<String, String> SIM_GROUPWISE_OTHERS = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("SIM_GROUPWISE_RANDOM", SIM_GROUPWISE_RANDOM);
        }
    };
    public static final HashMap<String, String> groupwiseMeasureMapping = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            putAll(SIM_GROUPWISE_ADD_ON);
            putAll(SIM_GROUPWISE_DAG);
            putAll(SIM_GROUPWISE_OTHERS);
            putAll(SIM_FRAMEWORK);
        }
    };
    public static final HashSet<String> GROUPWISE_MEASURE_FLAGS = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {
            addAll(groupwiseMeasureMapping.keySet());
        }
    };

    public static String groupwiseClassName(String methodLabel) {
        return groupwiseMeasureMapping.get(methodLabel);
    }
    //-------------------------------------------------------------------------------
    // Pairwise
    //-------------------------------------------------------------------------------

    /*
     * -1 reserved flag for error
     */
    public final static int NO_CORRESPONDANCE = -1;
    public final static int PAIRWISE_GENERAL = 0;
    public final static int PAIRWISE_DAG_NODE_BASED = 1;
    public final static int PAIRWISE_DAG_EDGE_BASED = 2;
    public final static int PAIRWISE_DAG_HYBRID_BASED = 3;
    public final static int PAIRWISE_DAG_SET_BASED = 4;
    // GENERAL
    public static final String SIM_PAIRWISE_RANDOM = Sim_pairwise_Random.class.getName();
    public static final Map<String, String> SIM_PAIRWISE_GENERAL = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("SIM_PAIRWISE_RANDOM", SIM_PAIRWISE_RANDOM);
        }
    };
    // EDGE BASED -----------------------------------------------------------------------------------
    public static final String SIM_PAIRWISE_DAG_EDGE_KYOGOKU_BASIC_2011 = Sim_pairwise_DAG_edge_Kyogoku_basic_2011.class.getName();
    public static final String SIM_PAIRWISE_DAG_EDGE_LEACOCK_CHODOROW_1998 = Sim_pairwise_DAG_edge_Leacock_Chodorow_1998.class.getName();
    public static final String SIM_PAIRWISE_DAG_EDGE_LI_2003 = Sim_pairwise_DAG_edge_Li_2003.class.getName();
    public static final String SIM_PAIRWISE_DAG_EDGE_PEKAR_STAAB_2002 = Sim_pairwise_DAG_edge_Pekar_Staab_2002.class.getName();
    public static final String SIM_PAIRWISE_DAG_EDGE_RADA_1989 = Sim_pairwise_DAG_edge_Rada_1989.class.getName();
    public static final String SIM_PAIRWISE_DAG_EDGE_RADA_LCA_1989 = Sim_pairwise_DAG_edge_Rada_LCA_1989.class.getName();
    public static final String SIM_PAIRWISE_DAG_EDGE_RESNIK_1995 = Sim_pairwise_DAG_edge_Resnik_1995.class.getName();
    public static final String SIM_PAIRWISE_DAG_EDGE_SLIMANI_2006 = Sim_pairwise_DAG_edge_Slimani_2006.class.getName();
    public static final String SIM_PAIRWISE_DAG_EDGE_STOJANOVIC_2001 = Sim_pairwise_DAG_edge_Stojanovic_2001.class.getName();
    public static final String SIM_PAIRWISE_DAG_EDGE_WU_PALMER_1994 = Sim_pairwise_DAG_edge_Wu_Palmer_1994.class.getName();
    public static final Map<String, String> SIM_PAIRWISE_DAG_EDGE_BASED = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("SIM_PAIRWISE_DAG_EDGE_KYOGOKU_BASIC_2011", SIM_PAIRWISE_DAG_EDGE_KYOGOKU_BASIC_2011);
            put("SIM_PAIRWISE_DAG_EDGE_LEACOCK_CHODOROW_1998", SIM_PAIRWISE_DAG_EDGE_LEACOCK_CHODOROW_1998);
            put("SIM_PAIRWISE_DAG_EDGE_LI_2003", SIM_PAIRWISE_DAG_EDGE_LI_2003);
            put("SIM_PAIRWISE_DAG_EDGE_PEKAR_STAAB_2002", SIM_PAIRWISE_DAG_EDGE_PEKAR_STAAB_2002);
            put("SIM_PAIRWISE_DAG_EDGE_RADA_1989", SIM_PAIRWISE_DAG_EDGE_RADA_1989);
            put("SIM_PAIRWISE_DAG_EDGE_RADA_LCA_1989", SIM_PAIRWISE_DAG_EDGE_RADA_LCA_1989);
            put("SIM_PAIRWISE_DAG_EDGE_RESNIK_1995", SIM_PAIRWISE_DAG_EDGE_RESNIK_1995);
            put("SIM_PAIRWISE_DAG_EDGE_SLIMANI_2006", SIM_PAIRWISE_DAG_EDGE_SLIMANI_2006);
            put("SIM_PAIRWISE_DAG_EDGE_STOJANOVIC_2001", SIM_PAIRWISE_DAG_EDGE_STOJANOVIC_2001);
            put("SIM_PAIRWISE_DAG_EDGE_WU_PALMER_1994", SIM_PAIRWISE_DAG_EDGE_WU_PALMER_1994);
        }
    };
    // NODE BASED -----------------------------------------------------------------------------------
    // - Information Content methods 
    public static final String FLAG_ICI_RESNIK_UNPROPAGATED_1995 = "ICI_RESNIK_UNPROPAGATED_1995";
    public static final String FLAG_ICI_RESNIK_1995 = "ICI_RESNIK_1995";
    public static final String FLAG_ICI_SANCHEZ_2011_a = "ICI_SANCHEZ_2011_a";
    public static final String FLAG_ICI_SANCHEZ_2011_b = "ICI_SANCHEZ_2011_b";
    public static final String FLAG_ICI_SECO_2004 = "ICI_SECO_2004";
    public static final String FLAG_ICI_ZHOU_2008 = "ICI_ZHOU_2008";
    public static final String FLAG_ICI_MAZANDU_2012 = "ICI_MAZANDU_2012";
    public static final String FLAG_ICI_DEPTH_MAX_NONLINEAR = "ICI_DEPTH_MAX_NONLINEAR";
    public static final String FLAG_ICI_DEPTH_MIN_NONLINEAR = "ICI_DEPTH_MIN_NONLINEAR";
    public static final String FLAG_ICI_PROB_OCCURENCE = "IC_PROB_OCCURENCE";
    public static final String FLAG_ICI_PROB_OCCURENCE_PROPAGATED = "ICI_PROB_OCCURENCE_PROPAGATED";
    public static final String FLAG_IC_ANNOT_RESNIK_1995 = "IC_ANNOT_RESNIK_1995";
    public static final String FLAG_IC_ANNOT_RESNIK_1995_NORMALIZED = "IC_ANNOT_RESNIK_1995_NORMALIZED";
    public static final String FLAG_IC_PROB_OCCURENCE_PROPAGATED = "IC_PROB_OCCURENCE_PROPAGATED";
    public static final String FLAG_IC_IDF = "IC_ANNOT_IDF";
    public static final String ICI_RESNIK_UNPROPAGATED_1995 = ICi_resnik_unpropagatted_1995.class.getName();
    public static final String ICI_RESNIK_1995 = ICi_resnik_1995.class.getName();
    public static final String ICI_SANCHEZ_2011_a = ICi_sanchez_2011_a.class.getName();
    public static final String ICI_SANCHEZ_2011_b = ICi_sanchez_2011_b.class.getName();
    public static final String ICI_SECO_2004 = ICi_seco_2004.class.getName();
    public static final String ICI_ZHOU_2008 = ICi_zhou_2008.class.getName();
    public static final String ICI_DEPTH_MAX_NONLINEAR = ICi_depth_max_nonlinear.class.getName();
    public static final String ICI_DEPTH_MIN_NONLINEAR = ICi_depth_min_nonlinear.class.getName();
    public static final String ICI_MAZANDU_2012 = ICi_mazandu_2012.class.getName();
    public static final String ICI_PROB_OCCURENCE = ICi_probOccurence.class.getName();
    public static final String ICI_PROB_OCCURENCE_PROPAGATED = ICi_probOccurence_propagatted.class.getName();
    public static final String IC_ANNOT_RESNIK_1995 = IC_annot_resnik_1995.class.getName();
    public static final String IC_ANNOT_IDF = IDF.class.getName();
    public static final String IC_ANNOT_RESNIK_1995_NORMALIZED = IC_annot_resnik_1995_Normalized.class.getName();
    public static final String IC_PROB_OCCURENCE_PROPAGATED = IC_probOccurence_propagatted.class.getName();
    public static final Map<String, String> SIM_PAIRWISE_DAG_NODE_IC_INTRINSIC = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(FLAG_ICI_RESNIK_UNPROPAGATED_1995, ICI_RESNIK_UNPROPAGATED_1995);
            put(FLAG_ICI_RESNIK_1995, ICI_RESNIK_1995);
            put(FLAG_ICI_SANCHEZ_2011_a, ICI_SANCHEZ_2011_a);
            put(FLAG_ICI_SANCHEZ_2011_b, ICI_SANCHEZ_2011_b);
            put(FLAG_ICI_SECO_2004, ICI_SECO_2004);
            put(FLAG_ICI_ZHOU_2008, ICI_ZHOU_2008);
            put(FLAG_ICI_DEPTH_MAX_NONLINEAR, ICI_DEPTH_MAX_NONLINEAR);
            put(FLAG_ICI_DEPTH_MIN_NONLINEAR, ICI_DEPTH_MIN_NONLINEAR);
            put(FLAG_ICI_PROB_OCCURENCE, ICI_PROB_OCCURENCE);
            put(FLAG_ICI_PROB_OCCURENCE_PROPAGATED, ICI_PROB_OCCURENCE_PROPAGATED);
            put(FLAG_ICI_MAZANDU_2012, ICI_MAZANDU_2012);
        }
    };
    public static final Map<String, String> SIM_PAIRWISE_DAG_NODE_IC_ANNOT = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(FLAG_IC_ANNOT_RESNIK_1995, IC_ANNOT_RESNIK_1995);
            put(FLAG_IC_ANNOT_RESNIK_1995_NORMALIZED, IC_ANNOT_RESNIK_1995_NORMALIZED);
            put(FLAG_IC_PROB_OCCURENCE_PROPAGATED, IC_PROB_OCCURENCE_PROPAGATED);
        }
    };
    // Methods 
    public static final String FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_2006 = "SIM_PAIRWISE_DAG_NODE_SCHLICKER_2006";
    public static final String FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_JACCARD = "SIM_PAIRWISE_DAG_NODE_SCHLICKER_JACCARD";
    public static final String FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_3WJACCARD = "SIM_PAIRWISE_DAG_NODE_SCHLICKER_3WJACCARD";
    public static final String FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_TVERSKY = "SIM_PAIRWISE_DAG_NODE_SCHLICKER_TVERSKY";
    public static final String FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_GL = "SIM_PAIRWISE_DAG_NODE_SCHLICKER_GL";
    public static final String FLAG_SIM_PAIRWISE_DAG_NODE_GL = "SIM_PAIRWISE_DAG_NODE_GL";
    public static final String FLAG_SIM_PAIRWISE_DAG_NODE_GL_GRASM = "SIM_PAIRWISE_DAG_NODE_GL_GRASM";
    public static final String FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998 = "SIM_PAIRWISE_DAG_NODE_LIN_1998";
    
    public static final String SIM_PAIRWISE_DAG_NODE_TVERSKY_IC = Sim_pairwise_DAG_node_Tversky_IC.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_LIN_1998 = Sim_pairwise_DAG_node_Lin_1998.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_JACCARD_IC = Sim_pairwise_DAG_node_Jaccard_IC.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_JACCARD_3W_IC = Sim_pairwise_DAG_node_Jaccard_3W_IC.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_JIANG_CONRATH_1997 = Sim_pairwise_DAG_node_Jiang_Conrath_1997.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_RESNIK_1995 = Sim_pairwise_DAG_node_Resnik_1995.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_RESNIK_1995_ANCESTORS = Sim_pairwise_DAG_node_Resnik_1995_Ancestors.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_RESNIK_1995_DESCENDANTS = Sim_pairwise_DAG_node_Resnik_1995_Descendants.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_SCHLICKER_2006 = Sim_pairwise_DAG_node_Schlicker_2006_SimRel.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_SCHLICKER_JACCARD = Sim_pairwise_DAG_node_Schlicker_Jaccard_SimRel.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_SCHLICKER_3WJACCARD = Sim_pairwise_DAG_node_Schlicker_3WJaccard_SimRel.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_SCHLICKER_TVERSKY = Sim_pairwise_DAG_node_Schlicker_Tversky_SimRel.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_SCHLICKER_GL = Sim_pairwise_DAG_node_Schlicker_GL_SimRel.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_GL = Sim_pairwise_DAG_node_GL.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_GL_GRASM = Sim_pairwise_DAG_node_GL_GraSM.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_RESNIK_1995_GraSM = Sim_pairwise_DAG_node_Resnik_1995_GraSM.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_LIN_1998_GraSM = Sim_pairwise_DAG_node_Lin_1998_GraSM.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_MAZANDU_2012 = Sim_pairwise_DAG_node_Mazandu_2012.class.getName();
    public static final String SIM_PAIRWISE_DAG_NODE_SIM_IC_2010 = Sim_pairwise_DAG_node_Sim_IC_2010.class.getName();
    public static final Map<String, String> SIM_PAIRWISE_DAG_NODE_BASED = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, SIM_PAIRWISE_DAG_NODE_LIN_1998);
            put("SIM_PAIRWISE_DAG_NODE_TVERSKY_IC", SIM_PAIRWISE_DAG_NODE_TVERSKY_IC);
            put("SIM_PAIRWISE_DAG_NODE_JACCARD_IC", SIM_PAIRWISE_DAG_NODE_JACCARD_IC);
            put("SIM_PAIRWISE_DAG_NODE_JACCARD_3W_IC", SIM_PAIRWISE_DAG_NODE_JACCARD_3W_IC);
            put("SIM_PAIRWISE_DAG_NODE_JIANG_CONRATH_1997", SIM_PAIRWISE_DAG_NODE_JIANG_CONRATH_1997);
            put("SIM_PAIRWISE_DAG_NODE_RESNIK_1995", SIM_PAIRWISE_DAG_NODE_RESNIK_1995);
            put("SIM_PAIRWISE_DAG_NODE_RESNIK_1995_ANCESTORS", SIM_PAIRWISE_DAG_NODE_RESNIK_1995_ANCESTORS);
            put("SIM_PAIRWISE_DAG_NODE_RESNIK_1995_DESCENDANTS", SIM_PAIRWISE_DAG_NODE_RESNIK_1995_DESCENDANTS);
            put("SIM_PAIRWISE_DAG_NODE_RESNIK_1995_GRASM", SIM_PAIRWISE_DAG_NODE_RESNIK_1995_GraSM);
            put("SIM_PAIRWISE_DAG_NODE_LIN_1998_GRASM", SIM_PAIRWISE_DAG_NODE_LIN_1998_GraSM);
            put(SIM_PAIRWISE_DAG_NODE_SIM_IC_2010, SIM_PAIRWISE_DAG_NODE_SIM_IC_2010);
            put(FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_2006, SIM_PAIRWISE_DAG_NODE_SCHLICKER_2006);
            put(FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_JACCARD, SIM_PAIRWISE_DAG_NODE_SCHLICKER_JACCARD);
            put(FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_3WJACCARD, SIM_PAIRWISE_DAG_NODE_SCHLICKER_3WJACCARD);
            put(FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_TVERSKY, SIM_PAIRWISE_DAG_NODE_SCHLICKER_TVERSKY);
            put(FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_GL, SIM_PAIRWISE_DAG_NODE_SCHLICKER_GL);
            put(FLAG_SIM_PAIRWISE_DAG_NODE_GL, SIM_PAIRWISE_DAG_NODE_GL);
            put(FLAG_SIM_PAIRWISE_DAG_NODE_GL_GRASM, SIM_PAIRWISE_DAG_NODE_GL_GRASM);

            put("SIM_PAIRWISE_DAG_NODE_MAZANDU_2012", SIM_PAIRWISE_DAG_NODE_MAZANDU_2012);

        }
    };
    public static final HashSet<String> MEASURE_REQUIRE_EXTRA_IC = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {
            add(FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_2006);
            add(FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_JACCARD);
            add(FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_3WJACCARD);
            add(FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_TVERSKY);
            add(FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_GL);
        }
    };
    // HYBRID -----------------------------------------------------------------------------------
    public static final String SIM_PAIRWISE_DAG_HYBRID_RANWEZ_2006 = Sim_pairwise_DAG_hybrid_Ranwez_2006.class.getName();
    public static final String SIM_PAIRWISE_DAG_HYBRID_WANG_2007 = Sim_pairwise_DAG_hybrid_Wang_2007.class.getName();
    public static final Map<String, String> SIM_PAIRWISE_DAG_HYBRID_BASED = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("SIM_PAIRWISE_DAG_HYBRID_RANWEZ_2006", SIM_PAIRWISE_DAG_HYBRID_RANWEZ_2006);
            put("SIM_PAIRWISE_DAG_HYBRID_WANG_2007", SIM_PAIRWISE_DAG_HYBRID_WANG_2007);
        }
    };
    public static final HashMap<String, String> pairwiseMeasureMapping = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            putAll(SIM_PAIRWISE_GENERAL);
            putAll(SIM_PAIRWISE_DAG_EDGE_BASED);
            putAll(SIM_PAIRWISE_DAG_NODE_BASED);
            putAll(SIM_PAIRWISE_DAG_HYBRID_BASED);
            putAll(SIM_FRAMEWORK_DAG_SET_BASED);
            putAll(SIM_FRAMEWORK);
        }
    };
    public static final HashSet<String> PAIRWISE_MEASURE_FLAGS = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {
            addAll(pairwiseMeasureMapping.keySet());
        }
    };
    public static final HashMap<String, String> icsMapping = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            putAll(SIM_PAIRWISE_DAG_NODE_IC_INTRINSIC);
            putAll(SIM_PAIRWISE_DAG_NODE_IC_ANNOT);
        }
    };
    public static final HashSet<String> IC_FLAGS = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {
            addAll(icsMapping.keySet());
        }
    };

    public static int getPairwiseApproach(String measureFlag) {

        if (SIM_PAIRWISE_GENERAL.keySet().contains(measureFlag)) {
            return PAIRWISE_GENERAL;
        } else if (SIM_PAIRWISE_DAG_EDGE_BASED.keySet().contains(measureFlag)) {
            return PAIRWISE_DAG_EDGE_BASED;
        } else if (SIM_PAIRWISE_DAG_NODE_BASED.keySet().contains(measureFlag)) {
            return PAIRWISE_DAG_NODE_BASED;
        } else if (SIM_FRAMEWORK_DAG_SET_BASED.keySet().contains(measureFlag)) {
            return PAIRWISE_DAG_SET_BASED;
        } else if (SIM_PAIRWISE_DAG_HYBRID_BASED.keySet().contains(measureFlag)) {
            return PAIRWISE_DAG_HYBRID_BASED;
        } else {
            return NO_CORRESPONDANCE;
        }

    }

    public static boolean requireDAG(int mApproach) {
        if (mApproach == PAIRWISE_DAG_EDGE_BASED
                || mApproach == PAIRWISE_DAG_NODE_BASED
                || mApproach == PAIRWISE_DAG_HYBRID_BASED
                || mApproach == PAIRWISE_DAG_SET_BASED) {

            return true;
        }
        return false;
    }

    public static String pairwiseClassName(String methodFLAG) {
        return pairwiseMeasureMapping.get(methodFLAG);
    }

    public static String semanticMeasureClassName(String methodLabel) {
        String className = groupwiseMeasureMapping.get(methodLabel);
        if (className == null) {
            return pairwiseClassName(methodLabel);
        }
        return className;
    }
    public static final HashSet<String> MEASURE_FLAGS_IC_DEPENDENCY = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {
            add(SIM_GROUPWISE_DAG_GIC_FLAG);
            addAll(SIM_PAIRWISE_DAG_NODE_BASED.keySet());
        }
    };
}
