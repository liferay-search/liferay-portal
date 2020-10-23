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

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilderFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = MisspellingsDefinitionToDocumentTranslator.class)
public class MisspellingsDefinitionToDocumentTranslatorImpl
	implements MisspellingsDefinitionToDocumentTranslator {

	@Override
	public Document translate(MisspellingsDefinition misspellingsDefinition) {
		return _documentBuilderFactory.builder(
		).setLong(
			MisspellingsDefinitionFields.COMPANY_ID,
			misspellingsDefinition.getCompanyId()
		).setDate(
			MisspellingsDefinitionFields.CREATED,
			parseDateString(misspellingsDefinition.getCreated())
		).setLong(
			MisspellingsDefinitionFields.GROUP_ID,
			misspellingsDefinition.getGroupId()
		).setValue(
			MisspellingsDefinitionFields.MAPPINGS,
			misspellingsDefinition.getMappingsJSONObject()
		).setDate(
			MisspellingsDefinitionFields.MODIFIED,
			parseDateString(misspellingsDefinition.getModified())
		).setString(
			MisspellingsDefinitionFields.NAME, misspellingsDefinition.getName()
		).setLong(
			MisspellingsDefinitionFields.USER_ID,
			misspellingsDefinition.getUserId()
		).setString(
			MisspellingsDefinitionFields.UID,
			misspellingsDefinition.getMisspellingsDefinitionId()
		).build();
	}

	protected String parseDateString(Date date) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

			return dateFormat.format(date);
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);
		}

		return StringPool.BLANK;
	}

	@Reference(unbind = "-")
	protected void setDocumentBuilderFactory(
		DocumentBuilderFactory documentBuilderFactory) {

		_documentBuilderFactory = documentBuilderFactory;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MisspellingsDefinitionToDocumentTranslatorImpl.class);

	private DocumentBuilderFactory _documentBuilderFactory;

}