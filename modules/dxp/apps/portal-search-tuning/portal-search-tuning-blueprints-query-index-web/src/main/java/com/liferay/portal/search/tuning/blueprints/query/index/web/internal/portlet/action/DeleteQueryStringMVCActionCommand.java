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

import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexName;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexPortletKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryIndexWebKeys;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryString;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexReader;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexWriter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		"mvc.command.name=" + QueryIndexMVCCommandNames.DELETE_QUERY_STRING
	},
	service = MVCActionCommand.class
)
public class DeleteQueryStringMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		QueryStringIndexName queryStringIndexName =
			_queryStringIndexNameBuilder.getQueryStringIndexName(
				_portal.getCompanyId(actionRequest));

		_deleteQueryStrings(
			queryStringIndexName,
			_getQueryStrings(actionRequest, queryStringIndexName));

		sendRedirect(actionRequest, actionResponse);
	}

	private void _deleteQueryStrings(
		QueryStringIndexName queryStringIndexName,
		List<QueryString> queryStrings) {

		for (QueryString queryString : queryStrings) {
			_queryStringIndexWriter.remove(
				queryStringIndexName, queryString.getQueryStringId());
		}
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

	private List<QueryString> _getQueryStrings(
		ActionRequest actionRequest,
		QueryStringIndexName queryStringIndexName) {

		return Stream.of(
			_getQueryStringIds(actionRequest)
		).map(
			queryStringId -> _queryStringIndexReader.fetchQueryStringOptional(
				queryStringIndexName, queryStringId)
		).filter(
			Optional::isPresent
		).map(
			Optional::get
		).collect(
			Collectors.toList()
		);
	}

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private Portal _portal;

	@Reference
	private QueryStringIndexNameBuilder _queryStringIndexNameBuilder;

	@Reference
	private QueryStringIndexReader _queryStringIndexReader;

	@Reference
	private QueryStringIndexWriter _queryStringIndexWriter;

}