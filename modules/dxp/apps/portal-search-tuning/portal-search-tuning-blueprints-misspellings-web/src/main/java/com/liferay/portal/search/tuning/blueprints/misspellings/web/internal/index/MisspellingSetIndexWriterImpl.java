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
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexName;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = MisspellingSetIndexWriter.class)
public class MisspellingSetIndexWriterImpl
	implements MisspellingSetIndexWriter {

	@Override
	public String create(
		MisspellingSetIndexName misspellingSetIndexName,
		MisspellingSet misspellingSet) {

		IndexDocumentRequest documentRequest = new IndexDocumentRequest(
			misspellingSetIndexName.getIndexName(),
			_misspellingsToDocumentTranslator.translate(misspellingSet));

		documentRequest.setRefresh(true);

		IndexDocumentResponse indexDocumentResponse =
			_searchEngineAdapter.execute(documentRequest);

		return indexDocumentResponse.getUid();
	}

	@Override
	public void remove(
		MisspellingSetIndexName misspellingSetIndexName, String id) {

		DeleteDocumentRequest deleteDocumentRequest = new DeleteDocumentRequest(
			misspellingSetIndexName.getIndexName(), id);

		deleteDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(deleteDocumentRequest);
	}

	@Override
	public void update(
		MisspellingSetIndexName misspellingSetIndexName,
		MisspellingSet misspellingSet) {

		UpdateDocumentRequest updateDocumentRequest = new UpdateDocumentRequest(
			misspellingSetIndexName.getIndexName(),
			misspellingSet.getMisspellingSetId(),
			_misspellingsToDocumentTranslator.translate(misspellingSet));

		updateDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(updateDocumentRequest);
	}

	@Reference
	private MisspellingSetToDocumentTranslator
		_misspellingsToDocumentTranslator;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}