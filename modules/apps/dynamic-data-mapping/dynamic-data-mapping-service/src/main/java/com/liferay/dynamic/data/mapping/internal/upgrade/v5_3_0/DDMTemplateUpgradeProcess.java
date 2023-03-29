/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.internal.upgrade.v5_3_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Tibor Lipusz
 */
public class DDMTemplateUpgradeProcess extends UpgradeProcess {

	public DDMTemplateUpgradeProcess(
		ClassNameLocalService classNameLocalService) {

		_classNameLocalService = classNameLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		long newClassNameId = _classNameLocalService.getClassNameId(
			"com.liferay.portal.search.web.internal.search.bar.portlet." +
				"SearchBarPortlet");

		long resourceClassNameId = _classNameLocalService.getClassNameId(
			"com.liferay.portlet.display.template.PortletDisplayTemplate");

		_deleteOrphanedDefaultSearchBarPortletTemplate(
			resourceClassNameId,
			"com.liferay.portal.search.web.internal.search.bar.portlet." +
				"display.context.SearchBarPortletDisplayContext");
		_deleteOrphanedDefaultSearchBarPortletTemplate(
			resourceClassNameId,
			"com.liferay.portal.search.web.internal.search.bar.portlet." +
				"SearchBarPortletDisplayContext");

		_updateDDMTemplate(
			newClassNameId,
			"com.liferay.portal.search.web.internal.search.bar.portlet." +
				"display.context.SearchBarPortletDisplayContext",
			resourceClassNameId);
		_updateDDMTemplate(
			newClassNameId,
			"com.liferay.portal.search.web.internal.search.bar.portlet." +
				"SearchBarPortletDisplayContext",
			resourceClassNameId);
	}

	private void _deleteOrphanedDefaultSearchBarPortletTemplate(
			long resourceClassNameId, String className)
		throws Exception {

		runSQL(
			StringBundler.concat(
				"delete from DDMTemplate where resourceClassNameId = ",
				resourceClassNameId, " and classNameId = ",
				_classNameLocalService.getClassNameId(className),
				" and templateKey = ", _DEFAULT_SEARCH_BAR_TEMPLATE_KEY));
	}

	private void _updateDDMTemplate(
			long newClassNameId, String oldClassName, long resourceClassNameId)
		throws Exception {

		runSQL(
			StringBundler.concat(
				"update DDMTemplate set classNameId = ", newClassNameId,
				" where classNameId = ",
				_classNameLocalService.getClassNameId(oldClassName),
				" and resourceClassNameId = ", resourceClassNameId));
	}

	private static final String _DEFAULT_SEARCH_BAR_TEMPLATE_KEY =
		"'SEARCH-BAR-LEFT-ALIGNED-ICON-FTL'";

	private final ClassNameLocalService _classNameLocalService;

}