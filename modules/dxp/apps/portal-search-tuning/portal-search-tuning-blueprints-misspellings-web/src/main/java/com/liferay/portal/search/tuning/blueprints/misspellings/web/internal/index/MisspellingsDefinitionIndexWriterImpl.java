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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index;

import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentResponse;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingsDefinitionIndexName;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = MisspellingsDefinitionIndexWriter.class)
public class MisspellingsDefinitionIndexWriterImpl
	implements MisspellingsDefinitionIndexWriter {

	@Override
	public String create(
		MisspellingsDefinitionIndexName misspellingsDefinitionIndexName,
		MisspellingsDefinition misspellings) {

		IndexDocumentRequest documentRequest = new IndexDocumentRequest(
			misspellingsDefinitionIndexName.getIndexName(),
			_misspellingsToDocumentTranslator.translate(misspellings));

		documentRequest.setRefresh(true);

		IndexDocumentResponse indexDocumentResponse =
			_searchEngineAdapter.execute(documentRequest);

		return indexDocumentResponse.getUid();
	}

	@Override
	public void remove(
		MisspellingsDefinitionIndexName misspellingsDefinitionIndexName,
		String uid) {

		DeleteDocumentRequest deleteDocumentRequest = new DeleteDocumentRequest(
			misspellingsDefinitionIndexName.getIndexName(), uid);

		deleteDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(deleteDocumentRequest);
	}

	@Override
	public void update(
		MisspellingsDefinitionIndexName misspellingsDefinitionIndexName,
		MisspellingsDefinition misspellingsDefinition) {

		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			misspellingsDefinitionIndexName.getIndexName(),
			misspellingsDefinition.getMisspellingsDefinitionId(),
			_misspellingsToDocumentTranslator.translate(
				misspellingsDefinition));

		indexDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(indexDocumentRequest);
	}

	@Reference
	private MisspellingsDefinitionToDocumentTranslator
		_misspellingsToDocumentTranslator;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}