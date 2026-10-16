/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.upgrade.v2_1_2.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Selena Aungst
 */
@RunWith(Arquillian.class)
public class DateFacetTemplateUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_classNameId = _classNameLocalService.getClassNameId(
			"com.liferay.portal.search.web.internal.date.facet.portlet." +
				"DateFacetPortlet");
		_db = DBManagerUtil.getDB();

		_upgradeStepRegistrator.register(
			new UpgradeStepRegistrator.Registry() {

				@Override
				public void register(
					String fromSchemaVersionString,
					String toSchemaVersionString, UpgradeStep... upgradeSteps) {

					for (UpgradeStep upgradeStep : upgradeSteps) {
						Class<?> clazz = upgradeStep.getClass();

						if (Objects.equals(
								clazz.getName(),
								"com.liferay.portal.search.web.internal." +
									"upgrade.v2_1_2." +
										"DateFacetTemplateUpgradeProcess")) {

							_upgradeProcess = (UpgradeProcess)upgradeStep;
						}
					}
				}

			});
	}

	@After
	public void tearDown() throws Exception {
		String templateIdsString = StringUtil.merge(_templateIds);

		_db.runSQL(
			StringBundler.concat(
				"delete from ResourcePermission where name = '",
				_CLASS_NAME_DDM_TEMPLATE, "' and primKeyId in (",
				templateIdsString, ")"));
		_db.runSQL(
			"delete from DDMTemplateVersion where templateId in (" +
				templateIdsString + ")");
		_db.runSQL(
			"delete from DDMTemplate where templateId in (" +
				templateIdsString + ")");
	}

	@Test
	public void testUpgrade() throws Exception {
		_addTemplate("DATE-FACET-CHECKBOX-FTL");
		_addTemplate("DATE-FACET-LINK-FTL");
		_addTemplate("DATE-FACET-RADIO-FTL");

		long customTemplateId = _addTemplate(
			StringUtil.toUpperCase(RandomTestUtil.randomString()));

		_assertCount(4);

		_upgradeProcess.upgrade();

		_assertCount(1);

		Assert.assertEquals(
			1,
			_count(
				"select count(*) from DDMTemplate where templateId = " +
					customTemplateId));
	}

	private long _addTemplate(String templateKey) throws Exception {
		long templateId = RandomTestUtil.nextLong();

		_db.runSQL(
			StringBundler.concat(
				"insert into DDMTemplate (ctCollectionId, templateId, ",
				"classNameId, templateKey) values (",
				RandomTestUtil.randomInt(), ", ", templateId, ", ",
				_classNameId, ", '", templateKey, "')"));
		_db.runSQL(
			StringBundler.concat(
				"insert into DDMTemplateVersion (ctCollectionId, ",
				"templateVersionId, templateId) values (",
				RandomTestUtil.randomInt(), ", ", RandomTestUtil.nextLong(),
				", ", templateId, ")"));
		_db.runSQL(
			StringBundler.concat(
				"insert into ResourcePermission (ctCollectionId, ",
				"resourcePermissionId, name, primKeyId) values (",
				RandomTestUtil.randomInt(), ", ", RandomTestUtil.nextLong(),
				", '", _CLASS_NAME_DDM_TEMPLATE, "', ", templateId, ")"));

		_templateIds.add(templateId);

		return templateId;
	}

	private void _assertCount(int expected) throws Exception {
		String templateIdsString = StringUtil.merge(_templateIds);

		Assert.assertEquals(
			expected,
			_count(
				"select count(*) from DDMTemplate where templateId in (" +
					templateIdsString + ")"));
		Assert.assertEquals(
			expected,
			_count(
				StringBundler.concat(
					"select count(*) from DDMTemplateVersion where templateId ",
					"in (", templateIdsString, ")")));
		Assert.assertEquals(
			expected,
			_count(
				StringBundler.concat(
					"select count(*) from ResourcePermission where name = '",
					_CLASS_NAME_DDM_TEMPLATE, "' and primKeyId in (",
					templateIdsString, ")")));
	}

	private int _count(String sql) throws Exception {
		try (Connection connection = DataAccess.getConnection();

			PreparedStatement preparedStatement = connection.prepareStatement(
				sql);

			ResultSet resultSet = preparedStatement.executeQuery()) {

			resultSet.next();

			return resultSet.getInt(1);
		}
	}

	private static final String _CLASS_NAME_DDM_TEMPLATE =
		"com.liferay.dynamic.data.mapping.model.DDMTemplate";

	private long _classNameId;

	@Inject
	private ClassNameLocalService _classNameLocalService;

	private DB _db;
	private final List<Long> _templateIds = new ArrayList<>();
	private UpgradeProcess _upgradeProcess;

	@Inject(
		filter = "component.name=com.liferay.portal.search.web.internal.upgrade.registry.SearchWebUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}