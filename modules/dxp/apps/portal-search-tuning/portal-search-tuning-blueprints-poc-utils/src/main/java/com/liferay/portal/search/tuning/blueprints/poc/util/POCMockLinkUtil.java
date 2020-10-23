package com.liferay.portal.search.tuning.blueprints.poc.util;

import com.liferay.asset.publisher.constants.AssetPublisherPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.List;

public class POCMockLinkUtil {

	// To create links on search results, 
	// please create this page and add an AP there.
	
	public static final String ASSET_PUBLISHER_PAGE = "/viewasset";
	
	public static String getNotLayoutBoundJournalArticleUrl(
			LiferayPortletRequest liferayPortletRequest, JournalArticle journalArticle) {

		ThemeDisplay themeDisplay = (ThemeDisplay)liferayPortletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
		
		try {
			Layout layout = getLayoutByFriendlyURL(
				liferayPortletRequest, ASSET_PUBLISHER_PAGE);

			String assetPublisherInstanceId =
				findDefaultAssetPublisherInstanceId(layout);
	
			StringBundler sb = new StringBundler();
	
			sb.append(PortalUtil.getLayoutFriendlyURL(layout, themeDisplay));
			sb.append("/-/asset_publisher/");
			sb.append(assetPublisherInstanceId);
			sb.append("/content/");
			sb.append(journalArticle.getUrlTitle());
			sb.append("?_");
			sb.append(AssetPublisherPortletKeys.ASSET_PUBLISHER);
			sb.append("_INSTANCE_");
			sb.append(assetPublisherInstanceId);
			sb.append("_groupId=");
			sb.append(journalArticle.getGroupId());
	
			return sb.toString();
			
		} catch (PortalException e) {
			_log.error("Please add a page \"/viewasset\" and an AP instance there.");
		}
		
		return null;

	}
	
	public static String findDefaultAssetPublisherInstanceId(Layout layout)
			throws PortalException {

			LayoutTypePortlet layoutType =
				(LayoutTypePortlet)layout.getLayoutType();

			List<Portlet> portlets = layoutType.getAllPortlets();

			for (Portlet p : portlets) {
				if (AssetPublisherPortletKeys.ASSET_PUBLISHER.equals(
						p.getRootPortletId())) {

					return p.getInstanceId();
				}
			}

			throw new PortalException(
				"Couldn't find asset publisher on page " + layout.getFriendlyURL() +
					". Please check configuration.");
		}

		public static String getCurrentLayoutFriendlyURL(
				LiferayPortletRequest liferayPortletRequest)
			throws PortalException {

			ThemeDisplay themeDisplay = (ThemeDisplay)liferayPortletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

			Layout selectedLayout = LayoutLocalServiceUtil.getLayout(
				themeDisplay.getPlid());

			return PortalUtil.getLayoutFriendlyURL(selectedLayout, themeDisplay);
		}

		public static Layout getLayoutByFriendlyURL(
				LiferayPortletRequest liferayPortletRequest, String layoutFriendlyURL)
			throws PortalException {

			ThemeDisplay themeDisplay = (ThemeDisplay)liferayPortletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

			if (layoutFriendlyURL == null) {
				throw new PortalException(
					"Default asset publisher page is not defined. Check Liferay GSearch configuration.");
			}

			if (layoutFriendlyURL.startsWith("/group/") ||
				layoutFriendlyURL.startsWith("/web/")) {

				boolean isPrivate = false;

				if (layoutFriendlyURL.startsWith("/group/")) {
					isPrivate = true;
				}

				int position1 = layoutFriendlyURL.indexOf("/", 1);

				int position2 = layoutFriendlyURL.indexOf("/", position1);

				int position3 = position1 + position2 + 1;

				String groupFriendlyURL = layoutFriendlyURL.substring(
					position1, position3);

				Group group = GroupLocalServiceUtil.getFriendlyURLGroup(
					themeDisplay.getCompanyId(), groupFriendlyURL);

				layoutFriendlyURL = layoutFriendlyURL.substring(position3);

				return LayoutLocalServiceUtil.getFriendlyURLLayout(
					group.getGroupId(), isPrivate, layoutFriendlyURL);
			}

			Layout layout = themeDisplay.getLayout();

			return LayoutLocalServiceUtil.getFriendlyURLLayout(
				themeDisplay.getScopeGroupId(), layout.isPrivateLayout(),
				layoutFriendlyURL);
		}
	
		private static final Log _log = LogFactoryUtil.getLog(
				POCMockLinkUtil.class);

}
