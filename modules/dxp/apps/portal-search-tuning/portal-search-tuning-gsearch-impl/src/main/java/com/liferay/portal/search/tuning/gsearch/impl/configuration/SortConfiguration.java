package com.liferay.portal.search.tuning.gsearch.impl.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * Sorts configuration.
 * 
 * @author Petteri Karttunen
 */
@ExtendedObjectClassDefinition(category = "gsearch")
@Meta.OCD(
	id = "com.liferay.portal.search.tuning.gsearch.impl.configuration.SortConfiguration",
	localization = "content/Language", 
	name = "sort-configuration"
)
public interface SortConfiguration {

	@Meta.AD(
		deflt = "", 
		description = "sort-configuration-desc",
		name = "sort-configuration-name", 
		required = false
	)
	public String[] sorts();

}