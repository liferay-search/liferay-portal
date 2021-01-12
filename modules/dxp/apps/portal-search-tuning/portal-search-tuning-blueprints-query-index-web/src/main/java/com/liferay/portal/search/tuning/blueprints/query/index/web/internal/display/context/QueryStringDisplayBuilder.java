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

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexWebKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryString;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public abstract class QueryStringDisplayBuilder {

	public QueryStringDisplayBuilder(
		HttpServletRequest httpServletRequest, Language language, Portal portal,
		RenderRequest renderRequest, RenderResponse renderResponse,
		QueryStringIndexNameBuilder queryStringIndexNameBuilder,
		QueryStringIndexReader queryStringIndexReader) {

		this.httpServletRequest = httpServletRequest;
		this.language = language;
		this.portal = portal;
		this.renderRequest = renderRequest;
		this.renderResponse = renderResponse;
		this.queryStringIndexNameBuilder = queryStringIndexNameBuilder;
		this.queryStringIndexReader = queryStringIndexReader;

		queryStringId = ParamUtil.getString(
			renderRequest, QueryIndexWebKeys.QUERY_STRING_ID);

		queryStringOptional = getQueryStringOptional();

		themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	protected long getCompanyId() {
		return portal.getCompanyId(renderRequest);
	}

	protected String getContent() {
		return queryStringOptional.map(
			QueryString::getContent
		).orElse(
			StringPool.BLANK
		);
	}

	protected Map<String, Object> getContext() {
		return HashMapBuilder.<String, Object>put(
			"locale", themeDisplay.getLanguageId()
		).put(
			"namespace", renderResponse.getNamespace()
		).build();
	}

	protected String getCreated() {
		return queryStringOptional.map(
			queryString -> queryString.getCreated(
			).toString()
		).orElse(
			StringPool.BLANK
		);
	}

	protected Long getGroupId() {
		return queryStringOptional.map(
			QueryString::getGroupId
		).orElse(
			null
		);
	}

	protected Long getHitCount() {
		return queryStringOptional.map(
			QueryString::getHitCount
		).orElse(
			0L
		);
	}

	protected String getLanguageId() {
		return queryStringOptional.map(
			QueryString::getLanguageId
		).orElse(
			StringPool.BLANK
		);
	}

	protected Map<String, Object> getProps() {
		return HashMapBuilder.<String, Object>put(
			"content", getContent()
		).put(
			"created", getCreated()
		).put(
			"groupId", getGroupId()
		).put(
			"hitCount", getHitCount()
		).put(
			"languageId", getLanguageId()
		).put(
			"queryStringId", queryStringId
		).put(
			"redirectURL", getRedirect()
		).put(
			"reportCount", getReportCount()
		).put(
			"reports", getReports()
		).put(
			"status", getStatus()
		).put(
			"statusDate", getStatusDate()
		).build();
	}

	protected Optional<QueryString> getQueryStringOptional() {
		return queryStringIndexReader.fetchQueryStringOptional(
			queryStringIndexNameBuilder.getQueryStringIndexName(getCompanyId()),
			queryStringId);
	}

	protected String getRedirect() {
		return ParamUtil.getString(httpServletRequest, "redirect");
	}

	protected Long getReportCount() {
		return queryStringOptional.map(
			QueryString::getReportCount
		).orElse(
			0L
		);
	}

	protected Map<String, String> getReports() {
		Map<String, String> reportsMap = new HashMap<>();

		if (queryStringOptional.isPresent()) {
			return reportsMap;
		}

		QueryString queryString = queryStringOptional.get();

		List<String> reports = queryString.getReports();

		Stream<String> stream = reports.stream();

		stream.forEach(
			s -> {
				try {
					String[] arr = s.split("-");

					reportsMap.put(arr[0], arr[1]);
				}
				catch (Exception exception) {
					_log.error(exception.getMessage(), exception);
				}
			});

		return reportsMap;
	}

	protected String getStatus() {
		return queryStringOptional.map(
			queryString -> queryString.getStatus(
			).name()
		).orElse(
			StringPool.BLANK
		);
	}

	protected String getStatusDate() {
		return queryStringOptional.map(
			queryString -> queryString.getStatusDate(
			).toString()
		).orElse(
			StringPool.BLANK
		);
	}

	protected void setData(
		QueryStringDisplayContext viewQueryStringDisplayContext) {

		viewQueryStringDisplayContext.setData(
			HashMapBuilder.<String, Object>put(
				"context", getContext()
			).put(
				"props", getProps()
			).build());
	}

	protected void setQueryStringId(
		QueryStringDisplayContext viewQueryStringDisplayContext) {

		queryStringOptional.ifPresent(
			queryString -> viewQueryStringDisplayContext.setQueryStringId(
				queryString.getQueryStringId()));
	}

	protected void setRedirect(
		QueryStringDisplayContext viewQueryStringDisplayContext) {

		viewQueryStringDisplayContext.setRedirect(getRedirect());
	}

	protected final HttpServletRequest httpServletRequest;
	protected final Language language;
	protected final Portal portal;
	protected final String queryStringId;
	protected final QueryStringIndexNameBuilder queryStringIndexNameBuilder;
	protected final QueryStringIndexReader queryStringIndexReader;
	protected final Optional<QueryString> queryStringOptional;
	protected final RenderRequest renderRequest;
	protected final RenderResponse renderResponse;
	protected final ThemeDisplay themeDisplay;

	private static final Log _log = LogFactoryUtil.getLog(
		QueryStringDisplayBuilder.class);

}