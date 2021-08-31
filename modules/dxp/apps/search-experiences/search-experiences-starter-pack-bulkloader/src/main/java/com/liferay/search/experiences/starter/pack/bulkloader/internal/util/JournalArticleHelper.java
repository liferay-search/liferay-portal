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

package com.liferay.search.experiences.starter.pack.bulkloader.internal.util;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PwdGenerator;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;

import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = JournalArticleHelper.class)
public class JournalArticleHelper {

	public JournalArticle addJournalArticle(
			PortletRequest portletRequest, long userId, long groupId,
			String languageId, String title, String content,
			String[] assetTagNames)
		throws PortalException {

		if (_log.isInfoEnabled()) {
			_log.info("Adding journal article " + title);
		}

		ServiceContext serviceContext = _getServiceContext(
			portletRequest, assetTagNames);

		Locale locale = LocaleUtil.fromLanguageId(languageId);

		// Respect ES 32KB field size limit (breaks the HTML).

		if (content.length() > 32000) {
			content = content.substring(0, 32000);
		}

		return _journalArticleLocalService.addArticle(
			userId, groupId, 0,
			HashMapBuilder.put(
				locale, title
			).build(),
			HashMapBuilder.put(
				locale, _getDescription(content)
			).build(),
			_createArticleXML(content, languageId), "BASIC-WEB-CONTENT",
			"BASIC-WEB-CONTENT", serviceContext);
	}

	public String cleanTagValue(String s) {
		return StringUtil.replace(s, CharPool.UNDERLINE, CharPool.SPACE);
	}

	public boolean isValidTagValue(String value) {
		if (Validator.isBlank(value)) {
			return false;
		}

		char[] wordCharArray = value.toCharArray();

		for (char c : wordCharArray) {
			for (char invalidChar : _INVALID_CHARACTERS) {
				if (c == invalidChar) {
					return false;
				}
			}
		}

		return true;
	}

	public void updateJournalArticle(JournalArticle journalArticle) {
		_journalArticleLocalService.updateJournalArticle(journalArticle);
	}

	private String _createArticleXML(String content, String languageId) {
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

	private String _generateInstanceId() {
		StringBuilder instanceId = new StringBuilder(8);

		String key = PwdGenerator.KEY1 + PwdGenerator.KEY2 + PwdGenerator.KEY3;

		for (int i = 0; i < 8; i++) {
			int pos = (int)Math.floor(Math.random() * key.length());

			instanceId.append(key.charAt(pos));
		}

		return instanceId.toString();
	}

	private String _getDescription(String s) {
		s = HtmlUtil.stripHtml(s);

		if (s.length() > 500) {
			return s.substring(0, 500);
		}

		return s;
	}

	private ServiceContext _getServiceContext(
			PortletRequest portletRequest, String[] assetTagNames)
		throws PortalException {

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			JournalArticle.class.getName(), portletRequest);

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setAssetTagNames(assetTagNames);

		return serviceContext;
	}

	private static final char[] _INVALID_CHARACTERS = {
		CharPool.AMPERSAND, CharPool.APOSTROPHE, CharPool.AT,
		CharPool.BACK_SLASH, CharPool.CLOSE_BRACKET, CharPool.CLOSE_CURLY_BRACE,
		CharPool.COLON, CharPool.COMMA, CharPool.EQUAL, CharPool.GREATER_THAN,
		CharPool.FORWARD_SLASH, CharPool.LESS_THAN, CharPool.NEW_LINE,
		CharPool.OPEN_BRACKET, CharPool.OPEN_CURLY_BRACE, CharPool.PERCENT,
		CharPool.PIPE, CharPool.PLUS, CharPool.POUND, CharPool.PRIME,
		CharPool.QUESTION, CharPool.QUOTE, CharPool.RETURN, CharPool.SEMICOLON,
		CharPool.SLASH, CharPool.STAR, CharPool.TILDE
	};

	private static final Log _log = LogFactoryUtil.getLog(
		JournalArticleHelper.class);

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

}