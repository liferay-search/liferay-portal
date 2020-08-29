
package com.liferay.portal.search.tuning.blueprints.response.internal.result.builder;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.webserver.WebServerServletTokenUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.poc.util.POCMockLinkUtil;
import com.liferay.portal.search.tuning.blueprints.response.constants.JSONResponseAttributes;
import com.liferay.portal.search.tuning.blueprints.response.spi.result.ResultBuilder;

import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.WindowState;

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
	service = ResultBuilder.class
)
public class JournalArticleResultBuilder
	extends BaseResultBuilder implements ResultBuilder {

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

	@Override
	public String getViewURL(
			SearchRequestContext searchRequestContext,
			Map<String, Object> responseAttributes, Document document)
		throws Exception {

		PortletRequest portletRequest = (PortletRequest)responseAttributes.get(
			JSONResponseAttributes.PORTLET_REQUEST);

		PortletResponse portletResponse =
			(PortletResponse)responseAttributes.get(
				JSONResponseAttributes.PORTLET_RESPONSE);

		if ((portletRequest == null) || (portletResponse == null)) {
			return StringPool.BLANK;
		}

		boolean viewResultsInContext = GetterUtil.getBoolean(
			responseAttributes.get(JSONResponseAttributes.VIEW_IN_CONTEXT));

		String viewURL = null;

		if (viewResultsInContext) {
			viewURL = 
				getAssetRenderer(
					document
				).getURLViewInContext(
					_portal.getLiferayPortletRequest(portletRequest),
					_portal.getLiferayPortletResponse(portletResponse), null
				);
		}

		// TODO: POC

		if (Validator.isBlank(viewURL)) {
			
			viewURL = POCMockLinkUtil.
					getNotLayoutBoundJournalArticleUrl(portletRequest, _getJournalArticle(document));
		}
			
		return viewURL;
	}

	private JournalArticle _getJournalArticle(Document document)
		throws PortalException {

		return _journalArticleService.getLatestArticle(
			document.getLong(Field.ENTRY_CLASS_PK));
	}

	@Reference
	private JournalArticleService _journalArticleService;

	@Reference
	private Portal _portal;
}