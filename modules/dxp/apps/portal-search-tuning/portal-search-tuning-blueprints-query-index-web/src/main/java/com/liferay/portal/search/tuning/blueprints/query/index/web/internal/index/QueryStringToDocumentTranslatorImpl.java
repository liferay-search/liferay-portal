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

import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.query.index.constants.QueryStringFields;
import com.liferay.portal.search.tuning.blueprints.query.index.constants.QueryStringStatus;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.util.QueryIndexUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = QueryStringToDocumentTranslator.class)
public class QueryStringToDocumentTranslatorImpl
	implements QueryStringToDocumentTranslator {

	@Override
	public Document translate(QueryString queryString) {
		DocumentBuilder documentBuilder = _documentBuilderFactory.builder();

		if (queryString.getCompanyId() != null) {
			documentBuilder.setLong(
				QueryStringFields.COMPANY_ID, queryString.getCompanyId());
		}

		if (queryString.getContent() != null) {
			documentBuilder.setString(
				QueryStringFields.CONTENT, queryString.getContent());
		}

		if (queryString.getCreated() != null) {
			documentBuilder.setDate(
				QueryStringFields.CREATED,
				QueryIndexUtil.toIndexDateString(queryString.getCreated()));
		}

		if (queryString.getGroupId() != null) {
			documentBuilder.setLong(
				QueryStringFields.GROUP_ID, queryString.getGroupId());
		}

		if (queryString.getHitCount() != null) {
			documentBuilder.setLong(
				QueryStringFields.HIT_COUNT, queryString.getHitCount());
		}

		if (queryString.getQueryStringId() != null) {
			documentBuilder.setValue(
				QueryStringFields.ID, queryString.getQueryStringId());
		}

		if (queryString.getLanguageId() != null) {
			documentBuilder.setString(
				QueryStringFields.LANGUAGE_ID, queryString.getLanguageId());
		}

		if (queryString.getLastAccessed() != null) {
			documentBuilder.setDate(
				QueryStringFields.LAST_ACCESSED,
				QueryIndexUtil.toIndexDateString(
					queryString.getLastAccessed()));
		}

		if (queryString.getLastReported() != null) {
			documentBuilder.setDate(
				QueryStringFields.LAST_REPORTED,
				QueryIndexUtil.toIndexDateString(
					queryString.getLastReported()));
		}

		if (queryString.getModified() != null) {
			documentBuilder.setDate(
				QueryStringFields.MODIFIED,
				QueryIndexUtil.toIndexDateString(queryString.getModified()));
		}

		if (queryString.getReportCount() != null) {
			documentBuilder.setLong(
				QueryStringFields.REPORT_COUNT, queryString.getReportCount());
		}

		if (queryString.getStatus() != null) {
			QueryStringStatus status = queryString.getStatus();

			documentBuilder.setString(QueryStringFields.STATUS, status.name());
		}

		if (queryString.getStatusDate() != null) {
			documentBuilder.setDate(
				QueryStringFields.STATUS_DATE,
				QueryIndexUtil.toIndexDateString(queryString.getStatusDate()));
		}

		if (QueryIndexUtil.isAnalyzedLanguage(queryString.getLanguageId())) {
			documentBuilder.setString(
				QueryIndexUtil.getLocalizedFieldName(
					QueryStringFields.CONTENT, queryString.getLanguageId()),
				queryString.getContent());
		}

		return documentBuilder.build();
	}

	@Reference
	private DocumentBuilderFactory _documentBuilderFactory;

}