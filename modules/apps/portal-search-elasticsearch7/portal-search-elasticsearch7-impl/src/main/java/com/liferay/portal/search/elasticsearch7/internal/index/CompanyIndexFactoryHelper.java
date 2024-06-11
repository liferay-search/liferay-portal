/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.index;

import com.liferay.osgi.service.tracker.collections.EagerServiceTrackerCustomizer;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.PortalRunMode;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationWrapper;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnectionNotInitializedException;
import com.liferay.portal.search.elasticsearch7.internal.helper.SearchLogHelperUtil;
import com.liferay.portal.search.elasticsearch7.internal.index.constants.IndexSettingsConstants;
import com.liferay.portal.search.elasticsearch7.internal.index.util.IndexFactoryCompanyIdRegistryUtil;
import com.liferay.portal.search.elasticsearch7.internal.settings.SettingsHelperImpl;
import com.liferay.portal.search.elasticsearch7.internal.util.ResourceUtil;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.spi.index.configuration.contributor.CompanyIndexConfigurationContributor;
import com.liferay.portal.search.spi.index.listener.CompanyIndexListener;

import java.io.IOException;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CloseIndexRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Joao Victor Alves
 */
@Component(service = CompanyIndexFactoryHelper.class)
public class CompanyIndexFactoryHelper {

	public void createIndex(String indexName, IndicesClient indicesClient) {
		CreateIndexRequest createIndexRequest = new CreateIndexRequest(
			indexName);

		MappingsHelperImpl mappingsHelperImpl = new MappingsHelperImpl(
			indexName, indicesClient, _jsonFactory);

		_setSettings(createIndexRequest);

		_setMappings(createIndexRequest, mappingsHelperImpl);

		try {
			ActionResponse actionResponse = indicesClient.create(
				createIndexRequest, RequestOptions.DEFAULT);

			SearchLogHelperUtil.logActionResponse(_log, actionResponse);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}

		_updateMappings(mappingsHelperImpl);

		_executeCompanyIndexListenersAfterCreate(indexName);
	}

	public void deleteIndex(
		String indexName, IndicesClient indicesClient, long companyId,
		boolean resetBothIndexNames) {

		_executeCompanyIndexListenersBeforeDelete(indexName);

		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(
			indexName);

		try {
			ActionResponse actionResponse = indicesClient.delete(
				deleteIndexRequest, RequestOptions.DEFAULT);

			SearchLogHelperUtil.logActionResponse(_log, actionResponse);

			if (companyId != CompanyConstants.SYSTEM) {
				if (resetBothIndexNames) {
					_companyLocalService.updateIndexNames(
						companyId, null, null);
				}
				else {
					_companyLocalService.updateIndexNameNext(companyId, null);
				}
			}
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	public String getIndexName(long companyId) {
		return _indexNameBuilder.getIndexName(companyId);
	}

	public boolean hasIndex(IndicesClient indicesClient, String indexName) {
		GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);

		try {
			return indicesClient.exists(
				getIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	public void updateIndex(String indexName, IndicesClient indicesClient) {
		MappingsHelperImpl mappingsHelperImpl = new MappingsHelperImpl(
			indexName, indicesClient, _jsonFactory);

		_updateSettings(indexName, indicesClient);

		_updateMappings(mappingsHelperImpl);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_companyIndexListenerServiceTrackerList =
			ServiceTrackerListFactory.open(
				bundleContext, CompanyIndexListener.class);

		_companyIndexConfigurationContributorServiceTrackerList =
			ServiceTrackerListFactory.open(
				bundleContext, CompanyIndexConfigurationContributor.class, null,
				new EagerServiceTrackerCustomizer
					<CompanyIndexConfigurationContributor,
					 CompanyIndexConfigurationContributor>() {

					@Override
					public CompanyIndexConfigurationContributor addingService(
						ServiceReference<CompanyIndexConfigurationContributor>
							serviceReference) {

						CompanyIndexConfigurationContributor
							companyIndexConfigurationContributor =
								bundleContext.getService(serviceReference);

						_processCompanyIndexConfigurationContributor(
							companyIndexConfigurationContributor);

						return companyIndexConfigurationContributor;
					}

					@Override
					public void modifiedService(
						ServiceReference<CompanyIndexConfigurationContributor>
							serviceReference,
						CompanyIndexConfigurationContributor
							companyIndexConfigurationContributor) {
					}

					@Override
					public void removedService(
						ServiceReference<CompanyIndexConfigurationContributor>
							serviceReference,
						CompanyIndexConfigurationContributor
							companyIndexConfigurationContributor) {

						bundleContext.ungetService(serviceReference);
					}

				});
	}

	@Deactivate
	protected void deactivate() {
		if (_companyIndexListenerServiceTrackerList != null) {
			_companyIndexListenerServiceTrackerList.close();
		}

		if (_companyIndexConfigurationContributorServiceTrackerList != null) {
			_companyIndexConfigurationContributorServiceTrackerList.close();
		}
	}

	private void _executeCompanyIndexListenerAfterCreate(
		CompanyIndexListener companyIndexListener, String indexName) {

		try {
			companyIndexListener.onAfterCreate(indexName);
		}
		catch (Throwable throwable) {
			_log.error(
				StringBundler.concat(
					"Unable to apply listener ", companyIndexListener,
					" after creating index ", indexName),
				throwable);
		}
	}

	private void _executeCompanyIndexListenerBeforeDelete(
		CompanyIndexListener companyIndexListener, String indexName) {

		try {
			companyIndexListener.onBeforeDelete(indexName);
		}
		catch (Throwable throwable) {
			_log.error(
				StringBundler.concat(
					"Unable to apply listener ", companyIndexListener,
					" before deleting index ", indexName),
				throwable);
		}
	}

	private void _executeCompanyIndexListenersAfterCreate(String indexName) {
		for (CompanyIndexListener companyIndexListener :
				_companyIndexListenerServiceTrackerList) {

			_executeCompanyIndexListenerAfterCreate(
				companyIndexListener, indexName);
		}
	}

	private void _executeCompanyIndexListenersBeforeDelete(String indexName) {
		for (CompanyIndexListener companyIndexListener :
				_companyIndexListenerServiceTrackerList) {

			_executeCompanyIndexListenerBeforeDelete(
				companyIndexListener, indexName);
		}
	}

	private void _loadAdditionalSettings(
		SettingsHelperImpl settingsHelperImpl,
		boolean includeStaticProperties) {

		_loadContributedSettings(settingsHelperImpl);

		_loadConfigurationSettings(settingsHelperImpl, includeStaticProperties);

		if (Validator.isNotNull(
				settingsHelperImpl.get("index.number_of_replicas"))) {

			settingsHelperImpl.put("index.auto_expand_replicas", false);
		}
	}

	private void _loadConfigurationSettings(
		SettingsHelperImpl settingsHelperImpl,
		boolean includeStaticProperties) {

		settingsHelperImpl.put(
			"index.max_result_window",
			String.valueOf(
				_elasticsearchConfigurationWrapper.indexMaxResultWindow()));
		settingsHelperImpl.put(
			"index.number_of_replicas",
			_elasticsearchConfigurationWrapper.indexNumberOfReplicas());

		if (includeStaticProperties) {
			settingsHelperImpl.put(
				"index.number_of_shards",
				_elasticsearchConfigurationWrapper.indexNumberOfShards());
		}

		settingsHelperImpl.loadFromSource(
			_elasticsearchConfigurationWrapper.additionalIndexConfigurations());
	}

	private void _loadContributedSettings(
		SettingsHelperImpl settingsHelperImpl) {

		for (CompanyIndexConfigurationContributor
				companyIndexConfigurationContributor :
					_companyIndexConfigurationContributorServiceTrackerList) {

			companyIndexConfigurationContributor.contributeSettings(
				settingsHelperImpl);
		}
	}

	private void _loadDefaultSettings(SettingsHelperImpl settingsHelperImpl) {
		settingsHelperImpl.loadFromSource(
			ResourceUtil.getResourceAsString(
				getClass(),
				IndexSettingsConstants.INDEX_SETTINGS_ANALYSIS_FILE_NAME));

		settingsHelperImpl.put("index.auto_expand_replicas", "0-all");
		settingsHelperImpl.put("index.default_pipeline", "timestamp");
		settingsHelperImpl.put("index.mapping.total_fields.limit", "7500");
	}

	private void _loadTestModeSettings(SettingsHelperImpl settingsHelperImpl) {
		if (!PortalRunMode.isTestMode()) {
			return;
		}

		settingsHelperImpl.put("index.refresh_interval", "1ms");
		settingsHelperImpl.put(
			"index.search.slowlog.threshold.fetch.warn", "-1");
		settingsHelperImpl.put(
			"index.search.slowlog.threshold.query.warn", "-1");
		settingsHelperImpl.put("index.translog.sync_interval", "100ms");
	}

	private void _processCompanyIndexConfigurationContributor(
		CompanyIndexConfigurationContributor
			companyIndexConfigurationContributor) {

		boolean contributeMappings = Validator.isNull(
			_elasticsearchConfigurationWrapper.overrideTypeMappings());

		SettingsHelperImpl settingsHelperImpl = new SettingsHelperImpl(
			Settings.builder());

		companyIndexConfigurationContributor.contributeSettings(
			settingsHelperImpl);

		Settings settings = settingsHelperImpl.build();

		if (!contributeMappings && settings.isEmpty()) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"No mappings or settings to contribute from " +
						companyIndexConfigurationContributor);
			}

			return;
		}

		RestHighLevelClient restHighLevelClient = null;

		try {
			restHighLevelClient =
				_elasticsearchConnectionManager.getRestHighLevelClient();
		}
		catch (ElasticsearchConnectionNotInitializedException
					elasticsearchConnectionNotInitializedException) {

			_log.error(elasticsearchConnectionNotInitializedException);

			return;
		}

		IndicesClient indicesClient = restHighLevelClient.indices();

		_companyLocalService.forEachCompanyId(
			companyId -> {
				String indexName = getIndexName(companyId);

				if (!settings.isEmpty()) {
					UpdateSettingsRequest updateSettingsRequest =
						new UpdateSettingsRequest(indexName);

					updateSettingsRequest.settings(settings);

					try {
						indicesClient.putSettings(
							updateSettingsRequest, RequestOptions.DEFAULT);
					}
					catch (Exception exception) {
						_log.error(
							StringBundler.concat(
								"Unable to put settings for index ", indexName,
								" with contributor ",
								companyIndexConfigurationContributor),
							exception);
					}
				}

				if (contributeMappings) {
					companyIndexConfigurationContributor.contributeMappings(
						new MappingsHelperImpl(
							indexName, indicesClient, _jsonFactory));
				}
			},
			IndexFactoryCompanyIdRegistryUtil.getCompanyIds());
	}

	private void _putAdditionalMappings(MappingsHelperImpl mappingsHelperImpl) {
		if (Validator.isNull(
				_elasticsearchConfigurationWrapper.additionalTypeMappings())) {

			return;
		}

		mappingsHelperImpl.putMappings(
			_elasticsearchConfigurationWrapper.additionalTypeMappings());
	}

	private void _putContributedMappings(
		MappingsHelperImpl mappingsHelperImpl) {

		for (CompanyIndexConfigurationContributor
				companyIndexConfigurationContributor :
					_companyIndexConfigurationContributorServiceTrackerList) {

			companyIndexConfigurationContributor.contributeMappings(
				mappingsHelperImpl);
		}
	}

	private void _setMappings(
		CreateIndexRequest createIndexRequest,
		MappingsHelperImpl mappingsHelperImpl) {

		mappingsHelperImpl.setMappings(
			createIndexRequest,
			_elasticsearchConfigurationWrapper.overrideTypeMappings());
	}

	private void _setSettings(CreateIndexRequest createIndexRequest) {
		SettingsHelperImpl settingsHelperImpl = new SettingsHelperImpl(
			Settings.builder());

		_loadDefaultSettings(settingsHelperImpl);

		_loadTestModeSettings(settingsHelperImpl);

		_loadAdditionalSettings(settingsHelperImpl, true);

		createIndexRequest.settings(settingsHelperImpl.getBuilder());
	}

	private void _updateMappings(MappingsHelperImpl mappingsHelperImpl) {
		mappingsHelperImpl.putMappings(
			mappingsHelperImpl.getMappings(
				_elasticsearchConfigurationWrapper.overrideTypeMappings()));

		if (Validator.isNotNull(
				_elasticsearchConfigurationWrapper.overrideTypeMappings())) {

			return;
		}

		_putContributedMappings(mappingsHelperImpl);

		_putAdditionalMappings(mappingsHelperImpl);
	}

	private void _updateSettings(
		String indexName, IndicesClient indicesClient) {

		SettingsHelperImpl settingsHelperImpl = new SettingsHelperImpl(
			Settings.builder());

		_loadDefaultSettings(settingsHelperImpl);

		_loadAdditionalSettings(settingsHelperImpl, false);

		UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(
			indexName);

		updateSettingsRequest.settings(settingsHelperImpl.getBuilder());

		try {
			indicesClient.close(
				new CloseIndexRequest(indexName), RequestOptions.DEFAULT);

			indicesClient.putSettings(
				updateSettingsRequest, RequestOptions.DEFAULT);

			indicesClient.open(
				new OpenIndexRequest(indexName), RequestOptions.DEFAULT);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to put settings for index " + indexName, exception);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CompanyIndexFactoryHelper.class);

	private ServiceTrackerList<CompanyIndexConfigurationContributor>
		_companyIndexConfigurationContributorServiceTrackerList;
	private ServiceTrackerList<CompanyIndexListener>
		_companyIndexListenerServiceTrackerList;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ElasticsearchConfigurationWrapper
		_elasticsearchConfigurationWrapper;

	@Reference
	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private JSONFactory _jsonFactory;

}