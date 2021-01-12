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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexReader;

import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class EditQueryStringDisplayBuilder extends QueryStringDisplayBuilder {

	public EditQueryStringDisplayBuilder(
		HttpServletRequest httpServletRequest, Language language, Portal portal,
		RenderRequest renderRequest, RenderResponse renderResponse,
		QueryStringIndexNameBuilder queryStringIndexNameBuilder,
		QueryStringIndexReader queryStringIndexReader) {

		super(
			httpServletRequest, language, portal, renderRequest, renderResponse,
			queryStringIndexNameBuilder, queryStringIndexReader);
	}

	public QueryStringDisplayContext build() {
		EditQueryStringDisplayContext editQueryStringDisplayContext =
			new EditQueryStringDisplayContext();

		setData(editQueryStringDisplayContext);
		setQueryStringId(editQueryStringDisplayContext);
		_setPageTitle(editQueryStringDisplayContext);
		setRedirect(editQueryStringDisplayContext);

		return editQueryStringDisplayContext;
	}

	@Override
	protected Map<String, Object> getProps() {
		Map<String, Object> props = super.getProps();

		props.put("submitFormURL", _getSubmitFormURL());

		return props;
	}

	private String _getSubmitFormURL() {
		ActionURL actionURL = renderResponse.createActionURL();

		actionURL.setParameter(
			ActionRequest.ACTION_NAME,
			QueryIndexMVCCommandNames.EDIT_QUERY_STRING);
		actionURL.setParameter(
			Constants.CMD,
			queryStringOptional.isPresent() ? Constants.EDIT : Constants.ADD);
		actionURL.setParameter("redirect", getRedirect());

		return actionURL.toString();
	}

	private void _setPageTitle(
		EditQueryStringDisplayContext editQueryStringDisplayContext) {

		StringBundler sb = new StringBundler(2);

		sb.append(queryStringOptional.isPresent() ? "edit-" : "add-");

		sb.append("query-string");

		editQueryStringDisplayContext.setPageTitle(
			language.get(httpServletRequest, sb.toString()));
	}

}