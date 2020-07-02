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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchConfigurationException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfiguration;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SearchConfigurationPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SearchConfigurationUtil;
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
public class SearchConfigurationPersistenceTest {

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
		_persistence = SearchConfigurationUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SearchConfiguration> iterator =
			_searchConfigurations.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchConfiguration searchConfiguration = _persistence.create(pk);

		Assert.assertNotNull(searchConfiguration);

		Assert.assertEquals(searchConfiguration.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SearchConfiguration newSearchConfiguration = addSearchConfiguration();

		_persistence.remove(newSearchConfiguration);

		SearchConfiguration existingSearchConfiguration =
			_persistence.fetchByPrimaryKey(
				newSearchConfiguration.getPrimaryKey());

		Assert.assertNull(existingSearchConfiguration);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSearchConfiguration();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchConfiguration newSearchConfiguration = _persistence.create(pk);

		newSearchConfiguration.setMvccVersion(RandomTestUtil.nextLong());

		newSearchConfiguration.setUuid(RandomTestUtil.randomString());

		newSearchConfiguration.setGroupId(RandomTestUtil.nextLong());

		newSearchConfiguration.setCompanyId(RandomTestUtil.nextLong());

		newSearchConfiguration.setUserId(RandomTestUtil.nextLong());

		newSearchConfiguration.setUserName(RandomTestUtil.randomString());

		newSearchConfiguration.setCreateDate(RandomTestUtil.nextDate());

		newSearchConfiguration.setModifiedDate(RandomTestUtil.nextDate());

		newSearchConfiguration.setStatus(RandomTestUtil.nextInt());

		newSearchConfiguration.setStatusByUserId(RandomTestUtil.nextLong());

		newSearchConfiguration.setStatusByUserName(
			RandomTestUtil.randomString());

		newSearchConfiguration.setStatusDate(RandomTestUtil.nextDate());

		newSearchConfiguration.setTitle(RandomTestUtil.randomString());

		newSearchConfiguration.setDescription(RandomTestUtil.randomString());

		newSearchConfiguration.setConfiguration(RandomTestUtil.randomString());

		newSearchConfiguration.setType(RandomTestUtil.nextInt());

		_searchConfigurations.add(_persistence.update(newSearchConfiguration));

		SearchConfiguration existingSearchConfiguration =
			_persistence.findByPrimaryKey(
				newSearchConfiguration.getPrimaryKey());

		Assert.assertEquals(
			existingSearchConfiguration.getMvccVersion(),
			newSearchConfiguration.getMvccVersion());
		Assert.assertEquals(
			existingSearchConfiguration.getUuid(),
			newSearchConfiguration.getUuid());
		Assert.assertEquals(
			existingSearchConfiguration.getSearchConfigurationId(),
			newSearchConfiguration.getSearchConfigurationId());
		Assert.assertEquals(
			existingSearchConfiguration.getGroupId(),
			newSearchConfiguration.getGroupId());
		Assert.assertEquals(
			existingSearchConfiguration.getCompanyId(),
			newSearchConfiguration.getCompanyId());
		Assert.assertEquals(
			existingSearchConfiguration.getUserId(),
			newSearchConfiguration.getUserId());
		Assert.assertEquals(
			existingSearchConfiguration.getUserName(),
			newSearchConfiguration.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSearchConfiguration.getCreateDate()),
			Time.getShortTimestamp(newSearchConfiguration.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingSearchConfiguration.getModifiedDate()),
			Time.getShortTimestamp(newSearchConfiguration.getModifiedDate()));
		Assert.assertEquals(
			existingSearchConfiguration.getStatus(),
			newSearchConfiguration.getStatus());
		Assert.assertEquals(
			existingSearchConfiguration.getStatusByUserId(),
			newSearchConfiguration.getStatusByUserId());
		Assert.assertEquals(
			existingSearchConfiguration.getStatusByUserName(),
			newSearchConfiguration.getStatusByUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSearchConfiguration.getStatusDate()),
			Time.getShortTimestamp(newSearchConfiguration.getStatusDate()));
		Assert.assertEquals(
			existingSearchConfiguration.getTitle(),
			newSearchConfiguration.getTitle());
		Assert.assertEquals(
			existingSearchConfiguration.getDescription(),
			newSearchConfiguration.getDescription());
		Assert.assertEquals(
			existingSearchConfiguration.getConfiguration(),
			newSearchConfiguration.getConfiguration());
		Assert.assertEquals(
			existingSearchConfiguration.getType(),
			newSearchConfiguration.getType());
	}

	@Test
	public void testCountByUuid() throws Exception {
		_persistence.countByUuid("");

		_persistence.countByUuid("null");

		_persistence.countByUuid((String)null);
	}

	@Test
	public void testCountByUUID_G() throws Exception {
		_persistence.countByUUID_G("", RandomTestUtil.nextLong());

		_persistence.countByUUID_G("null", 0L);

		_persistence.countByUUID_G((String)null, 0L);
	}

	@Test
	public void testCountByUuid_C() throws Exception {
		_persistence.countByUuid_C("", RandomTestUtil.nextLong());

		_persistence.countByUuid_C("null", 0L);

		_persistence.countByUuid_C((String)null, 0L);
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
	public void testCountByG_S() throws Exception {
		_persistence.countByG_S(
			RandomTestUtil.nextLong(), RandomTestUtil.nextInt());

		_persistence.countByG_S(0L, 0);
	}

	@Test
	public void testCountByG_T() throws Exception {
		_persistence.countByG_T(
			RandomTestUtil.nextLong(), RandomTestUtil.nextInt());

		_persistence.countByG_T(0L, 0);
	}

	@Test
	public void testCountByG_S_T() throws Exception {
		_persistence.countByG_S_T(
			RandomTestUtil.nextLong(), RandomTestUtil.nextInt(),
			RandomTestUtil.nextInt());

		_persistence.countByG_S_T(0L, 0, 0);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SearchConfiguration newSearchConfiguration = addSearchConfiguration();

		SearchConfiguration existingSearchConfiguration =
			_persistence.findByPrimaryKey(
				newSearchConfiguration.getPrimaryKey());

		Assert.assertEquals(
			existingSearchConfiguration, newSearchConfiguration);
	}

	@Test(expected = NoSuchConfigurationException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	@Test
	public void testFilterFindByGroupId() throws Exception {
		_persistence.filterFindByGroupId(
			0, QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SearchConfiguration> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SearchConfiguration", "mvccVersion", true, "uuid", true,
			"searchConfigurationId", true, "groupId", true, "companyId", true,
			"userId", true, "userName", true, "createDate", true,
			"modifiedDate", true, "status", true, "statusByUserId", true,
			"statusByUserName", true, "statusDate", true, "title", true,
			"description", true, "type", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SearchConfiguration newSearchConfiguration = addSearchConfiguration();

		SearchConfiguration existingSearchConfiguration =
			_persistence.fetchByPrimaryKey(
				newSearchConfiguration.getPrimaryKey());

		Assert.assertEquals(
			existingSearchConfiguration, newSearchConfiguration);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchConfiguration missingSearchConfiguration =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSearchConfiguration);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SearchConfiguration newSearchConfiguration1 = addSearchConfiguration();
		SearchConfiguration newSearchConfiguration2 = addSearchConfiguration();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchConfiguration1.getPrimaryKey());
		primaryKeys.add(newSearchConfiguration2.getPrimaryKey());

		Map<Serializable, SearchConfiguration> searchConfigurations =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, searchConfigurations.size());
		Assert.assertEquals(
			newSearchConfiguration1,
			searchConfigurations.get(newSearchConfiguration1.getPrimaryKey()));
		Assert.assertEquals(
			newSearchConfiguration2,
			searchConfigurations.get(newSearchConfiguration2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SearchConfiguration> searchConfigurations =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(searchConfigurations.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SearchConfiguration newSearchConfiguration = addSearchConfiguration();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchConfiguration.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SearchConfiguration> searchConfigurations =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, searchConfigurations.size());
		Assert.assertEquals(
			newSearchConfiguration,
			searchConfigurations.get(newSearchConfiguration.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SearchConfiguration> searchConfigurations =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(searchConfigurations.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SearchConfiguration newSearchConfiguration = addSearchConfiguration();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchConfiguration.getPrimaryKey());

		Map<Serializable, SearchConfiguration> searchConfigurations =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, searchConfigurations.size());
		Assert.assertEquals(
			newSearchConfiguration,
			searchConfigurations.get(newSearchConfiguration.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SearchConfigurationLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod
				<SearchConfiguration>() {

				@Override
				public void performAction(
					SearchConfiguration searchConfiguration) {

					Assert.assertNotNull(searchConfiguration);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SearchConfiguration newSearchConfiguration = addSearchConfiguration();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfiguration.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"searchConfigurationId",
				newSearchConfiguration.getSearchConfigurationId()));

		List<SearchConfiguration> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SearchConfiguration existingSearchConfiguration = result.get(0);

		Assert.assertEquals(
			existingSearchConfiguration, newSearchConfiguration);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfiguration.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"searchConfigurationId", RandomTestUtil.nextLong()));

		List<SearchConfiguration> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SearchConfiguration newSearchConfiguration = addSearchConfiguration();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfiguration.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("searchConfigurationId"));

		Object newSearchConfigurationId =
			newSearchConfiguration.getSearchConfigurationId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"searchConfigurationId",
				new Object[] {newSearchConfigurationId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingSearchConfigurationId = result.get(0);

		Assert.assertEquals(
			existingSearchConfigurationId, newSearchConfigurationId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfiguration.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("searchConfigurationId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"searchConfigurationId",
				new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testResetOriginalValues() throws Exception {
		SearchConfiguration newSearchConfiguration = addSearchConfiguration();

		_persistence.clearCache();

		SearchConfiguration existingSearchConfiguration =
			_persistence.findByPrimaryKey(
				newSearchConfiguration.getPrimaryKey());

		Assert.assertTrue(
			Objects.equals(
				existingSearchConfiguration.getUuid(),
				ReflectionTestUtil.invoke(
					existingSearchConfiguration, "getOriginalUuid",
					new Class<?>[0])));
		Assert.assertEquals(
			Long.valueOf(existingSearchConfiguration.getGroupId()),
			ReflectionTestUtil.<Long>invoke(
				existingSearchConfiguration, "getOriginalGroupId",
				new Class<?>[0]));
	}

	protected SearchConfiguration addSearchConfiguration() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchConfiguration searchConfiguration = _persistence.create(pk);

		searchConfiguration.setMvccVersion(RandomTestUtil.nextLong());

		searchConfiguration.setUuid(RandomTestUtil.randomString());

		searchConfiguration.setGroupId(RandomTestUtil.nextLong());

		searchConfiguration.setCompanyId(RandomTestUtil.nextLong());

		searchConfiguration.setUserId(RandomTestUtil.nextLong());

		searchConfiguration.setUserName(RandomTestUtil.randomString());

		searchConfiguration.setCreateDate(RandomTestUtil.nextDate());

		searchConfiguration.setModifiedDate(RandomTestUtil.nextDate());

		searchConfiguration.setStatus(RandomTestUtil.nextInt());

		searchConfiguration.setStatusByUserId(RandomTestUtil.nextLong());

		searchConfiguration.setStatusByUserName(RandomTestUtil.randomString());

		searchConfiguration.setStatusDate(RandomTestUtil.nextDate());

		searchConfiguration.setTitle(RandomTestUtil.randomString());

		searchConfiguration.setDescription(RandomTestUtil.randomString());

		searchConfiguration.setConfiguration(RandomTestUtil.randomString());

		searchConfiguration.setType(RandomTestUtil.nextInt());

		_searchConfigurations.add(_persistence.update(searchConfiguration));

		return searchConfiguration;
	}

	private List<SearchConfiguration> _searchConfigurations =
		new ArrayList<SearchConfiguration>();
	private SearchConfigurationPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}