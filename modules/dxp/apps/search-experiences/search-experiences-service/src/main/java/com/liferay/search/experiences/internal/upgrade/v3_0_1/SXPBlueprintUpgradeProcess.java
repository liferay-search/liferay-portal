/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.upgrade.v3_0_1;

import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gustavo Lima
 */
public class SXPBlueprintUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select description, externalReferenceCode, title, readOnly " +
					"from SXPElement");
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update SXPElement set description = ?, title = ? where " +
						"externalReferenceCode = ?")) {

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					if (!resultSet.getBoolean("readOnly")) {
						continue;
					}

					String title = resultSet.getString("title");

					title = _replaceLanguageIdWithDefaultLocale(title);

					String description = resultSet.getString("description");

					description = _replaceLanguageIdWithDefaultLocale(
						description);

					preparedStatement2.setString(1, description);

					preparedStatement2.setString(2, title);

					preparedStatement2.setString(
						3, resultSet.getString("externalReferenceCode"));

					preparedStatement2.addBatch();
				}

				preparedStatement2.executeBatch();
			}
		}
	}

	private String _replaceLanguageIdWithDefaultLocale(String xml) {
		Matcher matcher = _defaultLocalePattern.matcher(xml);

		if (matcher.find()) {
			String defaultLocaleValue = matcher.group(1);

			matcher = _languageIdPattern.matcher(xml);

			if (matcher.find()) {
				String languageIdValue = matcher.group(1);

				return StringUtil.replace(
					xml, languageIdValue, defaultLocaleValue);
			}
		}

		return xml;
	}

	private static final Pattern _defaultLocalePattern = Pattern.compile(
		"default-locale=\"(.*?)\"");
	private static final Pattern _languageIdPattern = Pattern.compile(
		"language-id=\"(.*?)\"");

}