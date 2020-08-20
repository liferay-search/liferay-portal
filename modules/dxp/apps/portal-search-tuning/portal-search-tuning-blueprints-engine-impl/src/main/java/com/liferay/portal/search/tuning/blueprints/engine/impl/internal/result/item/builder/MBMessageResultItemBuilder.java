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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.result.item.builder;

import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.tuning.blueprints.engine.constants.JSONResponseAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util.ValueUtil;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.results.item.ResultItemBuilder;
import com.liferay.wiki.model.WikiPage;

import java.util.Map;

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
	service = ResultItemBuilder.class
)
public class MBMessageResultItemBuilder
	extends BaseResultItemBuilder implements ResultItemBuilder {

	@Override
	public String getTitle(
			SearchRequestContext searchRequestContext,
			Map<String, Object> responseAttributes, Document document)
		throws Exception {

		String title = getStringFieldContent(
			document, Field.CONTENT, searchRequestContext.getLocale());

		return ValueUtil.stripHTML(title, -1);
	}

	@Override
	public String getViewURL(
			SearchRequestContext searchRequestContext,
			Map<String, Object> responseAttributes, Document document)
		throws Exception {

		PortletRequest portletRequest = (PortletRequest)responseAttributes.get(
				JSONResponseAttributes.PORTLET_REQUEST);
		
	
		PortletResponse portletResponse = (PortletResponse)responseAttributes.get(
				JSONResponseAttributes.PORTLET_RESPONSE);

		if (portletRequest == null || portletResponse ==  null) {
			return StringPool.BLANK;
		}

		boolean viewResultsInContext = 
				GetterUtil.getBoolean(responseAttributes.get(
				JSONResponseAttributes.VIEW_IN_CONTEXT));


		long classNameId = 
			document.getLong(Field.CLASS_NAME_ID);

		long classPK = document.getLong(Field.CLASS_PK);

		if (classNameId > 0) {

			String assetPublisherPageURL = null; //getAssetPublisherPageURL(queryContext);

			String className = _portal.getClassName(classNameId);

			if (JournalArticle.class.getName().equals(className)) {
				return getJournalArticleCommentLink(
					portletRequest, portletResponse,
					assetPublisherPageURL, classPK, viewResultsInContext);
			}
			else if (WikiPage.class.getName().equals(className)) {
				return getWikiPageCommentLink(
					portletRequest, portletResponse, classPK,
					viewResultsInContext);
			}
		}

		return super.getViewURL(searchRequestContext, responseAttributes, document);
	}
	
	private String getDLFileEntryCommentLink() {
		return null;
	}


	private String getJournalArticleCommentLink(
			PortletRequest portletRequest, PortletResponse portletResponse,
			String assetPublisherPageFriendlyURL,
			long classPK, boolean viewResultsInContext)
		throws Exception {

		AssetRenderer<?> assetRenderer = getAssetRenderer(
			JournalArticle.class.getName(), classPK);

		String link = null;

		if (viewResultsInContext || (assetPublisherPageFriendlyURL == null)) {
			link = assetRenderer.getURLViewInContext(
				(LiferayPortletRequest)portletRequest,
				(LiferayPortletResponse)portletResponse, null);
		}

		/*
		if (Validator.isNull(link)) {
			JournalArticle journalArticle =
				_journalArticleService.getLatestArticle(classPK);

			link = getNotLayoutBoundJournalArticleUrl(
				portletRequest, journalArticle, assetPublisherPageFriendlyURL);
		}
		*/
		return link;
	}

	private String getWikiPageCommentLink(
			PortletRequest portletRequest, PortletResponse portletResponse, long classPK,
			boolean viewResultsInContext)
		throws Exception {

		AssetRenderer<?> assetRenderer = getAssetRenderer(
			WikiPage.class.getName(), classPK);

		if (viewResultsInContext) {
			return assetRenderer.getURLViewInContext(
				(LiferayPortletRequest)portletRequest,
				(LiferayPortletResponse)portletResponse, "");
		}

		return assetRenderer.getURLView(
			(LiferayPortletResponse)portletResponse, WindowState.MAXIMIZED);
	}
	@Reference
	private JournalArticleService _journalArticleService;

	@Reference
	private Portal _portal;

}