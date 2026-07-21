/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.service.internal.search.index.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
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
import com.liferay.portal.search.spi.reindexer.IndexReindexer;
import com.liferay.portal.test.rule.Inject;
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
public class WorkflowMetricsReindexerOrphanReproductionTest
	extends BaseWorkflowMetricsIndexerTestCase {

	@Test
	public void testFullReindexRemovesOrphan() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		String processIndexName =
			_indexNameBuilder.getIndexName(companyId) +
				WorkflowMetricsIndexNameConstants.SUFFIX_PROCESS;

		WorkflowMetricsReindexer workflowMetricsReindexer =
			_workflowMetricsReindexerRegistry.getWorkflowMetricsReindexer(
				"process");

		workflowMetricsReindexer.reindex(companyId);

		_indexOrphan(processIndexName, companyId);

		Assert.assertEquals(
			"Orphan should be present right after seeding", 1,
			_countOrphan(processIndexName, companyId));

		workflowMetricsReindexer.reindex(companyId);

		Assert.assertEquals(
			"FULL reindex must remove the orphan", 0,
			_countOrphan(processIndexName, companyId));
	}

	@Test
	public void testSyncReindexRemovesOrphan() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		String processIndexName =
			_indexNameBuilder.getIndexName(companyId) +
				WorkflowMetricsIndexNameConstants.SUFFIX_PROCESS;

		IndexReindexer indexReindexer =
			(IndexReindexer)
				_workflowMetricsReindexerRegistry.getWorkflowMetricsReindexer(
					"process");

		indexReindexer.reindex(companyId, IndexReindexer.ExecutionMode.FULL);

		_indexOrphan(processIndexName, companyId);

		Assert.assertEquals(
			"Orphan should be present right after seeding", 1,
			_countOrphan(processIndexName, companyId));

		Thread.sleep(1100);

		indexReindexer.reindex(companyId, IndexReindexer.ExecutionMode.SYNC);

		Assert.assertEquals(
			"SYNC reindex must remove the orphan", 0,
			_countOrphan(processIndexName, companyId));
	}

	private long _countOrphan(String indexName, long companyId) {
		searchEngineAdapter.execute(new RefreshIndexRequest(indexName));

		CountSearchRequest countSearchRequest = new CountSearchRequest();

		countSearchRequest.setIndexNames(indexName);

		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		countSearchRequest.setQuery(
			booleanQuery.addFilterQueryClauses(
				QueriesUtil.term("companyId", companyId),
				QueriesUtil.term("processId", _ORPHAN_PROCESS_ID)));

		CountSearchResponse countSearchResponse = searchEngineAdapter.execute(
			countSearchRequest);

		return countSearchResponse.getCount();
	}

	private void _indexOrphan(String indexName, long companyId) {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.builder();

		documentBuilder.setLong(
			"companyId", companyId
		).setLong(
			"processId", _ORPHAN_PROCESS_ID
		).setString(
			"uid", "orphan-" + _ORPHAN_PROCESS_ID
		).setValue(
			"deleted", false
		);

		Document document = documentBuilder.build();

		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			indexName, document.getString("uid"), document);

		indexDocumentRequest.setRefresh(true);

		searchEngineAdapter.execute(indexDocumentRequest);
	}

	private static final long _ORPHAN_PROCESS_ID = 999999999;

	@Inject
	private IndexNameBuilder _indexNameBuilder;

	@Inject
	private WorkflowMetricsReindexerRegistry _workflowMetricsReindexerRegistry;

}