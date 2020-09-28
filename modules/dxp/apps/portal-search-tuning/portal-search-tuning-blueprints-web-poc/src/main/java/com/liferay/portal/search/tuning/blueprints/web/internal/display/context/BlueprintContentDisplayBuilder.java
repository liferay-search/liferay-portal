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

package com.liferay.portal.search.tuning.blueprints.web.internal.display.context;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.tuning.blueprints.web.internal.util.BlueprintUtil;

import java.util.Locale;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author Olivia Yu
 */
public class BlueprintContentDisplayBuilder {

	@SuppressWarnings("deprecation")
	public BlueprintContentDisplayContext build() throws Exception {
		BlueprintContentDisplayContext blueprintContentDisplayContext =
			new BlueprintContentDisplayContext();

		AssetRenderer<?> assetRenderer = BlueprintUtil.getAssetRenderer(
			_entryClassName, _entryClassPK);

		if (assetRenderer == null) {
			return blueprintContentDisplayContext;
		}

		AssetRendererFactory<?> assetRendererFactory =
			assetRenderer.getAssetRendererFactory();

		blueprintContentDisplayContext.setAssetRendererFactory(
			assetRendererFactory);

		AssetEntry assetEntry = assetRendererFactory.getAssetEntry(
			_assetEntryId);

		blueprintContentDisplayContext.setAssetEntry(assetEntry);

		blueprintContentDisplayContext.setAssetRenderer(assetRenderer);

		final boolean visible;

		if ((assetEntry != null) && (assetRenderer != null) &&
			assetEntry.isVisible() &&
			assetRenderer.hasViewPermission(_permissionChecker)) {

			visible = true;
		}
		else {
			visible = false;
		}

		blueprintContentDisplayContext.setVisible(visible);

		if (visible) {
			String title = assetRenderer.getTitle(_locale);

			blueprintContentDisplayContext.setHeaderTitle(title);

			boolean hasEditPermission = assetRenderer.hasEditPermission(
				_permissionChecker);

			blueprintContentDisplayContext.setHasEditPermission(
				hasEditPermission);

			if (hasEditPermission) {
				ThemeDisplay themeDisplay =
					(ThemeDisplay)_renderRequest.getAttribute(
						WebKeys.THEME_DISPLAY);

				blueprintContentDisplayContext.setIconEditTarget(title);

				PortletURL editPortletURL = assetRenderer.getURLEdit(
					_portal.getLiferayPortletRequest(_renderRequest),
					_portal.getLiferayPortletResponse(_renderResponse));

				editPortletURL.setParameter(
					"redirect", themeDisplay.getURLCurrent());

				blueprintContentDisplayContext.setIconURLString(
					editPortletURL.toString());
			}
		}

		return blueprintContentDisplayContext;
	}

	public void setAssetEntryId(long assetEntryId) {
		_assetEntryId = assetEntryId;
	}

	public void setEntryClassName(String entryClassName) {
		_entryClassName = entryClassName;
	}

	public void setEntryClassPK(long entryClassPK) {
		_entryClassPK = entryClassPK;
	}

	public void setLocale(Locale locale) {
		_locale = locale;
	}

	public void setPermissionChecker(PermissionChecker permissionChecker) {
		_permissionChecker = permissionChecker;
	}

	public void setPortal(Portal portal) {
		_portal = portal;
	}

	public void setRenderRequest(RenderRequest renderRequest) {
		_renderRequest = renderRequest;
	}

	public void setRenderResponse(RenderResponse renderResponse) {
		_renderResponse = renderResponse;
	}

	private long _assetEntryId;
	private String _entryClassName;
	private long _entryClassPK;
	private Locale _locale;
	private PermissionChecker _permissionChecker;
	private Portal _portal;
	private RenderRequest _renderRequest;
	private RenderResponse _renderResponse;

}