/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.search.engine.adapter.document;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.DeleteOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.bulk.OperationType;
import co.elastic.clients.elasticsearch.core.bulk.UpdateOperation;
import co.elastic.clients.json.JsonData;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.search.elasticsearch8.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.elasticsearch8.internal.util.JsonpUtil;
import com.liferay.portal.search.elasticsearch8.internal.util.SetterUtil;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentItemResponse;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.BulkableDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * @author Michael C. Han
 */
public class BulkDocumentRequestExecutor {

	public BulkDocumentRequestExecutor(
		ElasticsearchClientResolver elasticsearchClientResolver,
		int numberOfTries, int waitInSeconds) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
		_numberOfTries = numberOfTries;
		_waitInSeconds = waitInSeconds;
	}

	public BulkDocumentResponse execute(
		BulkDocumentRequest bulkDocumentRequest) {

		List<BulkDocumentItemResponse> bulkDocumentItemResponses =
			new ArrayList<>();
		boolean errors = false;
		long took = 0;

		Deque<List<BulkableDocumentRequest<?>>>
			pendingBulkableDocumentRequestsBatches = new ArrayDeque<>();

		pendingBulkableDocumentRequestsBatches.addFirst(
			new ArrayList<>(bulkDocumentRequest.getBulkableDocumentRequests()));

		while (!pendingBulkableDocumentRequestsBatches.isEmpty()) {
			List<BulkableDocumentRequest<?>> currentBulkableDocumentRequests =
				pendingBulkableDocumentRequestsBatches.pollFirst();

			BulkDocumentRequest partialBulkDocumentRequest =
				_createPartialBulkDocumentRequest(
					bulkDocumentRequest, currentBulkableDocumentRequests);

			BulkResponse bulkResponse = _getBulkResponse(
				partialBulkDocumentRequest,
				createBulkRequest(partialBulkDocumentRequest));

			JsonpUtil.logBulkResponse(bulkResponse, _log);

			took += bulkResponse.took();

			List<BulkResponseItem> bulkResponseItems = bulkResponse.items();

			List<BulkableDocumentRequest<?>> rejectedBulkableDocumentRequests =
				new ArrayList<>();
			String rejectionReason = null;

			for (int i = 0; i < bulkResponseItems.size(); i++) {
				BulkResponseItem bulkResponseItem = bulkResponseItems.get(i);

				ErrorCause errorCause = bulkResponseItem.error();

				if ((errorCause != null) &&
					(bulkResponseItem.status() ==
						_HTTP_STATUS_TOO_MANY_REQUESTS)) {

					rejectedBulkableDocumentRequests.add(
						currentBulkableDocumentRequests.get(i));

					if (rejectionReason == null) {
						rejectionReason = errorCause.reason();
					}
				}
				else {
					bulkDocumentItemResponses.add(
						_buildBulkDocumentItemResponse(bulkResponseItem));

					if (errorCause != null) {
						errors = true;
					}
				}
			}

			if (rejectedBulkableDocumentRequests.isEmpty()) {
				continue;
			}

			if (currentBulkableDocumentRequests.size() == 1) {
				throw new RuntimeException(
					"Unable to index bulk operation rejected by the search " +
						"engine: " + rejectionReason);
			}

			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Retrying ", rejectedBulkableDocumentRequests.size(),
						" of ", currentBulkableDocumentRequests.size(),
						" bulk operations rejected by the search engine: ",
						rejectionReason));
			}

			try {
				Thread.sleep(_waitInSeconds * Time.SECOND);
			}
			catch (InterruptedException interruptedException) {
				Thread.currentThread(
				).interrupt();

				throw new RuntimeException(interruptedException);
			}

			int splitIndex = rejectedBulkableDocumentRequests.size() / 2;

			pendingBulkableDocumentRequestsBatches.addFirst(
				new ArrayList<>(
					rejectedBulkableDocumentRequests.subList(
						splitIndex, rejectedBulkableDocumentRequests.size())));
			pendingBulkableDocumentRequestsBatches.addFirst(
				new ArrayList<>(
					rejectedBulkableDocumentRequests.subList(0, splitIndex)));
		}

		BulkDocumentResponse bulkDocumentResponse = new BulkDocumentResponse(
			took);

		if (errors) {
			bulkDocumentResponse.setErrors(true);
		}

		for (BulkDocumentItemResponse bulkDocumentItemResponse :
				bulkDocumentItemResponses) {

			bulkDocumentResponse.addBulkDocumentItemResponse(
				bulkDocumentItemResponse);
		}

		return bulkDocumentResponse;
	}

	protected BulkRequest createBulkRequest(
		BulkDocumentRequest bulkDocumentRequest) {

		BulkRequest.Builder builder = new BulkRequest.Builder();

		if (bulkDocumentRequest.isRefresh()) {
			builder.refresh(Refresh.True);
		}

		for (BulkableDocumentRequest<?> bulkableDocumentRequest :
				bulkDocumentRequest.getBulkableDocumentRequests()) {

			if (bulkableDocumentRequest instanceof DeleteDocumentRequest) {
				DeleteOperation deleteOperation =
					ElasticsearchBulkableDocumentRequestTranslatorUtil.
						translate(
							(DeleteDocumentRequest)bulkableDocumentRequest);

				builder.operations(new BulkOperation(deleteOperation));
			}
			else if (bulkableDocumentRequest instanceof IndexDocumentRequest) {
				IndexOperation<JsonData> indexOperation =
					ElasticsearchBulkableDocumentRequestTranslatorUtil.
						translate(
							(IndexDocumentRequest)bulkableDocumentRequest);

				builder.operations(new BulkOperation(indexOperation));
			}
			else if (bulkableDocumentRequest instanceof UpdateDocumentRequest) {
				UpdateOperation<JsonData, JsonData> updateOperation =
					ElasticsearchBulkableDocumentRequestTranslatorUtil.
						translate(
							(UpdateDocumentRequest)bulkableDocumentRequest);

				builder.operations(new BulkOperation(updateOperation));
			}
		}

		return builder.build();
	}

	private BulkDocumentItemResponse _buildBulkDocumentItemResponse(
		BulkResponseItem bulkResponseItem) {

		BulkDocumentItemResponse bulkDocumentItemResponse =
			new BulkDocumentItemResponse();

		bulkDocumentItemResponse.setId(bulkResponseItem.id());
		bulkDocumentItemResponse.setIndex(bulkResponseItem.index());
		bulkDocumentItemResponse.setStatus(bulkResponseItem.status());
		bulkDocumentItemResponse.setType(
			_getType(bulkResponseItem.operationType()));

		SetterUtil.setNotNullLong(
			bulkDocumentItemResponse::setVersion, bulkResponseItem.version());

		ErrorCause errorCause = bulkResponseItem.error();

		if (errorCause != null) {
			if (errorCause.causedBy() != null) {
				ErrorCause causedByErrorCause = errorCause.causedBy();

				bulkDocumentItemResponse.setFailureMessage(
					causedByErrorCause.reason());
				bulkDocumentItemResponse.setCause(
					new Exception(JsonpUtil.toString(causedByErrorCause)));
			}
			else {
				bulkDocumentItemResponse.setFailureMessage(errorCause.reason());
				bulkDocumentItemResponse.setCause(
					new Exception(JsonpUtil.toString(errorCause)));
			}
		}

		return bulkDocumentItemResponse;
	}

	private BulkDocumentRequest _createPartialBulkDocumentRequest(
		BulkDocumentRequest bulkDocumentRequest,
		List<BulkableDocumentRequest<?>> bulkableDocumentRequests) {

		BulkDocumentRequest partialBulkDocumentRequest =
			new BulkDocumentRequest();

		partialBulkDocumentRequest.setConnectionId(
			bulkDocumentRequest.getConnectionId());
		partialBulkDocumentRequest.setPreferLocalCluster(
			bulkDocumentRequest.isPreferLocalCluster());
		partialBulkDocumentRequest.setRefresh(bulkDocumentRequest.isRefresh());

		for (BulkableDocumentRequest<?> bulkableDocumentRequest :
				bulkableDocumentRequests) {

			partialBulkDocumentRequest.addBulkableDocumentRequest(
				bulkableDocumentRequest);
		}

		return partialBulkDocumentRequest;
	}

	private BulkResponse _getBulkResponse(
		BulkDocumentRequest bulkDocumentRequest, BulkRequest bulkRequest) {

		ElasticsearchClient elasticsearchClient =
			_elasticsearchClientResolver.getElasticsearchClient(
				bulkDocumentRequest.getConnectionId(),
				bulkDocumentRequest.isPreferLocalCluster());

		for (int i = 0;;) {
			try {
				return elasticsearchClient.bulk(bulkRequest);
			}
			catch (Exception exception) {
				if (i++ >= _numberOfTries) {
					if (_numberOfTries == 1) {
						_log.error("The retry failed to get a bulk response");
					}
					else if (_numberOfTries == 2) {
						_log.error(
							"Both retries failed to get a bulk response");
					}
					else if (_numberOfTries > 2) {
						_log.error(
							"All " + _numberOfTries +
								" retries failed to get a bulk response");
					}

					throw new RuntimeException(exception);
				}

				_log.error(
					StringBundler.concat(
						"There was an exception while getting a response from ",
						"the search engine, will retry in ", _waitInSeconds,
						" seconds (", i, "/", _numberOfTries, "). ", exception),
					exception);

				try {
					Thread.sleep(_waitInSeconds * Time.SECOND);
				}
				catch (InterruptedException interruptedException) {
					_log.error(interruptedException);

					throw new RuntimeException(exception);
				}
			}
		}
	}

	private String _getType(OperationType operationType) {
		return operationType.jsonValue();
	}

	private static final int _HTTP_STATUS_TOO_MANY_REQUESTS = 429;

	private static final Log _log = LogFactoryUtil.getLog(
		BulkDocumentRequestExecutor.class);

	private final ElasticsearchClientResolver _elasticsearchClientResolver;
	private final int _numberOfTries;
	private final int _waitInSeconds;

}