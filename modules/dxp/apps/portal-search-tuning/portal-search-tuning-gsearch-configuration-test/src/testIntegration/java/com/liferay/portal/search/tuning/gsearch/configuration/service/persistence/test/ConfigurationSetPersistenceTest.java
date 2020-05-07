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

package com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchConfigurationSetException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.ConfigurationSet;
import com.liferay.portal.search.tuning.gsearch.configuration.service.ConfigurationSetLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.ConfigurationSetPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.ConfigurationSetUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @generated
 */
@RunWith(Arquillian.class)
public class ConfigurationSetPersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED,
				"com.liferay.portal.search.tuning.gsearch.configuration.service"));

	@Before
	public void setUp() {
		_persistence = ConfigurationSetUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<ConfigurationSet> iterator = _configurationSets.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		ConfigurationSet configurationSet = _persistence.create(pk);

		Assert.assertNotNull(configurationSet);

		Assert.assertEquals(configurationSet.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		ConfigurationSet newConfigurationSet = addConfigurationSet();

		_persistence.remove(newConfigurationSet);

		ConfigurationSet existingConfigurationSet =
			_persistence.fetchByPrimaryKey(newConfigurationSet.getPrimaryKey());

		Assert.assertNull(existingConfigurationSet);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addConfigurationSet();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		ConfigurationSet newConfigurationSet = _persistence.create(pk);

		newConfigurationSet.setGroupId(RandomTestUtil.nextLong());

		newConfigurationSet.setCompanyId(RandomTestUtil.nextLong());

		newConfigurationSet.setUserId(RandomTestUtil.nextLong());

		newConfigurationSet.setUserName(RandomTestUtil.randomString());

		newConfigurationSet.setCreateDate(RandomTestUtil.nextDate());

		newConfigurationSet.setModifiedDate(RandomTestUtil.nextDate());

		newConfigurationSet.setName(RandomTestUtil.randomString());

		newConfigurationSet.setDescription(RandomTestUtil.randomString());

		_configurationSets.add(_persistence.update(newConfigurationSet));

		ConfigurationSet existingConfigurationSet =
			_persistence.findByPrimaryKey(newConfigurationSet.getPrimaryKey());

		Assert.assertEquals(
			existingConfigurationSet.getConfigurationSetId(),
			newConfigurationSet.getConfigurationSetId());
		Assert.assertEquals(
			existingConfigurationSet.getGroupId(),
			newConfigurationSet.getGroupId());
		Assert.assertEquals(
			existingConfigurationSet.getCompanyId(),
			newConfigurationSet.getCompanyId());
		Assert.assertEquals(
			existingConfigurationSet.getUserId(),
			newConfigurationSet.getUserId());
		Assert.assertEquals(
			existingConfigurationSet.getUserName(),
			newConfigurationSet.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingConfigurationSet.getCreateDate()),
			Time.getShortTimestamp(newConfigurationSet.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingConfigurationSet.getModifiedDate()),
			Time.getShortTimestamp(newConfigurationSet.getModifiedDate()));
		Assert.assertEquals(
			existingConfigurationSet.getName(), newConfigurationSet.getName());
		Assert.assertEquals(
			existingConfigurationSet.getDescription(),
			newConfigurationSet.getDescription());
	}

	@Test
	public void testCountByGroupId() throws Exception {
		_persistence.countByGroupId(RandomTestUtil.nextLong());

		_persistence.countByGroupId(0L);
	}

	@Test
	public void testCountByCompanyId() throws Exception {
		_persistence.countByCompanyId(RandomTestUtil.nextLong());

		_persistence.countByCompanyId(0L);
	}

	@Test
	public void testCountByG_U() throws Exception {
		_persistence.countByG_U(
			RandomTestUtil.nextLong(), RandomTestUtil.nextLong());

		_persistence.countByG_U(0L, 0L);
	}

	@Test
	public void testCountByG_N() throws Exception {
		_persistence.countByG_N(RandomTestUtil.nextLong(), "");

		_persistence.countByG_N(0L, "null");

		_persistence.countByG_N(0L, (String)null);
	}

	@Test
	public void testCountByC_N() throws Exception {
		_persistence.countByC_N(RandomTestUtil.nextLong(), "");

		_persistence.countByC_N(0L, "null");

		_persistence.countByC_N(0L, (String)null);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		ConfigurationSet newConfigurationSet = addConfigurationSet();

		ConfigurationSet existingConfigurationSet =
			_persistence.findByPrimaryKey(newConfigurationSet.getPrimaryKey());

		Assert.assertEquals(existingConfigurationSet, newConfigurationSet);
	}

	@Test(expected = NoSuchConfigurationSetException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<ConfigurationSet> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"ConfigurationSet", "configurationSetId", true, "groupId", true,
			"companyId", true, "userId", true, "userName", true, "createDate",
			true, "modifiedDate", true, "name", true, "description", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		ConfigurationSet newConfigurationSet = addConfigurationSet();

		ConfigurationSet existingConfigurationSet =
			_persistence.fetchByPrimaryKey(newConfigurationSet.getPrimaryKey());

		Assert.assertEquals(existingConfigurationSet, newConfigurationSet);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		ConfigurationSet missingConfigurationSet =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingConfigurationSet);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		ConfigurationSet newConfigurationSet1 = addConfigurationSet();
		ConfigurationSet newConfigurationSet2 = addConfigurationSet();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newConfigurationSet1.getPrimaryKey());
		primaryKeys.add(newConfigurationSet2.getPrimaryKey());

		Map<Serializable, ConfigurationSet> configurationSets =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, configurationSets.size());
		Assert.assertEquals(
			newConfigurationSet1,
			configurationSets.get(newConfigurationSet1.getPrimaryKey()));
		Assert.assertEquals(
			newConfigurationSet2,
			configurationSets.get(newConfigurationSet2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, ConfigurationSet> configurationSets =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(configurationSets.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		ConfigurationSet newConfigurationSet = addConfigurationSet();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newConfigurationSet.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, ConfigurationSet> configurationSets =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, configurationSets.size());
		Assert.assertEquals(
			newConfigurationSet,
			configurationSets.get(newConfigurationSet.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, ConfigurationSet> configurationSets =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(configurationSets.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		ConfigurationSet newConfigurationSet = addConfigurationSet();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newConfigurationSet.getPrimaryKey());

		Map<Serializable, ConfigurationSet> configurationSets =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, configurationSets.size());
		Assert.assertEquals(
			newConfigurationSet,
			configurationSets.get(newConfigurationSet.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			ConfigurationSetLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<ConfigurationSet>() {

				@Override
				public void performAction(ConfigurationSet configurationSet) {
					Assert.assertNotNull(configurationSet);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		ConfigurationSet newConfigurationSet = addConfigurationSet();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			ConfigurationSet.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"configurationSetId",
				newConfigurationSet.getConfigurationSetId()));

		List<ConfigurationSet> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		ConfigurationSet existingConfigurationSet = result.get(0);

		Assert.assertEquals(existingConfigurationSet, newConfigurationSet);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			ConfigurationSet.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"configurationSetId", RandomTestUtil.nextLong()));

		List<ConfigurationSet> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		ConfigurationSet newConfigurationSet = addConfigurationSet();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			ConfigurationSet.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("configurationSetId"));

		Object newConfigurationSetId =
			newConfigurationSet.getConfigurationSetId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"configurationSetId", new Object[] {newConfigurationSetId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingConfigurationSetId = result.get(0);

		Assert.assertEquals(existingConfigurationSetId, newConfigurationSetId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			ConfigurationSet.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("configurationSetId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"configurationSetId",
				new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testResetOriginalValues() throws Exception {
		ConfigurationSet newConfigurationSet = addConfigurationSet();

		_persistence.clearCache();

		ConfigurationSet existingConfigurationSet =
			_persistence.findByPrimaryKey(newConfigurationSet.getPrimaryKey());

		Assert.assertEquals(
			Long.valueOf(existingConfigurationSet.getGroupId()),
			ReflectionTestUtil.<Long>invoke(
				existingConfigurationSet, "getOriginalGroupId",
				new Class<?>[0]));
		Assert.assertTrue(
			Objects.equals(
				existingConfigurationSet.getName(),
				ReflectionTestUtil.invoke(
					existingConfigurationSet, "getOriginalName",
					new Class<?>[0])));
	}

	protected ConfigurationSet addConfigurationSet() throws Exception {
		long pk = RandomTestUtil.nextLong();

		ConfigurationSet configurationSet = _persistence.create(pk);

		configurationSet.setGroupId(RandomTestUtil.nextLong());

		configurationSet.setCompanyId(RandomTestUtil.nextLong());

		configurationSet.setUserId(RandomTestUtil.nextLong());

		configurationSet.setUserName(RandomTestUtil.randomString());

		configurationSet.setCreateDate(RandomTestUtil.nextDate());

		configurationSet.setModifiedDate(RandomTestUtil.nextDate());

		configurationSet.setName(RandomTestUtil.randomString());

		configurationSet.setDescription(RandomTestUtil.randomString());

		_configurationSets.add(_persistence.update(configurationSet));

		return configurationSet;
	}

	private List<ConfigurationSet> _configurationSets =
		new ArrayList<ConfigurationSet>();
	private ConfigurationSetPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}