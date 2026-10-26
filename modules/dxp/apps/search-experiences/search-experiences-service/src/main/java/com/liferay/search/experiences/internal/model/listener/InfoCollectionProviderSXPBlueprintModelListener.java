/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.model.listener;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.list.model.AssetListEntryUsageTable;
import com.liferay.asset.util.AssetHelper;
import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalService;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.service.CalendarBookingLocalService;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureService;
import com.liferay.fragment.model.FragmentCompositionTable;
import com.liferay.fragment.model.FragmentEntryLinkTable;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.knowledge.base.model.KBArticle;
import com.liferay.knowledge.base.service.KBArticleLocalService;
import com.liferay.layout.content.model.LayoutContentVersionTable;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructureRelTable;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.instance.lifecycle.EveryNodeEveryStartup;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.dao.jdbc.CurrentConnection;
import com.liferay.portal.kernel.instance.PortalInstancePool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.PortletPreferenceValueTable;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.InfrastructureUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.asset.AssetSubtypeIdentifier;
import com.liferay.portal.search.asset.AssetSubtypeIdentifierBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.util.PortalInstances;
import com.liferay.search.experiences.internal.info.collection.provider.AssetEntrySXPBlueprintInfoCollectionProvider;
import com.liferay.search.experiences.internal.info.collection.provider.BlogsEntrySXPBlueprintInfoCollectionProvider;
import com.liferay.search.experiences.internal.info.collection.provider.CalendarBookingSXPBlueprintInfoCollectionProvider;
import com.liferay.search.experiences.internal.info.collection.provider.FileEntrySXPBlueprintInfoCollectionProvider;
import com.liferay.search.experiences.internal.info.collection.provider.JournalArticleSXPBlueprintInfoCollectionProvider;
import com.liferay.search.experiences.internal.info.collection.provider.KBArticleSXPBlueprintInfoCollectionProvider;
import com.liferay.search.experiences.internal.info.collection.provider.ObjectEntrySXPBlueprintInfoCollectionProvider;
import com.liferay.search.experiences.internal.info.collection.provider.SXPBlueprintInfoCollectionProvider;
import com.liferay.search.experiences.model.SXPBlueprint;
import com.liferay.search.experiences.rest.dto.v1_0.Configuration;
import com.liferay.search.experiences.rest.dto.v1_0.GeneralConfiguration;
import com.liferay.search.experiences.service.SXPBlueprintLocalService;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Shuyang Zhou
 * @author Joshua Cords
 */
@Component(enabled = false, service = ModelListener.class)
public class InfoCollectionProviderSXPBlueprintModelListener
	extends BaseModelListener<SXPBlueprint> {

	@Override
	public void onAfterCreate(SXPBlueprint sxpBlueprint) {
		if (_deactivated || !_isCollectionProvider(sxpBlueprint)) {
			return;
		}

		Map<Long, ServiceRegistration<?>> serviceRegistrations =
			_serviceRegistrations.computeIfAbsent(
				sxpBlueprint.getCompanyId(),
				companyId -> new ConcurrentHashMap<>());

		String className = _getClassName(sxpBlueprint);

		ServiceRegistration<?> serviceRegistration = serviceRegistrations.put(
			sxpBlueprint.getSXPBlueprintId(),
			_bundleContext.registerService(
				InfoCollectionProvider.class,
				_createInfoCollectionProvider(className, sxpBlueprint),
				HashMapDictionaryBuilder.<String, Object>put(
					"company.id", sxpBlueprint.getCompanyId()
				).put(
					"item.class.name", className
				).build()));

		if (serviceRegistration != null) {
			serviceRegistration.unregister();
		}

		if (_deactivated) {
			_unregisterServiceRegistration(
				serviceRegistrations.remove(sxpBlueprint.getSXPBlueprintId()));
		}
	}

	@Override
	public void onAfterRemove(SXPBlueprint sxpBlueprint) {
		Map<Long, ServiceRegistration<?>> serviceRegistrations =
			_serviceRegistrations.get(sxpBlueprint.getCompanyId());

		if (serviceRegistrations == null) {
			return;
		}

		ServiceRegistration<?> serviceRegistration =
			serviceRegistrations.remove(sxpBlueprint.getSXPBlueprintId());

		if (serviceRegistration != null) {
			serviceRegistration.unregister();
		}
	}

	@Override
	public void onAfterUpdate(
		SXPBlueprint originalSXPBlueprint, SXPBlueprint newSXPBlueprint) {

		onAfterRemove(originalSXPBlueprint);

		onAfterCreate(newSXPBlueprint);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_serviceRegistration = bundleContext.registerService(
			PortalInstanceLifecycleListener.class,
			new InfoCollectionProviderPortalInstanceLifecycleListener(), null);
	}

	@Deactivate
	protected void deactivate() {
		_deactivated = true;

		_serviceRegistration.unregister();

		Set<Long> companyIds = _serviceRegistrations.keySet();

		companyIds.forEach(this::_unregisterServiceRegistrations);
	}

	private InfoCollectionProvider<?> _createInfoCollectionProvider(
		String className, SXPBlueprint sxpBlueprint) {

		if (className.equals(BlogsEntry.class.getName())) {
			return new BlogsEntrySXPBlueprintInfoCollectionProvider(
				_assetHelper, _blogsEntryLocalService, _searcher,
				_searchRequestBuilderFactory, sxpBlueprint);
		}

		if (className.equals(CalendarBooking.class.getName())) {
			return new CalendarBookingSXPBlueprintInfoCollectionProvider(
				_assetHelper, _calendarBookingLocalService, _searcher,
				_searchRequestBuilderFactory, sxpBlueprint);
		}

		if (className.equals(DLFileEntry.class.getName())) {
			return new FileEntrySXPBlueprintInfoCollectionProvider(
				_assetHelper, _assetSubtypeIdentifierBuilder,
				_dlAppLocalService, _dlFileEntryTypeLocalService, _groupService,
				_searcher, _searchRequestBuilderFactory, sxpBlueprint);
		}

		if (className.equals(JournalArticle.class.getName())) {
			return new JournalArticleSXPBlueprintInfoCollectionProvider(
				_assetHelper, _assetSubtypeIdentifierBuilder,
				_classNameLocalService, _ddmStructureService, _groupService,
				_journalArticleService, _searcher, _searchRequestBuilderFactory,
				sxpBlueprint);
		}

		if (className.equals(KBArticle.class.getName())) {
			return new KBArticleSXPBlueprintInfoCollectionProvider(
				_assetHelper, _kbArticleLocalService, _searcher,
				_searchRequestBuilderFactory, sxpBlueprint);
		}

		if (className.contains(ObjectDefinition.class.getName())) {
			return new ObjectEntrySXPBlueprintInfoCollectionProvider(
				_assetHelper, className, _objectDefinitionLocalService,
				_objectEntryLocalService, _searcher,
				_searchRequestBuilderFactory, sxpBlueprint);
		}

		return new AssetEntrySXPBlueprintInfoCollectionProvider(
			_assetHelper, _searcher, _searchRequestBuilderFactory,
			sxpBlueprint);
	}

	private String _getClassName(SXPBlueprint sxpBlueprint) {
		Configuration configuration = Configuration.unsafeToDTO(
			sxpBlueprint.getConfigurationJSON());

		GeneralConfiguration generalConfiguration =
			configuration.getGeneralConfiguration();

		if (generalConfiguration == null) {
			return AssetEntry.class.getName();
		}

		String collectionProviderType =
			generalConfiguration.getCollectionProviderType();

		if (Validator.isNull(collectionProviderType)) {
			return AssetEntry.class.getName();
		}

		AssetSubtypeIdentifier assetSubtypeIdentifier =
			_assetSubtypeIdentifierBuilder.searchableAssetType(
				collectionProviderType
			).build();

		return assetSubtypeIdentifier.getClassName();
	}

	private boolean _isCollectionProvider(SXPBlueprint sxpBlueprint) {
		if (!JSONUtil.isJSONObject(sxpBlueprint.getConfigurationJSON())) {
			return false;
		}

		Configuration configuration = Configuration.unsafeToDTO(
			sxpBlueprint.getConfigurationJSON());

		if (configuration == null) {
			return false;
		}

		GeneralConfiguration generalConfiguration =
			configuration.getGeneralConfiguration();

		if (generalConfiguration == null) {
			return false;
		}

		return GetterUtil.getBoolean(
			generalConfiguration.getCollectionProvider());
	}

	private void _replaceInfoCollectionProviderKeys(
			Connection connection, long fromCompanyId, long toCompanyId)
		throws Exception {

		String assetEntryClassName =
			AssetEntrySXPBlueprintInfoCollectionProvider.class.getName();
		String sxpBlueprintClassName = SXPBlueprint.class.getName();

		for (Map.Entry<String, String> entry : _columnNames.entrySet()) {
			try (PreparedStatement preparedStatement =
					connection.prepareStatement(
						SQLTransformer.transform(
							StringBundler.concat(
								"update ",
								PropsValues.
									DATABASE_PARTITION_SCHEMA_NAME_PREFIX,
								toCompanyId, StringPool.PERIOD, entry.getKey(),
								" set ", entry.getValue(),
								" = replace(replace(", entry.getValue(),
								", ?, ?), ?, ?) where ", entry.getValue(),
								" like ?")))) {

				preparedStatement.setString(
					1,
					SXPBlueprintInfoCollectionProvider.getKey(
						assetEntryClassName, fromCompanyId, StringPool.BLANK));
				preparedStatement.setString(
					2,
					SXPBlueprintInfoCollectionProvider.getKey(
						assetEntryClassName, toCompanyId, StringPool.BLANK));
				preparedStatement.setString(
					3,
					SXPBlueprintInfoCollectionProvider.getKey(
						sxpBlueprintClassName, fromCompanyId,
						StringPool.BLANK));
				preparedStatement.setString(
					4,
					SXPBlueprintInfoCollectionProvider.getKey(
						sxpBlueprintClassName, toCompanyId, StringPool.BLANK));
				preparedStatement.setString(
					5, StringBundler.concat("%_", fromCompanyId, "_%"));

				preparedStatement.executeUpdate();
			}
		}
	}

	private void _unregisterServiceRegistration(
		ServiceRegistration<?> serviceRegistration) {

		if (serviceRegistration != null) {
			serviceRegistration.unregister();
		}
	}

	private void _unregisterServiceRegistrations(long companyId) {
		Map<Long, ServiceRegistration<?>> serviceRegistrations =
			_serviceRegistrations.remove(companyId);

		if (serviceRegistrations == null) {
			return;
		}

		for (Long sxpBlueprintId : serviceRegistrations.keySet()) {
			_unregisterServiceRegistration(
				serviceRegistrations.remove(sxpBlueprintId));
		}
	}

	@Reference
	private AssetHelper _assetHelper;

	@Reference
	private AssetSubtypeIdentifierBuilder _assetSubtypeIdentifierBuilder;

	@Reference
	private BlogsEntryLocalService _blogsEntryLocalService;

	private BundleContext _bundleContext;

	@Reference
	private CalendarBookingLocalService _calendarBookingLocalService;

	@Reference
	private ClassNameLocalService _classNameLocalService;

	private final Map<String, String> _columnNames = HashMapBuilder.put(
		AssetListEntryUsageTable.INSTANCE.getTableName(),
		AssetListEntryUsageTable.INSTANCE.key.getName()
	).put(
		FragmentCompositionTable.INSTANCE.getTableName(),
		FragmentCompositionTable.INSTANCE.data.getName()
	).put(
		FragmentEntryLinkTable.INSTANCE.getTableName(),
		FragmentEntryLinkTable.INSTANCE.editableValues.getName()
	).put(
		LayoutContentVersionTable.INSTANCE.getTableName(),
		LayoutContentVersionTable.INSTANCE.data.getName()
	).put(
		LayoutPageTemplateStructureRelTable.INSTANCE.getTableName(),
		LayoutPageTemplateStructureRelTable.INSTANCE.data.getName()
	).put(
		PortletPreferenceValueTable.INSTANCE.getTableName(),
		PortletPreferenceValueTable.INSTANCE.smallValue.getName()
	).build();

	@Reference
	private CurrentConnection _currentConnection;

	@Reference
	private DDMStructureService _ddmStructureService;

	private volatile boolean _deactivated;

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private DLFileEntryTypeLocalService _dlFileEntryTypeLocalService;

	@Reference
	private GroupService _groupService;

	@Reference
	private JournalArticleService _journalArticleService;

	@Reference
	private KBArticleLocalService _kbArticleLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private Searcher _searcher;

	private ServiceRegistration<PortalInstanceLifecycleListener>
		_serviceRegistration;
	private final Map<Long, Map<Long, ServiceRegistration<?>>>
		_serviceRegistrations = new ConcurrentHashMap<>();

	@Reference
	private SXPBlueprintLocalService _sxpBlueprintLocalService;

	private class InfoCollectionProviderPortalInstanceLifecycleListener
		implements EveryNodeEveryStartup, PortalInstanceLifecycleListener {

		@Override
		public void portalInstancePreregistered(Company company)
			throws Exception {

			if (!PropsValues.DATABASE_PARTITION_ENABLED ||
				!PortalInstances.isCompanyInCopyProcess() ||
				(company.getCompanyId() ==
					PortalInstancePool.getDefaultCompanyId())) {

				return;
			}

			_replaceInfoCollectionProviderKeys(
				_currentConnection.getConnection(
					InfrastructureUtil.getDataSource()),
				PortalInstances.getCopyInProcessCompanyId(),
				company.getCompanyId());
		}

		@Override
		public void portalInstanceRegistered(Company company) {
			for (SXPBlueprint sxpBlueprint :
					_sxpBlueprintLocalService.getSXPBlueprints(
						company.getCompanyId())) {

				onAfterCreate(sxpBlueprint);
			}
		}

		@Override
		public void portalInstanceUnregistered(Company company) {
			_unregisterServiceRegistrations(company.getCompanyId());
		}

	}

}