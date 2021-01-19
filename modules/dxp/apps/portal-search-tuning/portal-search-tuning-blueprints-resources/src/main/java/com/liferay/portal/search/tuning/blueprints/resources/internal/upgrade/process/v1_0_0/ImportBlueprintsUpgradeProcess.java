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

package com.liferay.portal.search.tuning.blueprints.resources.internal.upgrade.process.v1_0_0;

import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.search.tuning.blueprints.resources.internal.upgrade.util.ImportHelper;

/**
 * @author Petteri Karttunen
 */
public class ImportBlueprintsUpgradeProcess extends UpgradeProcess {

	public ImportBlueprintsUpgradeProcess(ImportHelper importHelper) {
		_importHelper = importHelper;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_importHelper.importDefaultBlueprints();
	}

	private final ImportHelper _importHelper;

}