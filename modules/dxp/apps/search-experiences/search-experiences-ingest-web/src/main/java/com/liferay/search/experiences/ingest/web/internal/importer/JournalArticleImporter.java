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

package com.liferay.search.experiences.ingest.web.internal.importer;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PwdGenerator;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.text.BreakIterator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(enabled = false, service = JournalArticleImporter.class)
public class JournalArticleImporter {

	public JournalArticle importBasicWebContentJournalArticle(
			String content, ServiceContext serviceContext, String title)
		throws PortalException {

		if (_log.isInfoEnabled()) {
			_log.info("Add " + title);
		}

		return _journalArticleLocalService.addArticle(
			StringUtil.randomString(10), serviceContext.getUserId(),
			serviceContext.getScopeGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			HashMapBuilder.put(
				serviceContext.getLocale(), title
			).build(),
			HashMapBuilder.put(
				serviceContext.getLocale(), _createDescription(content)
			).build(),
			_createBasicWebContentContentXML(
				content, serviceContext.getLanguageId()),
			_basicWebContentDDMStructureId, "BASIC-WEB-CONTENT",
			serviceContext);
	}

	public JournalArticle updateJournalArticle(JournalArticle journalArticle) {
		return _journalArticleLocalService.updateJournalArticle(journalArticle);
	}

	@Activate
	protected void activate() {
		_basicWebContentDDMStructureId = _getBasicWebContentDDMStructureId();
	}

	private String _createBasicWebContentContentXML(
		String content, String languageId) {

		StringBundler sb = new StringBundler(13);

		sb.append("<root available-locales=\"en_US\" default-locale=\"");
		sb.append(languageId);
		sb.append("\">");
		sb.append("<dynamic-element name=\"content\" type=\"text_area\" ");
		sb.append("index-type=\"text\" instance-id=\"");
		sb.append(_generateInstanceId());
		sb.append("\">");
		sb.append("<dynamic-content language-id=\"");
		sb.append(languageId);
		sb.append("\"><![CDATA[");
		sb.append(content);
		sb.append("]]></dynamic-content></dynamic-element>");
		sb.append("</root>");

		return sb.toString();
	}

	private String _createDescription(String content) {
		content = HtmlUtil.stripHtml(content);

		if (Validator.isBlank(content)) {
			return StringPool.BLANK;
		}

		BreakIterator breakIterator = BreakIterator.getSentenceInstance();

		breakIterator.setText(content);

		if (content.length() > _DESCRIPTION_MAX_LENGTH) {
			return content.substring(0, breakIterator.preceding(500));
		}

		return content;
	}

	private String _generateInstanceId() {
		StringBuilder instanceId = new StringBuilder(8);

		String key = PwdGenerator.KEY1 + PwdGenerator.KEY2 + PwdGenerator.KEY3;

		for (int i = 0; i < 8; i++) {
			int pos = (int)Math.floor(Math.random() * key.length());

			instanceId.append(key.charAt(pos));
		}

		return instanceId.toString();
	}

	private long _getBasicWebContentDDMStructureId() {
		try {
			Group globalGroup = _groupLocalService.getCompanyGroup(
				_portal.getDefaultCompanyId());

			DDMStructure ddmStructure = _ddmStructureLocalService.getStructure(
				globalGroup.getGroupId(),
				_portal.getClassNameId(JournalArticle.class.getName()),
				"BASIC-WEB-CONTENT", true);

			return ddmStructure.getStructureId();
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	private static final int _DESCRIPTION_MAX_LENGTH = 500;

	private static final Log _log = LogFactoryUtil.getLog(
		JournalArticleImporter.class);

	private static long _basicWebContentDDMStructureId;

	@Reference
	private DDMStructureLocalService _ddmStructureLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	@Reference
	private Portal _portal;

}