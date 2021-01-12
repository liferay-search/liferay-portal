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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.UpdateByQueryDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.script.Script;
import com.liferay.portal.search.script.ScriptType;
import com.liferay.portal.search.script.Scripts;
import com.liferay.portal.search.tuning.blueprints.query.index.constants.Reason;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexName;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryStringStatus;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.util.QueryIndexUtil;

import java.util.Date;
import java.util.HashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = QueryStringIndexWriter.class)
public class QueryStringIndexWriterImpl implements QueryStringIndexWriter {

	@Override
	public void addHit(QueryStringIndexName queryStringIndexName, String id) {
		UpdateByQueryDocumentRequest updateByQueryDocumentRequest =
			new UpdateByQueryDocumentRequest(
				_queries.term("_id", id), _getAddHitScript(),
				queryStringIndexName.getIndexName());

		updateByQueryDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(updateByQueryDocumentRequest);
	}

	@Override
	public void addReport(
		QueryStringIndexName queryStringIndexName, String id, Reason reason) {

		UpdateByQueryDocumentRequest updateByQueryDocumentRequest =
			new UpdateByQueryDocumentRequest(
				_queries.term("_id", id), _getAddReportScript(reason),
				queryStringIndexName.getIndexName());

		updateByQueryDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(updateByQueryDocumentRequest);
	}

	@Override
	public String create(
		QueryStringIndexName queryStringIndexName, QueryString queryString) {

		IndexDocumentRequest documentRequest = new IndexDocumentRequest(
			queryStringIndexName.getIndexName(),
			_queryStringToDocumentTranslator.translate(queryString));

		documentRequest.setRefresh(true);

		IndexDocumentResponse indexDocumentResponse =
			_searchEngineAdapter.execute(documentRequest);

		return indexDocumentResponse.getUid();
	}

	@Override
	public void remove(QueryStringIndexName queryStringIndexName, String id) {
		DeleteDocumentRequest deleteDocumentRequest = new DeleteDocumentRequest(
			queryStringIndexName.getIndexName(), id);

		deleteDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(deleteDocumentRequest);
	}

	@Override
	public void update(
		QueryStringIndexName queryStringIndexName, QueryString queryString) {

		UpdateDocumentRequest updateDocumentRequest = new UpdateDocumentRequest(
			queryStringIndexName.getIndexName(), queryString.getQueryStringId(),
			_queryStringToDocumentTranslator.translate(queryString));

		updateDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(updateDocumentRequest);
	}

	private Script _getAddHitScript() {
		String now = QueryIndexUtil.toIndexDateString(new Date());

		StringBundler sb = new StringBundler(5);

		sb.append("ctx._source.");
		sb.append(QueryStringFields.LAST_ACCESSED);
		sb.append("=");
		sb.append(now);
		sb.append("L;ctx._source.hitCount++");

		return _scripts.builder(
		).idOrCode(
			sb.toString()
		).language(
			"painless"
		).parameters(
			new HashMap<String, Object>()
		).scriptType(
			ScriptType.INLINE
		).build();
	}

	private Script _getAddReportScript(Reason reason) {
		Date now = new Date();

		StringBundler sb = new StringBundler(14);

		sb.append("ctx._source.");
		sb.append(QueryStringFields.STATUS);
		sb.append("=");
		sb.append(QueryStringStatus.REPORTED.name());
		sb.append(";ctx._source.");
		sb.append(QueryStringFields.LAST_REPORTED);
		sb.append("=");
		sb.append(QueryIndexUtil.toIndexDateString(now));
		sb.append("L;ctx._source.reportCount++;ctx._source.");
		sb.append(QueryStringFields.REPORTS);
		sb.append("=");
		sb.append(now);
		sb.append("-");
		sb.append(reason.name());

		return _scripts.builder(
		).idOrCode(
			sb.toString()
		).language(
			"painless"
		).parameters(
			new HashMap<String, Object>()
		).scriptType(
			ScriptType.INLINE
		).build();
	}

	@Reference
	private Queries _queries;

	@Reference
	private QueryStringToDocumentTranslator _queryStringToDocumentTranslator;

	@Reference
	private Scripts _scripts;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}