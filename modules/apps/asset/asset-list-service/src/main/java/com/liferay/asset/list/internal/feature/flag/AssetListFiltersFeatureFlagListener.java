/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.internal.feature.flag;

import com.liferay.asset.list.internal.util.AssetListFiltersMigrationUtil;
import com.liferay.asset.list.model.AssetListEntrySegmentsEntryRel;
import com.liferay.asset.list.service.AssetListEntrySegmentsEntryRelLocalService;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagListener;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Felipe Lorenz
 */
@Component(
	property = "feature.flag.key=LPD-74731", service = FeatureFlagListener.class
)
public class AssetListFiltersFeatureFlagListener
	implements FeatureFlagListener {

	@Override
	public void onValue(
		long companyId, String featureFlagKey, boolean enabled) {

		if (!enabled || !Objects.equals(featureFlagKey, "LPD-74731")) {
			return;
		}

		ActionableDynamicQuery actionableDynamicQuery =
			_assetListEntrySegmentsEntryRelLocalService.
				getActionableDynamicQuery();

		actionableDynamicQuery.setCompanyId(companyId);
		actionableDynamicQuery.setPerformActionMethod(
			(AssetListEntrySegmentsEntryRel assetListEntrySegmentsEntryRel) ->
				_migrate(assetListEntrySegmentsEntryRel));

		try {
			actionableDynamicQuery.performActions();
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}
	}

	private void _migrate(
		AssetListEntrySegmentsEntryRel assetListEntrySegmentsEntryRel) {

		String typeSettings = assetListEntrySegmentsEntryRel.getTypeSettings();

		if (!AssetListFiltersMigrationUtil.rendersInNewFilterUI(
				assetListEntrySegmentsEntryRel.getCompanyId(),
				UnicodePropertiesBuilder.create(
					true
				).fastLoad(
					typeSettings
				).build())) {

			return;
		}

		String migratedTypeSettings =
			AssetListFiltersMigrationUtil.toFiltersTypeSettings(typeSettings);

		if (Objects.equals(typeSettings, migratedTypeSettings)) {
			return;
		}

		assetListEntrySegmentsEntryRel.setTypeSettings(migratedTypeSettings);

		_assetListEntrySegmentsEntryRelLocalService.
			updateAssetListEntrySegmentsEntryRel(
				assetListEntrySegmentsEntryRel);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AssetListFiltersFeatureFlagListener.class);

	@Reference
	private AssetListEntrySegmentsEntryRelLocalService
		_assetListEntrySegmentsEntryRelLocalService;

}