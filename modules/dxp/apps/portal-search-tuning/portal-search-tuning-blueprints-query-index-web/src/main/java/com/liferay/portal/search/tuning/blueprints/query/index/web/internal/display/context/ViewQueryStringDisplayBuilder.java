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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.display.context;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexReader;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class ViewQueryStringDisplayBuilder extends QueryStringDisplayBuilder {

	public ViewQueryStringDisplayBuilder(
		HttpServletRequest httpServletRequest, Language language, Portal portal,
		RenderRequest renderRequest, RenderResponse renderResponse,
		QueryStringIndexNameBuilder queryStringIndexNameBuilder,
		QueryStringIndexReader queryStringIndexReader) {

		super(
			httpServletRequest, language, portal, renderRequest, renderResponse,
			queryStringIndexNameBuilder, queryStringIndexReader);
	}

	public ViewQueryStringDisplayContext build() {
		ViewQueryStringDisplayContext viewQueryStringDisplayContext =
			new ViewQueryStringDisplayContext();

		setData(viewQueryStringDisplayContext);
		setQueryStringId(viewQueryStringDisplayContext);
		_setPageTitle(viewQueryStringDisplayContext);
		setRedirect(viewQueryStringDisplayContext);

		return viewQueryStringDisplayContext;
	}

	private void _setPageTitle(
		ViewQueryStringDisplayContext viewQueryStringDisplayContext) {

		viewQueryStringDisplayContext.setPageTitle(
			language.get(httpServletRequest, "view-query-string"));
	}

}