/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.service.internal.search.index.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.index.RefreshIndexRequest;
import com.liferay.portal.search.engine.adapter.search.CountSearchRequest;
import com.liferay.portal.search.engine.adapter.search.CountSearchResponse;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.spi.reindexer.IndexReindexer;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.metrics.search.index.constants.WorkflowMetricsIndexNameConstants;
import com.liferay.portal.workflow.metrics.search.index.reindexer.WorkflowMetricsReindexer;
import com.liferay.portal.workflow.metrics.search.index.reindexer.WorkflowMetricsReindexerRegistry;
import com.liferay.portal.workflow.metrics.service.util.BaseWorkflowMetricsIndexerTestCase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Rodrigo Guedes de Souza
 */
@RunWith(Arquillian.class)
public class WorkflowMetricsIndexerCrossWriterTest
	extends BaseWorkflowMetricsIndexerTestCase {

	@Test
	public void testInstanceFullReindexPopulatesTasks() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			addKaleoTaskInstanceToken("Review");

		_reindex(companyId, "instance", IndexReindexer.ExecutionMode.FULL);

		Query tasksQuery = _getTasksQuery(
			kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());

		Assert.assertEquals(
			tasksQuery.toString(), 1,
			_getCount(
				_getIndexName(
					companyId,
					WorkflowMetricsIndexNameConstants.SUFFIX_INSTANCE),
				tasksQuery));
	}

	@Test
	public void testInstanceSyncReindexPopulatesTemplates() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		addKaleoTaskInstanceToken("Review");

		_reindex(companyId, "instance", IndexReindexer.ExecutionMode.FULL);

		String instanceIndexName = _getIndexName(
			companyId, WorkflowMetricsIndexNameConstants.SUFFIX_INSTANCE);

		Query templateQuery = _getTemplateQuery("instanceId");

		long totalCount = _getCount(instanceIndexName, templateQuery);

		Assert.assertTrue(totalCount > 0);

		_reindex(companyId, "instance", IndexReindexer.ExecutionMode.SYNC);

		Assert.assertEquals(
			templateQuery.toString(), totalCount,
			_getCount(instanceIndexName, templateQuery));
	}

	@Test
	public void testProcessFullReindexRemovesOrphanDocument() throws Exception {
		_assertProcessReindexRemovesOrphanDocument(
			IndexReindexer.ExecutionMode.FULL);
	}

	@Test
	public void testProcessSyncReindexRemovesOrphanDocument() throws Exception {
		_assertProcessReindexRemovesOrphanDocument(
			IndexReindexer.ExecutionMode.SYNC);
	}

	@Test
	public void testTaskSyncReindexPopulatesTemplates() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		addKaleoTaskInstanceToken("Review");

		_reindex(companyId, "instance", IndexReindexer.ExecutionMode.FULL);

		String taskIndexName = _getIndexName(
			companyId, WorkflowMetricsIndexNameConstants.SUFFIX_TASK);

		Query templateQuery = _getTemplateQuery("taskId");

		long totalCount = _getCount(taskIndexName, templateQuery);

		Assert.assertTrue(totalCount > 0);

		_reindex(companyId, "task", IndexReindexer.ExecutionMode.SYNC);

		Assert.assertEquals(
			templateQuery.toString(), totalCount,
			_getCount(taskIndexName, templateQuery));
	}

	private void _assertProcessReindexRemovesOrphanDocument(
			IndexReindexer.ExecutionMode executionMode)
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		String processIndexName = _getIndexName(
			companyId, WorkflowMetricsIndexNameConstants.SUFFIX_PROCESS);

		_indexProcessDocument(processIndexName, companyId);

		Assert.assertEquals(
			1, _getCount(processIndexName, _getProcessQuery(companyId)));

		_reindex(companyId, "process", executionMode);

		Assert.assertEquals(
			executionMode + " reindex must remove orphan documents", 0,
			_getCount(processIndexName, _getProcessQuery(companyId)));
	}

	private long _getCount(String indexName, Query query) {
		searchEngineAdapter.execute(new RefreshIndexRequest(indexName));

		CountSearchRequest countSearchRequest = new CountSearchRequest();

		countSearchRequest.setIndexNames(indexName);
		countSearchRequest.setQuery(query);

		CountSearchResponse countSearchResponse = searchEngineAdapter.execute(
			countSearchRequest);

		return countSearchResponse.getCount();
	}

	private String _getIndexName(long companyId, String suffix) {
		return _indexNameBuilder.getIndexName(companyId) + suffix;
	}

	private Query _getProcessQuery(long companyId) {
		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		return booleanQuery.addFilterQueryClauses(
			QueriesUtil.term("companyId", companyId),
			QueriesUtil.term("processId", _PROCESS_ID));
	}

	private Query _getTasksQuery(long taskId) {
		return QueriesUtil.nested(
			"tasks", QueriesUtil.term("tasks.taskId", taskId));
	}

	private Query _getTemplateQuery(String idFieldName) {
		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addMustQueryClauses(
			QueriesUtil.term(idFieldName, 0L),
			QueriesUtil.term("deleted", false));

		return booleanQuery;
	}

	private void _indexProcessDocument(String indexName, long companyId) {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.builder();

		documentBuilder.setValue(
			"deleted", false
		).setLong(
			"companyId", companyId
		).setLong(
			"processId", _PROCESS_ID
		).setString(
			"uid", "WorkflowMetricsProcess" + _PROCESS_ID
		);

		Document document = documentBuilder.build();

		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			indexName, document.getString("uid"), document);

		indexDocumentRequest.setRefresh(true);

		searchEngineAdapter.execute(indexDocumentRequest);
	}

	private void _reindex(
			long companyId, String key,
			IndexReindexer.ExecutionMode executionMode)
		throws Exception {

		if (executionMode == IndexReindexer.ExecutionMode.SYNC) {
			Thread.sleep(1000);
		}

		WorkflowMetricsReindexer workflowMetricsReindexer =
			_workflowMetricsReindexerRegistry.getWorkflowMetricsReindexer(key);

		IndexReindexer indexReindexer =
			(IndexReindexer)workflowMetricsReindexer;

		indexReindexer.reindex(companyId, executionMode);
	}

	private static final long _PROCESS_ID = RandomTestUtil.randomLong();

	@Inject
	private IndexNameBuilder _indexNameBuilder;

	@Inject
	private WorkflowMetricsReindexerRegistry _workflowMetricsReindexerRegistry;

}