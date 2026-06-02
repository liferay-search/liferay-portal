/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.internal.resource.v1_0;

import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.headless.delivery.dto.v1_0.CollectionEntry;
import com.liferay.headless.delivery.internal.odata.entity.v1_0.CollectionEntryEntityModel;
import com.liferay.headless.delivery.resource.v1_0.CollectionEntryResource;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.SearchUtil;

import jakarta.ws.rs.core.MultivaluedMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Luis Ortiz
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/collection-entry.properties",
	scope = ServiceScope.PROTOTYPE, service = CollectionEntryResource.class
)
public class CollectionEntryResourceImpl
	extends BaseCollectionEntryResourceImpl {

	@Override
	public Page<CollectionEntry> getAssetLibraryCollectionEntriesPage(
			Long assetLibraryId, String search, Filter filter,
			Pagination pagination, Sort[] sorts)
		throws Exception {

		return _getCollectionEntriesPage(
			assetLibraryId, search, filter, pagination, sorts);
	}

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap) {
		return _entityModel;
	}

	@Override
	public Page<CollectionEntry> getSiteCollectionEntriesPage(
			Long siteId, String search, Filter filter, Pagination pagination,
			Sort[] sorts)
		throws Exception {

		return _getCollectionEntriesPage(
			siteId, search, filter, pagination, sorts);
	}

	private Page<CollectionEntry> _getCollectionEntriesPage(
			Long groupId, String search, Filter filter, Pagination pagination,
			Sort[] sorts)
		throws Exception {

		return SearchUtil.search(
			null,
			booleanQuery -> {
			},
			filter, AssetListEntry.class.getName(),
			GetterUtil.getString(search), pagination,
			queryConfig -> queryConfig.setSelectedFieldNames(
				Field.ENTRY_CLASS_PK),
			searchContext -> {
				searchContext.setCompanyId(contextCompany.getCompanyId());
				searchContext.setGroupIds(new long[] {groupId});
				searchContext.setUserId(contextUser.getUserId());
			},
			sorts,
			document -> _toCollectionEntry(
				GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK))));
	}

	private CollectionEntry _toCollectionEntry(long collectionEntryId)
		throws Exception {

		return _collectionEntryDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				null, collectionEntryId,
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser));
	}

	private static final EntityModel _entityModel =
		new CollectionEntryEntityModel();

	@Reference(
		target = "(component.name=com.liferay.headless.delivery.internal.dto.v1_0.converter.CollectionEntryDTOConverter)"
	)
	private DTOConverter<AssetListEntry, CollectionEntry>
		_collectionEntryDTOConverter;

}