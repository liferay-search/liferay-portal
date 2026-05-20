/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.internal.resource.v1_0;

import com.liferay.headless.delivery.dto.v1_0.ContentSetProvider;
import com.liferay.headless.delivery.resource.v1_0.ContentSetProviderResource;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.portal.events.ServicePreAction;
import com.liferay.portal.events.ThemeServicePreAction;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.servlet.DummyHttpServletResponse;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Luis Ortiz
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/content-set-provider.properties",
	scope = ServiceScope.PROTOTYPE, service = ContentSetProviderResource.class
)
public class ContentSetProviderResourceImpl
	extends BaseContentSetProviderResourceImpl {

	@Override
	public Page<ContentSetProvider> getAssetLibraryContentSetProvidersPage(
			Long assetLibraryId, String itemType, String keywords,
			Pagination pagination)
		throws Exception {

		return _getContentSetProvidersPage(
			assetLibraryId, itemType, keywords, pagination);
	}

	@Override
	public Page<ContentSetProvider> getSiteContentSetProvidersPage(
			Long siteId, String itemType, String keywords,
			Pagination pagination)
		throws Exception {

		return _getContentSetProvidersPage(
			siteId, itemType, keywords, pagination);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private List<InfoCollectionProvider<?>> _getAllInfoCollectionProviders(
		String itemType) {

		Class<InfoCollectionProvider<?>> clazz =
			(Class<InfoCollectionProvider<?>>)
				(Class<?>)InfoCollectionProvider.class;

		if (Validator.isNotNull(itemType)) {
			return (List)_infoItemServiceRegistry.getAllInfoItemServices(
				clazz, itemType);
		}

		return (List)_infoItemServiceRegistry.getAllInfoItemServices(clazz);
	}

	private Page<ContentSetProvider> _getContentSetProvidersPage(
			Long groupId, String itemType, String keywords,
			Pagination pagination)
		throws Exception {

		ServiceContextThreadLocal.pushServiceContext(
			_getServiceContext(groupId));

		try {
			Locale locale = contextAcceptLanguage.getPreferredLocale();

			String lowerKeywords =
				Validator.isNotNull(keywords) ?
					StringUtil.toLowerCase(keywords) : null;

			List<InfoCollectionProvider<?>> infoCollectionProviders =
				ListUtil.sort(
					ListUtil.filter(
						_getAllInfoCollectionProviders(itemType),
						infoCollectionProvider ->
							infoCollectionProvider.isAvailable() &&
							_matchesKeywords(
								infoCollectionProvider, lowerKeywords, locale)),
					Comparator.comparing(
						infoCollectionProvider ->
							infoCollectionProvider.getLabel(locale),
						String.CASE_INSENSITIVE_ORDER));

			int totalCount = infoCollectionProviders.size();

			int start = Math.min(pagination.getStartPosition(), totalCount);
			int end = Math.min(pagination.getEndPosition(), totalCount);

			return Page.of(
				transform(
					infoCollectionProviders.subList(start, end),
					infoCollectionProvider -> _toContentSetProvider(
						infoCollectionProvider, locale)),
				pagination, totalCount);
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	private ServiceContext _getServiceContext(Long groupId) throws Exception {
		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(contextCompany.getCompanyId());
		serviceContext.setRequest(contextHttpServletRequest);
		serviceContext.setScopeGroupId(groupId);
		serviceContext.setUserId(contextUser.getUserId());

		_initThemeDisplay(groupId);

		return serviceContext;
	}

	private void _initThemeDisplay(Long groupId) throws Exception {
		ThemeDisplay themeDisplay =
			(ThemeDisplay)contextHttpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if (themeDisplay != null) {
			return;
		}

		ServicePreAction servicePreAction = new ServicePreAction();

		HttpServletResponse httpServletResponse =
			new DummyHttpServletResponse();

		servicePreAction.servicePre(
			contextHttpServletRequest, httpServletResponse, false);

		ThemeServicePreAction themeServicePreAction =
			new ThemeServicePreAction();

		themeServicePreAction.run(
			contextHttpServletRequest, httpServletResponse);

		themeDisplay = (ThemeDisplay)contextHttpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		themeDisplay.setScopeGroupId(groupId);
		themeDisplay.setSiteGroupId(groupId);
	}

	private boolean _matchesKeywords(
		InfoCollectionProvider<?> infoCollectionProvider, String keywords,
		Locale locale) {

		if (keywords == null) {
			return true;
		}

		String label = StringUtil.toLowerCase(
			infoCollectionProvider.getLabel(locale));

		return label.contains(keywords);
	}

	private ContentSetProvider _toContentSetProvider(
			InfoCollectionProvider<?> infoCollectionProvider, Locale locale)
		throws Exception {

		return _contentSetProviderDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				null, infoCollectionProvider.getKey(), locale, contextUriInfo,
				contextUser),
			infoCollectionProvider);
	}

	@Reference(
		target = "(component.name=com.liferay.headless.delivery.internal.dto.v1_0.converter.ContentSetProviderDTOConverter)"
	)
	private DTOConverter<InfoCollectionProvider<?>, ContentSetProvider>
		_contentSetProviderDTOConverter;

	@Reference
	private InfoItemServiceRegistry _infoItemServiceRegistry;

}