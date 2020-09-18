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

package com.liferay.portal.search.tuning.blueprints.response.internal.result.builder;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.webserver.WebServerServletTokenUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.poc.util.POCMockLinkUtil;
import com.liferay.portal.search.tuning.blueprints.response.spi.result.ResultBuilder;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
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
			Document document, BlueprintsAttributes blueprintsAttributes)
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
			Document document, BlueprintsAttributes blueprintsAttributes)
		throws Exception, PortalException {

		PortletRequest portletRequest = getPortletRequest(blueprintsAttributes);

		PortletResponse portletResponse = getPortletResponse(
			blueprintsAttributes);

		if ((portletRequest == null) || (portletResponse == null)) {
			return StringPool.BLANK;
		}

		boolean viewInContext = isViewInContext(blueprintsAttributes);

		String viewURL = null;

		LiferayPortletRequest liferayPortletRequest =
			_portal.getLiferayPortletRequest(portletRequest);

		if (viewInContext) {
			viewURL = getAssetRenderer(
				document
			).getURLViewInContext(
				liferayPortletRequest,
				_portal.getLiferayPortletResponse(portletResponse), null
			);
		}

		// TODO: https://issues.liferay.com/browse/LPS-119744

		if (Validator.isBlank(viewURL)) {
			viewURL = POCMockLinkUtil.getNotLayoutBoundJournalArticleUrl(
				liferayPortletRequest, _getJournalArticle(document));
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