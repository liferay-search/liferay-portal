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

import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CommerceCatalogService;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprints.engine.parameter.Parameter;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterData;
import com.liferay.search.experiences.blueprints.engine.spi.searchrequest.SearchRequestBodyContributor;
import com.liferay.search.experiences.blueprints.message.Messages;
import com.liferay.search.experiences.blueprints.model.Blueprint;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	enabled = true, immediate = true, property = "name=commerce",
	service = SearchRequestBodyContributor.class
)
public class CommerceSearchRequestBodyContributor
	implements SearchRequestBodyContributor {

	@Override
	public void contribute(
		SearchRequestBuilder searchRequestBuilder, Blueprint blueprint,
		ParameterData parameterData, Messages messages) {

		if (!_isProductSearch(searchRequestBuilder)) {
			return;
		}

		if (!_isIndexersEnabled(searchRequestBuilder)) {
			_addCommerceCatalogGroupIdFilterClauses(
				searchRequestBuilder, parameterData);

			return;
		}

		if (_isBlueprintPreview(searchRequestBuilder)) {
			_setCommerceCatalogGroupsIds(searchRequestBuilder, parameterData);
		}
	}

	private void _addCommerceCatalogGroupIdFilterClauses(
		SearchRequestBuilder searchRequestBuilder,
		ParameterData parameterData) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		BooleanQuery mustNotQuery = _queries.booleanQuery();

		mustNotQuery.addMustNotQueryClauses(
			_queries.term(
				Field.ENTRY_CLASS_NAME, CPDefinition.class.getName()));

		booleanQuery.addShouldQueryClauses(mustNotQuery);

		BooleanQuery mustQuery = _queries.booleanQuery();

		mustQuery.addMustQueryClauses(
			_queries.term(
				Field.ENTRY_CLASS_NAME, CPDefinition.class.getName()));

		TermsQuery termsQuery = _queries.terms(Field.GROUP_ID);

		long[] commerceCatalogGroupIds = _getCommerceCatalogGroupIds(
			parameterData);

		if (commerceCatalogGroupIds.length == 0) {
			termsQuery.addValue("-1");
		}
		else {
			termsQuery.addValues(
				ArrayUtil.toStringArray(commerceCatalogGroupIds));
		}

		mustQuery.addMustQueryClauses(termsQuery);

		booleanQuery.addShouldQueryClauses(mustQuery);

		searchRequestBuilder.addComplexQueryPart(
			_complexQueryPartBuilderFactory.builder(
			).query(
				booleanQuery
			).occur(
				"filter"
			).build());
	}

	private long[] _getCommerceCatalogGroupIds(ParameterData parameterData) {
		List<CommerceCatalog> commerceCatalogs =
			_commerceCatalogService.getCommerceCatalogs(
				_getCompanyId(parameterData), 0, 100);

		if (commerceCatalogs.isEmpty()) {
			return new long[0];
		}

		Stream<CommerceCatalog> stream = commerceCatalogs.stream();

		return stream.mapToLong(
			commerceCatalog -> commerceCatalog.getGroupId()
		).toArray();
	}

	private long _getCompanyId(ParameterData parameterData) {
		Optional<Parameter> optional = parameterData.getByNameOptional(
			"company_id");

		Parameter parameter = optional.get();

		return GetterUtil.getLong(parameter.getValue());
	}

	private boolean _isBlueprintPreview(
		SearchRequestBuilder searchRequestBuilder) {

		return searchRequestBuilder.withSearchContextGet(
			searchContext -> GetterUtil.getBoolean(
				searchContext.getAttribute(
					"search.experiences.blueprint.preview")));
	}

	private boolean _isIndexersEnabled(
		SearchRequestBuilder searchRequestBuilder) {

		return searchRequestBuilder.withSearchContextGet(
			searchContext -> !GetterUtil.getBoolean(
				searchContext.getAttribute(
					"search.full.query.suppress.indexer.provided.clauses")));
	}

	private boolean _isProductSearch(
		SearchRequestBuilder searchRequestBuilder) {

		return searchRequestBuilder.withSearchContextGet(
			searchContext -> {
				SearchRequest searchRequest =
					(SearchRequest)searchContext.getAttribute("search.request");

				List<String> modelIndexerClassNames =
					searchRequest.getModelIndexerClassNames();

				if (modelIndexerClassNames.isEmpty() ||
					modelIndexerClassNames.contains(
						CPDefinition.class.getName())) {

					return true;
				}

				return false;
			});
	}

	private void _setCommerceCatalogGroupsIds(
		SearchRequestBuilder searchRequestBuilder,
		ParameterData parameterData) {

		long[] commerceCatalogGroupIds = _getCommerceCatalogGroupIds(
			parameterData);

		if (commerceCatalogGroupIds.length == 0) {
			return;
		}

		searchRequestBuilder.withSearchContext(
			searchContext -> searchContext.setAttribute(
				"commerceCatalogGroupIds", commerceCatalogGroupIds));
	}

	@Reference
	private CommerceCatalogService _commerceCatalogService;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private Queries _queries;

}