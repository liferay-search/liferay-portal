/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.search.spi.model.query.contributor;

import com.liferay.object.constants.ObjectEntrySearchConstants;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.localization.SearchLocalizationHelper;
import com.liferay.portal.search.spi.model.query.contributor.HighlightFieldNamesQueryConfigContributor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(
	property = "indexer.class.name=com.liferay.object.model.ObjectEntry",
	service = HighlightFieldNamesQueryConfigContributor.class
)
public class ObjectEntryHighlightFieldNamesQueryConfigContributor
	implements HighlightFieldNamesQueryConfigContributor {

	@Override
	public String[] getHighlightFieldNames(SearchContext searchContext) {
		String[] fieldNames = {
			Field.ENTRY_CLASS_PK,
			ObjectEntrySearchConstants.NESTED_FIELD_ARRAY_VALUE_BOOLEAN,
			ObjectEntrySearchConstants.NESTED_FIELD_ARRAY_VALUE_KEYWORD,
			ObjectEntrySearchConstants.NESTED_FIELD_ARRAY_VALUE_TEXT,
			ObjectEntrySearchConstants.OBJECT_ENTRY_TITLE
		};

		return ArrayUtil.append(
			fieldNames,
			_searchLocalizationHelper.getLocalizedFieldNames(
				new String[] {
					ObjectEntrySearchConstants.NESTED_FIELD_ARRAY_VALUE
				},
				searchContext));
	}

	@Reference
	private SearchLocalizationHelper _searchLocalizationHelper;

}