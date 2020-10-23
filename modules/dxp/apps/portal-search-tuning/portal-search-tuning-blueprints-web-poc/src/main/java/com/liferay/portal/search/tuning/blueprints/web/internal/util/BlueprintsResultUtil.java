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

package com.liferay.portal.search.tuning.blueprints.web.internal.util;

import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.web.internal.constants.BlueprintsWebPortletKeys;
import com.liferay.portal.search.web.interpreter.SearchResultInterpreter;
import com.liferay.portal.search.web.interpreter.SearchResultInterpreterProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Olivia Yu
 */
@Component(immediate = true, service = {})
public class BlueprintsResultUtil {

	public static AssetRenderer<?> getAssetRenderer(
		String entryClassName, long entryClassPK) {

		Document document = _documentBuilderFactory.builder(
		).setString(
			Field.ENTRY_CLASS_NAME, entryClassName
		).setLong(
			Field.ENTRY_CLASS_PK, Long.valueOf(entryClassPK)
		).build();

		SearchResultInterpreter searchResultInterpreter =
			_getSearchResultInterpreter();

		try {
			return searchResultInterpreter.getAssetRenderer(document);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Unable to get asset renderer for class ",
						entryClassName, " with primary key ", entryClassPK),
					exception);
			}

			return null;
		}
	}

	@Reference(unbind = "-")
	protected void setPortal(Portal portal) {
		_portal = portal;
	}

	@Reference(unbind = "-")
	protected void setSearchDocumentBuilderFactory(
		DocumentBuilderFactory documentBuilderFactory) {

		_documentBuilderFactory = documentBuilderFactory;
	}

	@Reference(unbind = "-")
	protected void setSearchResultInterpreterProvider(
		SearchResultInterpreterProvider searchResultInterpreterProvider) {

		_searchResultInterpreterProvider = searchResultInterpreterProvider;
	}

	private static SearchResultInterpreter _getSearchResultInterpreter() {
		return _searchResultInterpreterProvider.getSearchResultInterpreter(
			BlueprintsWebPortletKeys.BLUEPRINTS_WEB);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsResultUtil.class);

	private static DocumentBuilderFactory _documentBuilderFactory;
	private static Portal _portal;
	private static SearchResultInterpreterProvider
		_searchResultInterpreterProvider;

}