/*
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package com.github.sharispe.slib.dsm.core.model.access.twodmodels;

import com.github.sharispe.slib.dsm.core.model.access.ModelAccessor;
import com.github.sharispe.slib.dsm.core.model.access.ModelAccessorUtils;
import com.github.sharispe.slib.dsm.core.model.utils.entityinfo.EntityInfo_2D_MODEL;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConf;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Class used to access information which is stored into the model
 *
 * TODO Do not open a new file channel each time you load a vector.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public abstract class ModelAccessor_2D implements ModelAccessor<EntityInfo_2D_MODEL> {

    Logger logger = LoggerFactory.getLogger(ModelAccessor_2D.class);

    Map<Integer, EntityInfo_2D_MODEL> index;
    final ModelConf model;

    public ModelAccessor_2D(ModelConf c) throws SLIB_Ex_Critic {
        this.model = c;
        this.index = ModelAccessorUtils.loadIndex_2D_MODEL(c);
    }
        
    public ModelAccessor_2D(ModelConf c, Map<Integer, EntityInfo_2D_MODEL> index) throws SLIB_Ex_Critic {
        this.model = c;
        this.index = index;
    }

   

    @Override
    public Map<Integer, EntityInfo_2D_MODEL> getIndexedElementInfo() {
        return index;
    }

    @Override
    public Set<Integer> getElementIds() {
        return index.keySet();
    }

    @Override
    public ModelConf getConf() {
        return model;
    }

}
