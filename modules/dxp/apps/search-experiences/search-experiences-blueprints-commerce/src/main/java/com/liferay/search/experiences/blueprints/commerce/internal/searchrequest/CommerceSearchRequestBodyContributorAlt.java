/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.blueprints.commerce.internal.searchrequest;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=commerce",
	service = SearchRequestBodyContributor.class
)
public class CommerceSearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		if ((blueprint.getBlueprintId() == 0) &&
			!_isIndexerClausesSuppressed(searchRequestBuilder)) {

			searchRequestBuilder.withSearchContext(
				searchContext -> searchContext.setAttribute(
					"search.blueprint.preview", true));
		}
	}

	private boolean _isIndexerClausesSuppressed(
		SearchRequestBuilder searchRequestBuilder) {

		return searchRequestBuilder.withSearchContextGet(
			searchContext -> GetterUtil.getBoolean(
				searchContext.getAttribute(
					"search.full.query.suppress.indexer.provided.clauses")));
	}

}