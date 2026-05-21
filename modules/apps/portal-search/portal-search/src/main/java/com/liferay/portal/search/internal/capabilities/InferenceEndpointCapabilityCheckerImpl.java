/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.capabilities;

import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.license.util.LicenseManager;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.search.capabilities.ElasticsearchLicenseInformation;
import com.liferay.portal.search.capabilities.InferenceEndpointCapabilityChecker;
import com.liferay.portal.search.capabilities.InferenceEndpointCapabilityStatus;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.rest.semantic.constants.SemanticSearchConstants;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Guedes de Souza
 */
@Component(service = InferenceEndpointCapabilityChecker.class)
public class InferenceEndpointCapabilityCheckerImpl
	implements InferenceEndpointCapabilityChecker {

	@Override
	public InferenceEndpointCapabilityStatus check() {
		if (!FeatureFlagManagerUtil.isEnabled(_FEATURE_FLAG_KEY)) {
			return InferenceEndpointCapabilityStatus.unavailable(
				_REASON_FEATURE_FLAG_DISABLED);
		}

		if (_licenseManager.isFreeTier()) {
			return InferenceEndpointCapabilityStatus.unavailable(
				_REASON_MISSING_DXP_ENTERPRISE);
		}

		if (!_isElasticsearchVersionSufficient(
				_searchEngineInformation.getClientVersionString())) {

			return InferenceEndpointCapabilityStatus.unavailable(
				_REASON_ELASTICSEARCH_VERSION_BELOW_MINIMUM);
		}

		ElasticsearchLicenseInformation elasticsearchLicenseInformation =
			_elasticsearchLicenseInformationSnapshot.get();

		if ((elasticsearchLicenseInformation == null) ||
			!elasticsearchLicenseInformation.isInferenceLicenseAvailable()) {

			return InferenceEndpointCapabilityStatus.unavailable(
				_REASON_MISSING_ELASTICSEARCH_LICENSE);
		}

		return InferenceEndpointCapabilityStatus.available();
	}

	private boolean _isElasticsearchVersionSufficient(String versionString) {
		if (Validator.isNull(versionString)) {
			return false;
		}

		Version version = Version.parseVersion(versionString);

		if (version.compareTo(_MINIMUM_VERSION) >= 0) {
			return true;
		}

		return false;
	}

	private static final String _FEATURE_FLAG_KEY = "LPD-11319";

	private static final Version _MINIMUM_VERSION = Version.parseVersion(
		SemanticSearchConstants.MINIMUM_ELASTICSEARCH_VERSION);

	private static final String _REASON_ELASTICSEARCH_VERSION_BELOW_MINIMUM =
		"semantic-search.capability.elasticsearch-version-below-minimum";

	private static final String _REASON_FEATURE_FLAG_DISABLED =
		"semantic-search.capability.feature-flag-disabled";

	private static final String _REASON_MISSING_DXP_ENTERPRISE =
		"semantic-search.capability.missing-dxp-enterprise";

	private static final String _REASON_MISSING_ELASTICSEARCH_LICENSE =
		"semantic-search.capability.missing-elasticsearch-license";

	private static final Snapshot<ElasticsearchLicenseInformation>
		_elasticsearchLicenseInformationSnapshot = new Snapshot<>(
			InferenceEndpointCapabilityCheckerImpl.class,
			ElasticsearchLicenseInformation.class);

	@Reference
	private LicenseManager _licenseManager;

	@Reference
	private SearchEngineInformation _searchEngineInformation;

}