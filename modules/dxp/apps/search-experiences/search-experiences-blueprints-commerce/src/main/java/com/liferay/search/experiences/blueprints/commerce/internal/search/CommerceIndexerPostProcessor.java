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

package com.liferay.search.experiences.blueprints.commerce.internal.search;

import com.liferay.portal.kernel.search.BaseIndexerPostProcessor;
import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.IndexerPostProcessor;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.filter.TermFilter;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "indexer.class.name=com.liferay.commerce.product.model.CPDefinition",
	service = IndexerPostProcessor.class
)
public class CommerceIndexerPostProcessor extends BaseIndexerPostProcessor {

	@Override
	public void postProcessContextBooleanFilter(
			BooleanFilter booleanFilter, SearchContext searchContext)
		throws Exception {

		boolean blueprintPreview = GetterUtil.getBoolean(
			searchContext.getAttribute("search.blueprint.preview"));

		if (blueprintPreview) {
			_removeMinusOneGroupFilter(booleanFilter);
		}
	}

	private boolean _isMinusOneGroupFilter(BooleanClause<Filter> b) {
		Filter filter = b.getClause();

		if (filter instanceof TermFilter) {
			TermFilter termFilter = (TermFilter)filter;

			String field = termFilter.getField();

			String value = termFilter.getValue();

			if (field.equals("groupId") && value.equals("-1")) {
				return true;
			}
		}

		return false;
	}

	private void _removeMinusOneGroupFilter(BooleanFilter booleanFilter) {
		List<BooleanClause<Filter>> clauses =
			booleanFilter.getMustBooleanClauses();

		clauses.removeIf(
			booleanClause -> _isMinusOneGroupFilter(booleanClause));
	}

}