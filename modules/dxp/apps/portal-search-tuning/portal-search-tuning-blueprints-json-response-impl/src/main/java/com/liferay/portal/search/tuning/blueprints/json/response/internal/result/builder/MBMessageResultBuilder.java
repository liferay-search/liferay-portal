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

package com.liferay.portal.search.tuning.blueprints.json.response.internal.result.builder;

import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.json.response.internal.util.ResponseUtil;
import com.liferay.portal.search.tuning.blueprints.response.spi.result.ResultBuilder;
import com.liferay.wiki.model.WikiPage;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.WindowState;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "model.class.name=com.liferay.message.boards.model.MBMessage",
	service = ResultBuilder.class
)
public class MBMessageResultBuilder
	extends BaseResultBuilder implements ResultBuilder {

	@Override
	public String getTitle(
			Document document, BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		String title = getStringFieldContent(
			document, Field.CONTENT, blueprintsAttributes.getLocale());

		return ResponseUtil.stripHTML(title, -1);
	}

	@Override
	public String getViewURL(
			Document document, BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		PortletRequest portletRequest = getPortletRequest(blueprintsAttributes);
		PortletResponse portletResponse = getPortletResponse(
			blueprintsAttributes);

		if ((portletRequest == null) || (portletResponse == null)) {
			return StringPool.BLANK;
		}

		boolean viewInContext = isViewInContext(blueprintsAttributes);

		long classNameId = document.getLong(Field.CLASS_NAME_ID);

		long classPK = document.getLong(Field.CLASS_PK);

		if (classNameId > 0) {
			String className = _portal.getClassName(classNameId);

			LiferayPortletRequest liferayPortletRequest =
				_portal.getLiferayPortletRequest(portletRequest);

			LiferayPortletResponse liferayPortletResponse =
				_portal.getLiferayPortletResponse(portletResponse);

			String journalArticleClassName = JournalArticle.class.getName();

			String wikiPageClassName = WikiPage.class.getName();

			if (className.equals(journalArticleClassName)) {
				return _getJournalArticleCommentLink(
					liferayPortletRequest, liferayPortletResponse, classPK,
					viewInContext);
			}
			else if (className.equals(wikiPageClassName)) {
				return _getWikiPageCommentLink(
					liferayPortletRequest, liferayPortletResponse, classPK,
					viewInContext);
			}
		}

		return super.getViewURL(document, blueprintsAttributes);
	}

	private String _getJournalArticleCommentLink(
			LiferayPortletRequest liferayPortletRequest,
			LiferayPortletResponse liferayPortletResponse, long classPK,
			boolean viewResultsInContext)
		throws Exception {

		AssetRenderer<?> assetRenderer = getAssetRenderer(
			JournalArticle.class.getName(), classPK);

		String link = null;

		if (viewResultsInContext) {
			link = assetRenderer.getURLViewInContext(
				liferayPortletRequest, liferayPortletResponse, null);
		}

		return link;
	}

	private String _getWikiPageCommentLink(
			LiferayPortletRequest liferayPortletRequest,
			LiferayPortletResponse liferayPortletResponse, long classPK,
			boolean viewResultsInContext)
		throws Exception {

		AssetRenderer<?> assetRenderer = getAssetRenderer(
			WikiPage.class.getName(), classPK);

		if (viewResultsInContext) {
			return assetRenderer.getURLViewInContext(
				liferayPortletRequest, liferayPortletResponse, "");
		}

		return assetRenderer.getURLView(
			liferayPortletResponse, WindowState.MAXIMIZED);
	}

	@Reference
	private JournalArticleService _journalArticleService;

	@Reference
	private Portal _portal;

}