/*
 * Copyright: (c) 2012  Children's Hospital Boston, Regents of the University of Colorado 
 *
 * Except as contained in the copyright notice above, or as used to identify
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Dmitriy Dligach
 */

package org.chboston.cnlp.ctakes.relationextractor.cr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.XMLReader;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.Span;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.uimafit.component.JCasAnnotator_ImplBase;

import edu.mayo.bmi.uima.core.type.constants.CONST;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;

/**
 * Read named entity annotations from knowtator xml files into the CAS
 * 
 * @author dmitriy dligach
 *
 */
public class GoldEntityReader extends JCasAnnotator_ImplBase {

	// paramater that should contain the path to knowtator xml files
	public static final String PARAM_INPUTDIR = "InputDirectory";
	// path to knowtator xml files
	public static String inputDirectory;
	// counter for assigning entity ids
	public int identifiedAnnotationId;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		
		inputDirectory = (String)aContext.getConfigParameterValue(PARAM_INPUTDIR);
		identifiedAnnotationId = 0;
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

			JCas initView;
      try {
        initView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      } 
			String goldFilePath = inputDirectory + DocumentIDAnnotationUtil.getDocumentID(jCas) + ".knowtator.xml";
			
      SAXBuilder builder = new SAXBuilder();
      Document document;
      try {
        document = builder.build(new File(goldFilePath));
      } catch (JDOMException e) {
        throw new AnalysisEngineProcessException(e);
      } catch (IOException e) {
        throw new AnalysisEngineProcessException(e);
      }
			
      // map knowtator mention ids to entity offsets
			HashMap<String, ArrayList<Span>> entityMentions = XMLReader.getEntityMentions(document);
			// map knowtator mention ids to entity types
			HashMap<String, String> entityTypes = XMLReader.getEntityTypes(document);
			
			for(Map.Entry<String, ArrayList<Span>> entry : entityMentions.entrySet()) {

				// for disjoint spans, just ignore the gap
				Span first = entry.getValue().get(0);
				Span last = entry.getValue().get(entry.getValue().size() - 1);
				
				EntityMention entityMention = new EntityMention(initView, first.start, last.end);
				entityMention.setTypeID(Mapper.getEntityTypeId(entityTypes.get(entry.getKey())));
				entityMention.setId(identifiedAnnotationId++);
				entityMention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention.setConfidence(1);
				
				entityMention.addToIndexes();
			}
	}
}