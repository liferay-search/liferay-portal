package com.liferay.portal.search.tuning.gsearch.api.constants;

/**
 * Clause (JSON) configuration values.
 * 
 * @author Petteri Karttunen
 */
public interface ClauseConfigurationValues extends ConfigurationValues {

	public static final String QUERY_TYPE_GSEARCH_DECAY_FUNCTION_SCORE = 
			"gsearch_decay_function_score";

	public static final String QUERY_TYPE_GSEARCH_FIELD_VALUE_FACTOR_FUNCTION_SCORE = 
			"gsearch_field_value_factor";

	public static final String QUERY_TYPE_GSEARCH_QUERY_STRING = 
			"query_string";

	public static final String QUERY_TYPE_GSEARCH_MULTIMATCH = 
			"multi_match";

	public static final String QUERY_TYPE_MATCH = 
			"match";

	public static final String QUERY_TYPE_MORE_LIKE_THIS = 
			"more_like_this";

	public static final String QUERY_TYPE_LTR = 
			"ltr";

	public static final String QUERY_TYPE_TERM = 
			"term";

	public static final String QUERY_TYPE_WILDCARD = 
			"wildcard";


}
