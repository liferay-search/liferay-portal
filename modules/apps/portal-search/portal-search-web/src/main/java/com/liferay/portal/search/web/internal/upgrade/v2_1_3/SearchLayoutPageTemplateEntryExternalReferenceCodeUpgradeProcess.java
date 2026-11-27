/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.upgrade.v2_1_3;

import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Javier Moral
 */
public class SearchLayoutPageTemplateEntryExternalReferenceCodeUpgradeProcess
	extends UpgradeProcess {

	public SearchLayoutPageTemplateEntryExternalReferenceCodeUpgradeProcess(
		CompanyLocalService companyLocalService, Language language) {

		_companyLocalService = companyLocalService;
		_language = language;
	}

	@Override
	protected void doUpgrade() throws Exception {
		if (!hasTable("LayoutPageTemplateEntry")) {
			return;
		}

		Set<String> names = _getNames();

		_companyLocalService.forEachCompanyId(
			companyId -> _upgradeLayoutPageTemplateEntry(companyId, names));
	}

	private Set<String> _getNames() {
		Set<String> names = new HashSet<>();

		for (Locale locale : _language.getAvailableLocales()) {
			names.add(_language.get(locale, "search"));
		}

		return names;
	}

	private boolean _hasExternalReferenceCode(
			String externalReferenceCode, long groupId)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select layoutPageTemplateEntryId from " +
					"LayoutPageTemplateEntry where groupId = ? and " +
						"externalReferenceCode = ?")) {

			preparedStatement.setLong(1, groupId);
			preparedStatement.setString(2, externalReferenceCode);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	private void _upgradeLayoutPageTemplateEntry(
			long companyId, Set<String> names)
		throws Exception {

		Company company = _companyLocalService.getCompany(companyId);

		if (_hasExternalReferenceCode(
				_EXTERNAL_REFERENCE_CODE, company.getGroupId())) {

			return;
		}

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select ctCollectionId, externalReferenceCode, ",
					"layoutPageTemplateEntryId, name from ",
					"LayoutPageTemplateEntry where companyId = ? and groupId ",
					"= ? and type_ = ? and uuid_ = externalReferenceCode ",
					"order by createDate, layoutPageTemplateEntryId"));
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					StringBundler.concat(
						"update LayoutPageTemplateEntry set ",
						"externalReferenceCode = ? where ctCollectionId = ? ",
						"and layoutPageTemplateEntryId = ?"))) {

			preparedStatement1.setLong(1, companyId);
			preparedStatement1.setLong(2, company.getGroupId());
			preparedStatement1.setInt(
				3, LayoutPageTemplateEntryTypeConstants.WIDGET_PAGE);

			String externalReferenceCode = null;
			long layoutPageTemplateEntryId = 0;

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					long curLayoutPageTemplateEntryId = resultSet.getLong(
						"layoutPageTemplateEntryId");

					if (layoutPageTemplateEntryId == 0) {
						if (!names.contains(resultSet.getString("name"))) {
							continue;
						}

						externalReferenceCode = resultSet.getString(
							"externalReferenceCode");
						layoutPageTemplateEntryId =
							curLayoutPageTemplateEntryId;
					}
					else if (layoutPageTemplateEntryId !=
								curLayoutPageTemplateEntryId) {

						continue;
					}

					preparedStatement2.setString(1, _EXTERNAL_REFERENCE_CODE);
					preparedStatement2.setLong(
						2, resultSet.getLong("ctCollectionId"));
					preparedStatement2.setLong(3, layoutPageTemplateEntryId);

					preparedStatement2.addBatch();
				}
			}

			preparedStatement2.executeBatch();

			if (layoutPageTemplateEntryId != 0) {
				_upgradeLayouts(company, externalReferenceCode);
			}
		}
	}

	private void _upgradeLayouts(Company company, String externalReferenceCode)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"update Layout set portletLPTEERC = ? where companyId = ? " +
					"and portletLPTEERC = ? and portletLPTESERC = ?")) {

			preparedStatement.setString(1, _EXTERNAL_REFERENCE_CODE);
			preparedStatement.setLong(2, company.getCompanyId());
			preparedStatement.setString(3, externalReferenceCode);

			Group group = company.getGroup();

			preparedStatement.setString(4, group.getExternalReferenceCode());

			preparedStatement.executeUpdate();
		}
	}

	private static final String _EXTERNAL_REFERENCE_CODE = "L_SEARCH";

	private final CompanyLocalService _companyLocalService;
	private final Language _language;

}