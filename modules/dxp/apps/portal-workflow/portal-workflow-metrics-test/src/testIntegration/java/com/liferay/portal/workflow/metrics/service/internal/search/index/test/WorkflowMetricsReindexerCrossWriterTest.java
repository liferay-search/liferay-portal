/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.service.internal.search.index.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.util.TestPropsValues;
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
public class WorkflowMetricsReindexerCrossWriterTest
	extends BaseWorkflowMetricsIndexerTestCase {

	@Test
	public void testFullReindexInstancePopulatesTasksArray() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			addKaleoTaskInstanceToken("Review");

		_reindex(companyId, "instance", IndexReindexer.ExecutionMode.FULL);

		Query tasksQuery = _tasksQuery(
			kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());

		String instanceIndexName = _getIndexName(
			companyId, WorkflowMetricsIndexNameConstants.SUFFIX_INSTANCE);

		Assert.assertEquals(
			"FULL instance reindex must repopulate the task-indexer-written " +
				"tasks[] array",
			1, _count(instanceIndexName, tasksQuery));
	}

	@Test
	public void testSyncReindexPreservesInstanceIndexTemplate()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		addKaleoTaskInstanceToken("Review");

		_reindex(companyId, "instance", IndexReindexer.ExecutionMode.FULL);

		String instanceIndexName = _getIndexName(
			companyId, WorkflowMetricsIndexNameConstants.SUFFIX_INSTANCE);

		long baseline = _count(instanceIndexName, _templateQuery("instanceId"));

		Assert.assertTrue(
			"Baseline must contain the process-written instanceId=0 template",
			baseline > 0);

		// Guarantee the baseline timestamps are strictly older (second
		// precision) than the SYNC cutoff date.

		Thread.sleep(1100);

		_reindex(companyId, "instance", IndexReindexer.ExecutionMode.SYNC);

		Assert.assertEquals(
			"SYNC instance reindex must not strand the process-written " +
				"instanceId=0 template",
			baseline, _count(instanceIndexName, _templateQuery("instanceId")));
	}

	@Test
	public void testSyncReindexPreservesTaskIndexTemplate() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		addKaleoTaskInstanceToken("Review");

		_reindex(companyId, "instance", IndexReindexer.ExecutionMode.FULL);

		String taskIndexName = _getIndexName(
			companyId, WorkflowMetricsIndexNameConstants.SUFFIX_TASK);

		long baseline = _count(taskIndexName, _templateQuery("taskId"));

		Assert.assertTrue(
			"Baseline must contain the node-written taskId=0 template",
			baseline > 0);

		// Guarantee the baseline timestamps are strictly older (second
		// precision) than the SYNC cutoff date.

		Thread.sleep(1100);

		_reindex(companyId, "task", IndexReindexer.ExecutionMode.SYNC);

		Assert.assertEquals(
			"SYNC task reindex must not strand the node-written taskId=0 " +
				"template",
			baseline, _count(taskIndexName, _templateQuery("taskId")));
	}

	private long _count(String indexName, Query query) {
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

	private void _reindex(
			long companyId, String key,
			IndexReindexer.ExecutionMode executionMode)
		throws Exception {

		WorkflowMetricsReindexer workflowMetricsReindexer =
			_workflowMetricsReindexerRegistry.getWorkflowMetricsReindexer(key);

		IndexReindexer indexReindexer =
			(IndexReindexer)workflowMetricsReindexer;

		indexReindexer.reindex(companyId, executionMode);
	}

	private Query _tasksQuery(long taskId) {
		return QueriesUtil.nested(
			"tasks", QueriesUtil.term("tasks.taskId", taskId));
	}

	private Query _templateQuery(String idFieldName) {
		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addMustQueryClauses(
			QueriesUtil.term(idFieldName, 0L),
			QueriesUtil.term("deleted", false));

		return booleanQuery;
	}

	@Inject
	private IndexNameBuilder _indexNameBuilder;

	@Inject
	private WorkflowMetricsReindexerRegistry _workflowMetricsReindexerRegistry;

}