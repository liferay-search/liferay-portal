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
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexName;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexWebKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryString;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexReader;

import java.util.Map;
import java.util.Optional;

import javax.portlet.ActionRequest;
import javax.portlet.ActionURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class EditQueryStringDisplayBuilder {

	public EditQueryStringDisplayBuilder(
		HttpServletRequest httpServletRequest, Language language, Portal portal,
		RenderRequest renderRequest, RenderResponse renderResponse,
		QueryStringIndexNameBuilder queryStringIndexNameBuilder,
		QueryStringIndexReader queryStringIndexReader) {

		_httpServletRequest = httpServletRequest;
		_language = language;
		_portal = portal;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
		_queryStringIndexNameBuilder = queryStringIndexNameBuilder;
		_queryStringIndexReader = queryStringIndexReader;

		_queryStringId = ParamUtil.getString(
			renderRequest, QueryIndexWebKeys.QUERY_STRING_ID);

		_queryStringOptional = _getQueryStringOptional();

		_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public EditQueryStringDisplayContext build() {
		EditQueryStringDisplayContext editQueryStringDisplayContext =
			new EditQueryStringDisplayContext();

		_setData(editQueryStringDisplayContext);
		_setQueryStringId(editQueryStringDisplayContext);
		_setPageTitle(editQueryStringDisplayContext);
		_setRedirect(editQueryStringDisplayContext);

		return editQueryStringDisplayContext;
	}

	private long _getCompanyId() {
		return _portal.getCompanyId(_renderRequest);
	}

	private String _getContent() {
		return _queryStringOptional.map(
			QueryString::getContent
		).orElse(
			StringPool.BLANK
		);
	}

	private Map<String, Object> _getContext() {
		return HashMapBuilder.<String, Object>put(
			"locale", _themeDisplay.getLanguageId()
		).put(
			"namespace", _renderResponse.getNamespace()
		).build();
	}

	private String _getCreated() {
		return _queryStringOptional.map(
			queryString -> queryString.getCreated(
			).toString()
		).orElse(
			StringPool.BLANK
		);
	}

	private Long _getGroupId() {
		return _queryStringOptional.map(
			QueryString::getGroupId
		).orElse(
			null
		);
	}

	private String _getLanguageId() {
		return _queryStringOptional.map(
			QueryString::getLanguageId
		).orElse(
			StringPool.BLANK
		);
	}

	private Map<String, Object> _getProps() {
		return HashMapBuilder.<String, Object>put(
			"content", _getContent()
		).put(
			"created", _getCreated()
		).put(
			"groupId", _getGroupId()
		).put(
			"languageId", _getLanguageId()
		).put(
			"queryStringId", _queryStringId
		).put(
			"redirectURL", _getRedirect()
		).put(
			"status", _getStatus()
		).put(
			"statusDate", _getStatusDate()
		).put(
			"submitFormURL", _getSubmitFormURL()
		).build();
	}

	private Optional<QueryString> _getQueryStringOptional() {
		QueryStringIndexName queryStringIndexName =
			_queryStringIndexNameBuilder.getQueryStringIndexName(
				_getCompanyId());

		return _queryStringIndexReader.fetchQueryStringOptional(
			queryStringIndexName, _queryStringId);
	}

	private String _getRedirect() {
		return ParamUtil.getString(_httpServletRequest, "redirect");
	}

	private String _getStatus() {
		return _queryStringOptional.map(
			queryString -> queryString.getStatus(
			).name()
		).orElse(
			StringPool.BLANK
		);
	}

	private String _getStatusDate() {
		return _queryStringOptional.map(
			queryString -> queryString.getStatusDate(
			).toString()
		).orElse(
			StringPool.BLANK
		);
	}

	private String _getSubmitFormURL() {
		ActionURL actionURL = _renderResponse.createActionURL();

		actionURL.setParameter(
			ActionRequest.ACTION_NAME,
			QueryIndexMVCCommandNames.EDIT_QUERY_STRING);
		actionURL.setParameter(
			Constants.CMD,
			_queryStringOptional.isPresent() ? Constants.EDIT : Constants.ADD);
		actionURL.setParameter("redirect", _getRedirect());

		return actionURL.toString();
	}

	private void _setData(
		EditQueryStringDisplayContext editQueryStringDisplayContext) {

		editQueryStringDisplayContext.setData(
			HashMapBuilder.<String, Object>put(
				"context", _getContext()
			).put(
				"props", _getProps()
			).build());
	}

	private void _setPageTitle(
		EditQueryStringDisplayContext editQueryStringDisplayContext) {

		StringBundler sb = new StringBundler(2);

		sb.append(_queryStringOptional.isPresent() ? "edit-" : "add-");

		sb.append("query-string");

		editQueryStringDisplayContext.setPageTitle(
			_language.get(_httpServletRequest, sb.toString()));
	}

	private void _setQueryStringId(
		EditQueryStringDisplayContext editQueryStringDisplayContext) {

		_queryStringOptional.ifPresent(
			queryString -> editQueryStringDisplayContext.setQueryStringId(
				queryString.getQueryStringId()));
	}

	private void _setRedirect(
		EditQueryStringDisplayContext editQueryStringDisplayContext) {

		editQueryStringDisplayContext.setRedirect(_getRedirect());
	}

	private final HttpServletRequest _httpServletRequest;
	private final Language _language;
	private final Portal _portal;
	private final String _queryStringId;
	private final QueryStringIndexNameBuilder _queryStringIndexNameBuilder;
	private final QueryStringIndexReader _queryStringIndexReader;
	private final Optional<QueryString> _queryStringOptional;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ThemeDisplay _themeDisplay;

}