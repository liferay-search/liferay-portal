
package com.liferay.portal.search.tuning.gsearch.impl.results.item;

import com.liferay.portal.search.document.Document;

import com.liferay.portal.search.tuning.gsearch.api.results.item.ResultItemBuilder;

/**
 * Default item builder.
 *
 * @author Petteri Karttunen
 */
public class DefaultItemBuilder
	extends BaseResultItemBuilder implements ResultItemBuilder {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBuild(Document document) {
		return true;
	}

}