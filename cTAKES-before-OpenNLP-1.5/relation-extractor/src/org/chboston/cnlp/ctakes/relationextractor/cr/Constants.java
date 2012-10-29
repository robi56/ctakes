package org.chboston.cnlp.ctakes.relationextractor.cr;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Various global settings.
 * 
 * @author dmitriy dligach
 *
 */
public class Constants {
	
	// names of semantic arguments
	public static final String mipacqArgumentName = "Argument";
	public static final String mipacqRelatedToName = "Related_to";
	public static final String sharpArgumentName = "Argument_CU";
	public static final String sharpRelatedToName = "Related_to_CU";
	
	// high priority sharp relations 
	public static final HashSet<String> relationSet = new HashSet<String>(Arrays.asList("location_of", "degree_of"));
}