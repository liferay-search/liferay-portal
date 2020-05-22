package com.liferay.portal.search.tuning.gsearch.configuration.constants;

import com.liferay.portal.kernel.security.permission.ActionKeys;

/**
 * @author Petteri Karttunen
 */
public class SearchConfigurationActionKeys {

	public static final String ADD_CONFIGURATION = "ADD_CONFIGURATION";
	public static final String ADD_SNIPPET = "ADD_SNIPPET";
	public static final String ADD_TEMPLATE = "ADD_TEMPLATE";
	public static final String DELETE_CONFIGURATION = "DELETE_CONFIGURATION";
	public static final String DELETE_SNIPPET = "DELETE_SNIPPET";
	public static final String DELETE_TEMPLATE = "DELETE_TEMPLATE";
	public static final String UPDATE_CONFIGURATION = "UPDATE_CONFIGURATION";
	public static final String UPDATE_SNIPPET = "UPDATE_SNIPPET";
	public static final String UPDATE_TEMPLATE = "UPDATE_TEMPLATE";
	public static final String VIEW_CONFIGURATION = "VIEW_CONFIGURATION";
	public static final String VIEW_SNIPPET = "VIEW_SNIPPET";
	public static final String VIEW_TEMPLATE = "VIEW_TEMPLATE";
	
	public static String getTypedActionKey(int type, String action) {
		
		if (type == SearchConfigurationTypes.CONFIGURATION) {
			if (ActionKeys.VIEW.equals(action)) {
				return SearchConfigurationActionKeys.VIEW_CONFIGURATION;
			} else if (ActionKeys.ADD_ENTRY.equals(action)) {
				return SearchConfigurationActionKeys.ADD_CONFIGURATION;
			} else if (ActionKeys.UPDATE.equals(action)) {
				return SearchConfigurationActionKeys.UPDATE_CONFIGURATION;
			} else if (ActionKeys.DELETE.equals(action)) {
				return SearchConfigurationActionKeys.DELETE_CONFIGURATION;
			}
		}
		else if (type == SearchConfigurationTypes.SNIPPET) {
			if (ActionKeys.VIEW.equals(action)) {
				return SearchConfigurationActionKeys.VIEW_SNIPPET;
			} else if (ActionKeys.ADD_ENTRY.equals(action)) {
				return SearchConfigurationActionKeys.ADD_SNIPPET;
			} else if (ActionKeys.UPDATE.equals(action)) {
				return SearchConfigurationActionKeys.UPDATE_SNIPPET;
			} else if (ActionKeys.DELETE.equals(action)) {
				return SearchConfigurationActionKeys.DELETE_SNIPPET;
			}
		}
		else if (type == SearchConfigurationTypes.TEMPLATE) {
			if (ActionKeys.VIEW.equals(action)) {
				return SearchConfigurationActionKeys.VIEW_TEMPLATE;
			} else if (ActionKeys.ADD_ENTRY.equals(action)) {
				return SearchConfigurationActionKeys.ADD_TEMPLATE;
			} else if (ActionKeys.UPDATE.equals(action)) {
				return SearchConfigurationActionKeys.UPDATE_TEMPLATE;
			} else if (ActionKeys.DELETE.equals(action)) {
				return SearchConfigurationActionKeys.DELETE_TEMPLATE;
			}
		}
		
		throw new RuntimeException(
				"Search Configuration type " + type + " not recognized.");
	}
}
