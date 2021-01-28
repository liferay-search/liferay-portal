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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.portlet.action;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexName;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexPortletKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexWebKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryStringStatus;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryString;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexReader;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexWriter;

import java.util.Date;
import java.util.Optional;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + QueryIndexPortletKeys.QUERY_INDEX_ADMIN,
		"mvc.command.name=" + QueryIndexMVCCommandNames.EDIT_QUERY_STRING
	},
	service = MVCActionCommand.class
)
public class EditQueryStringMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		editQueryString(actionRequest);

		sendRedirect(actionRequest, actionResponse);
	}

	protected void editQueryString(ActionRequest actionRequest) {
		QueryStringIndexName queryStringIndexName =
			_queryStringIndexNameBuilder.getQueryStringIndexName(
				_portal.getCompanyId(actionRequest));

		String[] queryStringIds = _getQueryStringIds(actionRequest);

		if (queryStringIds == null) {
			_addQueryString(actionRequest, queryStringIndexName);

			return;
		}

		for (String id : queryStringIds) {
			Optional<QueryString> queryStringOptional = _getQueryStringOptional(
				id, queryStringIndexName);

			if (queryStringOptional.isPresent()) {
				_updateQueryString(
					actionRequest, queryStringIndexName,
					queryStringOptional.get());
			}
		}
	}

	private void _addQueryString(
		ActionRequest actionRequest,
		QueryStringIndexName queryStringIndexName) {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		_queryStringIndexWriter.create(
			queryStringIndexName,
			new QueryString.QueryStringBuilder().companyId(
				themeDisplay.getCompanyId()
			).content(
				_getContent(actionRequest)
			).created(
				new Date()
			).groupId(
				_getGroupId(actionRequest)
			).hitCount(
				0L
			).languageId(
				_getLanguageId(actionRequest)
			).modified(
				new Date()
			).reportCount(
				0L
			).status(
				QueryStringStatus.ACTIVE
			).statusDate(
				new Date()
			).build());
	}

	private String _getContent(ActionRequest actionRequest) {
		return ParamUtil.getString(actionRequest, QueryIndexWebKeys.CONTENT);
	}

	private long _getGroupId(ActionRequest actionRequest) {
		return ParamUtil.getLong(actionRequest, QueryIndexWebKeys.GROUP_ID);
	}

	private String _getLanguageId(ActionRequest actionRequest) {
		return ParamUtil.getString(
			actionRequest, QueryIndexWebKeys.LANGUAGE_ID);
	}

	private String[] _getQueryStringIds(ActionRequest actionRequest) {
		String[] queryStringIds = null;

		String queryStringId = ParamUtil.getString(
			actionRequest, QueryIndexWebKeys.QUERY_STRING_ID);

		if (!Validator.isBlank(queryStringId)) {
			queryStringIds = new String[] {queryStringId};
		}
		else {
			queryStringIds = ParamUtil.getStringValues(
				actionRequest, QueryIndexWebKeys.ROW_IDS);
		}

		return queryStringIds;
	}

	private Optional<QueryString> _getQueryStringOptional(
		String id, QueryStringIndexName queryStringIndexName) {

		return _queryStringIndexReader.fetchQueryStringOptional(
			queryStringIndexName, id);
	}

	private QueryStringStatus _getStatus(ActionRequest actionRequest)
		throws IllegalArgumentException {

		String status = ParamUtil.getString(
			actionRequest, QueryIndexWebKeys.STATUS);

		if (Validator.isBlank(status)) {
			return null;
		}

		return QueryStringStatus.valueOf(status);
	}

	private void _updateQueryString(
		ActionRequest actionRequest, QueryStringIndexName queryStringIndexName,
		QueryString queryString) {

		try {
			QueryStringStatus status = _getStatus(actionRequest);

			long groupId = _getGroupId(actionRequest);

			if ((status == null) && (groupId == 0)) {
				return;
			}

			QueryString.QueryStringBuilder queryStringBuilder =
				new QueryString.QueryStringBuilder();

			queryStringBuilder.queryStringId(queryString.getQueryStringId());

			if (status != null) {
				queryStringBuilder.status(status);
				queryStringBuilder.statusDate(new Date());
			}

			if (groupId > 0) {
				queryStringBuilder.groupId(groupId);
			}

			queryStringBuilder.modified(new Date());

			_queryStringIndexWriter.update(
				queryStringIndexName, queryStringBuilder.build());
		}
		catch (IllegalArgumentException illegalArgumentException) {
			SessionErrors.add(
				actionRequest, QueryIndexWebKeys.ERROR,
				illegalArgumentException.getMessage());

			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		EditQueryStringMVCActionCommand.class);

	@Reference
	private Portal _portal;

	@Reference
	private QueryStringIndexNameBuilder _queryStringIndexNameBuilder;

	@Reference
	private QueryStringIndexReader _queryStringIndexReader;

	@Reference
	private QueryStringIndexWriter _queryStringIndexWriter;

}