/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.upgrade.v3_2_0;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeProcessFactory;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.kernel.util.Validator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Gustavo Lima
 */
public class SXPBlueprintUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		_upgradeSXPBlueprint();
	}

	@Override
	protected UpgradeStep[] getPreUpgradeSteps() {
		return new UpgradeStep[] {
			UpgradeProcessFactory.addColumns(
				"SXPBlueprint", "fallbackDescription STRING null",
				"fallbackTitle VARCHAR(500) null")
		};
	}

	private String _getDefaultValue(String fieldName, String xml) {
		if (!Validator.isBlank(xml)) {
			int start = xml.indexOf(">", xml.indexOf("<" + fieldName));

			int end = xml.indexOf("<", start);

			if ((end != -1) && (start != -1)) {
				return xml.substring(start + 1, end);
			}
		}

		return StringPool.BLANK;
	}

	private void _upgradeSXPBlueprint() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select description, title, sxpBlueprintId from SXPBlueprint");
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update SXPBlueprint set fallbackDescription = ?, " +
						"fallbackTitle = ? where sxpBlueprintId = ?")) {

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					preparedStatement2.setString(
						1,
						_getDefaultValue(
							"Description", resultSet.getString("description")));
					preparedStatement2.setString(
						2,
						_getDefaultValue(
							"Title", resultSet.getString("title")));
					preparedStatement2.setLong(
						3, resultSet.getLong("sxpBlueprintId"));

					preparedStatement2.addBatch();
				}

				preparedStatement2.executeBatch();
			}
		}
	}

}