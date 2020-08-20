
package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.result.item.builder;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.webserver.WebServerServletTokenUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.tuning.blueprints.engine.constants.JSONResponseAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.results.item.ResultItemBuilder;

import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * JournalArticle item type result builder.
 *
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "model.class.name=com.liferay.journal.model.JournalArticle",
	service = ResultItemBuilder.class
)
public class JournalArticleResultItemBuilder
	extends BaseResultItemBuilder implements ResultItemBuilder {

	@Override
	public String getThumbnail(
			SearchRequestContext queryContext,
			Map<String, Object> responseAttributes, Document document)
		throws Exception {

		long smallImageId = _getJournalArticle(
			document
		).getSmallImageId();

		StringBundler sb = new StringBundler(4);

		sb.append("/image/journal/article?img_id=");
		sb.append(String.valueOf(smallImageId));
		sb.append("&t=");
		sb.append(WebServerServletTokenUtil.getToken(smallImageId));

		return sb.toString();
	}

	private JournalArticle _getJournalArticle(Document document)
		throws PortalException {

		return _journalArticleService.getLatestArticle(
			document.getLong(Field.ENTRY_CLASS_PK));
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


		StringBundler sb = new StringBundler();

		if (viewResultsInContext) {
			sb.append(
				getAssetRenderer(
					document
				).getURLViewInContext(
					(LiferayPortletRequest)portletRequest,
					(LiferayPortletResponse)portletResponse, null
				));
		}
		/*

		if ((sb.length() == 0) || sb.toString().equals("null")) {

			String assetPublisherPageURL = getAssetPublisherPageURL();

			if (assetPublisherPageURL == null) {
				return null;
			}

			// It can happen that there's a string "null".

			sb = new StringBundler();

			sb.append(
				getNotLayoutBoundJournalArticleUrl(
					portletRequest, getJournalArticle(document),
					assetPublisherPageURL));
		}
		*/

		return sb.toString();
	}
	@Reference
	private JournalArticleService _journalArticleService;

}