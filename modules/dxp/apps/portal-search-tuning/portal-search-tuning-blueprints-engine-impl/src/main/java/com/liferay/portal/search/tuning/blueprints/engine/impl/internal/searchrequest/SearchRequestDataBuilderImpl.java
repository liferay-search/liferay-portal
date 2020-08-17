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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.searchrequest;

import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.exception.SearchRequestDataException;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.searchrequest.data.contributor.SearchRequestDataContributor;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchRequestDataBuilder.class)
public class SearchRequestDataBuilderImpl implements SearchRequestDataBuilder {

	@Override
	public SearchRequestData build(SearchRequestContext searchRequestContext)
		throws SearchRequestDataException {

		SearchRequestData searchRequestData = new SearchRequestData(_queries);

		for (SearchRequestDataContributor searchRequestDataContributor :
				_searchRequestDataContributors) {

			searchRequestDataContributor.contribute(
				searchRequestContext, searchRequestData);
		}

		if (searchRequestContext.hasErrors()) {
			throw new SearchRequestDataException(
				"Couldn't build searchrequest data.",
				searchRequestContext.getMessages());
		}

		return searchRequestData;
	}

	protected void addSearchRequestDataContributor(
		SearchRequestDataContributor searchRequestDataContributor) {

		_searchRequestDataContributors.add(searchRequestDataContributor);
	}

	protected void removeSearchRequestDataContributor(
		SearchRequestDataContributor searchRequestDataContributor) {

		_searchRequestDataContributors.remove(searchRequestDataContributor);
	}

	@Reference
	private Queries _queries;

	@Reference(
		bind = "addSearchRequestDataContributor",
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		service = SearchRequestDataContributor.class,
		unbind = "removeSearchRequestDataContributor"
	)
	private volatile List<SearchRequestDataContributor>
		_searchRequestDataContributors = new ArrayList<>();

}