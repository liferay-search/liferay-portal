/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.internal.dto.v1_0.converter;

import com.liferay.headless.delivery.dto.v1_0.ContentSetProvider;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.collection.provider.SingleFormVariationInfoCollectionProvider;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Ortiz
 */
@Component(
	property = "dto.class.name=com.liferay.info.collection.provider.InfoCollectionProvider",
	service = DTOConverter.class
)
public class ContentSetProviderDTOConverter
	implements DTOConverter<InfoCollectionProvider<?>, ContentSetProvider> {

	@Override
	public String getContentType() {
		return ContentSetProvider.class.getSimpleName();
	}

	@Override
	public ContentSetProvider toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		return toDTO(
			dtoConverterContext,
			_infoItemServiceRegistry.getInfoItemService(
				InfoCollectionProvider.class,
				String.valueOf(dtoConverterContext.getId())));
	}

	@Override
	public ContentSetProvider toDTO(
			DTOConverterContext dtoConverterContext,
			InfoCollectionProvider<?> infoCollectionProvider)
		throws Exception {

		if (infoCollectionProvider == null) {
			return null;
		}

		Locale locale = dtoConverterContext.getLocale();

		return new ContentSetProvider() {
			{
				setItemSubtype(
					() -> {
						if (!(infoCollectionProvider instanceof
								SingleFormVariationInfoCollectionProvider)) {

							return null;
						}

						SingleFormVariationInfoCollectionProvider<?>
							singleFormVariationInfoCollectionProvider =
								(SingleFormVariationInfoCollectionProvider<?>)
									infoCollectionProvider;

						return singleFormVariationInfoCollectionProvider.
							getFormVariationKey();
					});
				setItemType(infoCollectionProvider::getCollectionItemClassName);
				setKey(infoCollectionProvider::getKey);
				setTitle(() -> infoCollectionProvider.getLabel(locale));
			}
		};
	}

	@Reference
	private InfoItemServiceRegistry _infoItemServiceRegistry;

}