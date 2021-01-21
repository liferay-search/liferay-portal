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

package com.liferay.portal.search.tuning.blueprints.resources.internal.model.listener;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.search.tuning.blueprints.resources.internal.upgrade.util.ImportHelper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(enabled = true, immediate = true, service = ModelListener.class)
public class CreateDefaultBlueprintsCompanyModelListener
	extends BaseModelListener<Company> {

	@Override
	public void onAfterCreate(Company company) {
		if (_isCleanDatabase()) {
			return;
		}

		try {
			_importHelper.importDefaultBlueprints(company.getCompanyId());
		}
		catch (PortalException portalException) {
			_log.error(portalException.getMessage(), portalException);
		}
	}

	private boolean _isCleanDatabase() {
		if (_companyLocalService.getCompaniesCount() == 1) {
			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CreateDefaultBlueprintsCompanyModelListener.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ImportHelper _importHelper;

}