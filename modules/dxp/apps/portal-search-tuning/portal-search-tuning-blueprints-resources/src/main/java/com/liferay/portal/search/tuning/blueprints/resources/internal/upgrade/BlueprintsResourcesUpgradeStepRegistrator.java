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

package com.liferay.portal.search.tuning.blueprints.resources.internal.upgrade;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.tuning.blueprints.resources.internal.upgrade.process.v1_0_0.ImportBlueprintsUpgradeProcess;
import com.liferay.portal.search.tuning.blueprints.resources.internal.upgrade.util.ImportHelper;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	enabled = true, immediate = true, service = UpgradeStepRegistrator.class
)
public class BlueprintsResourcesUpgradeStepRegistrator
	implements UpgradeStepRegistrator {

	@Override
	public void register(Registry registry) {
		if (_log.isInfoEnabled()) {
			_log.info("Blueprints resources upgrade step registrator started");
		}

		registry.register(
			"0.0.0", "1.0.0",
			new ImportBlueprintsUpgradeProcess(_importHelper));

		if (_log.isInfoEnabled()) {
			_log.info("Blueprints Admin Web upgrade step registrator finished");
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintsResourcesUpgradeStepRegistrator.class);

	@Reference
	private ImportHelper _importHelper;

}