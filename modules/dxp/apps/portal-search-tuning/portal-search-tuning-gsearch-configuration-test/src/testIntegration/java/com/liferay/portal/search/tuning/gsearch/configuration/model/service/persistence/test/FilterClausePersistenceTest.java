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

package com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.search.tuning.gsearch.configuration.model.exception.NoSuchFilterClauseException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.model.FilterClause;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.FilterClauseLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.FilterClausePersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.FilterClauseUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
public class FilterClausePersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED,
				"com.liferay.portal.search.tuning.gsearch.configuration.model.service"));

	@Before
	public void setUp() {
		_persistence = FilterClauseUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<FilterClause> iterator = _filterClauses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		FilterClause filterClause = _persistence.create(pk);

		Assert.assertNotNull(filterClause);

		Assert.assertEquals(filterClause.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		FilterClause newFilterClause = addFilterClause();

		_persistence.remove(newFilterClause);

		FilterClause existingFilterClause = _persistence.fetchByPrimaryKey(
			newFilterClause.getPrimaryKey());

		Assert.assertNull(existingFilterClause);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addFilterClause();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		FilterClause newFilterClause = _persistence.create(pk);

		newFilterClause.setGroupId(RandomTestUtil.nextLong());

		newFilterClause.setCompanyId(RandomTestUtil.nextLong());

		newFilterClause.setUserId(RandomTestUtil.nextLong());

		newFilterClause.setUserName(RandomTestUtil.randomString());

		newFilterClause.setCreateDate(RandomTestUtil.nextDate());

		newFilterClause.setModifiedDate(RandomTestUtil.nextDate());

		newFilterClause.setConfigurationSetId(RandomTestUtil.nextLong());

		newFilterClause.setFilterClause(RandomTestUtil.randomString());

		_filterClauses.add(_persistence.update(newFilterClause));

		FilterClause existingFilterClause = _persistence.findByPrimaryKey(
			newFilterClause.getPrimaryKey());

		Assert.assertEquals(
			existingFilterClause.getFilterClauseId(),
			newFilterClause.getFilterClauseId());
		Assert.assertEquals(
			existingFilterClause.getGroupId(), newFilterClause.getGroupId());
		Assert.assertEquals(
			existingFilterClause.getCompanyId(),
			newFilterClause.getCompanyId());
		Assert.assertEquals(
			existingFilterClause.getUserId(), newFilterClause.getUserId());
		Assert.assertEquals(
			existingFilterClause.getUserName(), newFilterClause.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingFilterClause.getCreateDate()),
			Time.getShortTimestamp(newFilterClause.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingFilterClause.getModifiedDate()),
			Time.getShortTimestamp(newFilterClause.getModifiedDate()));
		Assert.assertEquals(
			existingFilterClause.getConfigurationSetId(),
			newFilterClause.getConfigurationSetId());
		Assert.assertEquals(
			existingFilterClause.getFilterClause(),
			newFilterClause.getFilterClause());
	}

	@Test
	public void testCountByConfigurationSetId() throws Exception {
		_persistence.countByConfigurationSetId(RandomTestUtil.nextLong());

		_persistence.countByConfigurationSetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		FilterClause newFilterClause = addFilterClause();

		FilterClause existingFilterClause = _persistence.findByPrimaryKey(
			newFilterClause.getPrimaryKey());

		Assert.assertEquals(existingFilterClause, newFilterClause);
	}

	@Test(expected = NoSuchFilterClauseException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<FilterClause> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"FilterClause", "filterClauseId", true, "groupId", true,
			"companyId", true, "userId", true, "userName", true, "createDate",
			true, "modifiedDate", true, "configurationSetId", true,
			"filterClause", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		FilterClause newFilterClause = addFilterClause();

		FilterClause existingFilterClause = _persistence.fetchByPrimaryKey(
			newFilterClause.getPrimaryKey());

		Assert.assertEquals(existingFilterClause, newFilterClause);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		FilterClause missingFilterClause = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingFilterClause);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		FilterClause newFilterClause1 = addFilterClause();
		FilterClause newFilterClause2 = addFilterClause();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newFilterClause1.getPrimaryKey());
		primaryKeys.add(newFilterClause2.getPrimaryKey());

		Map<Serializable, FilterClause> filterClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, filterClauses.size());
		Assert.assertEquals(
			newFilterClause1,
			filterClauses.get(newFilterClause1.getPrimaryKey()));
		Assert.assertEquals(
			newFilterClause2,
			filterClauses.get(newFilterClause2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, FilterClause> filterClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(filterClauses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		FilterClause newFilterClause = addFilterClause();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newFilterClause.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, FilterClause> filterClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, filterClauses.size());
		Assert.assertEquals(
			newFilterClause,
			filterClauses.get(newFilterClause.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, FilterClause> filterClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(filterClauses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		FilterClause newFilterClause = addFilterClause();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newFilterClause.getPrimaryKey());

		Map<Serializable, FilterClause> filterClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, filterClauses.size());
		Assert.assertEquals(
			newFilterClause,
			filterClauses.get(newFilterClause.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			FilterClauseLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<FilterClause>() {

				@Override
				public void performAction(FilterClause filterClause) {
					Assert.assertNotNull(filterClause);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		FilterClause newFilterClause = addFilterClause();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			FilterClause.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"filterClauseId", newFilterClause.getFilterClauseId()));

		List<FilterClause> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		FilterClause existingFilterClause = result.get(0);

		Assert.assertEquals(existingFilterClause, newFilterClause);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			FilterClause.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"filterClauseId", RandomTestUtil.nextLong()));

		List<FilterClause> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		FilterClause newFilterClause = addFilterClause();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			FilterClause.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("filterClauseId"));

		Object newFilterClauseId = newFilterClause.getFilterClauseId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"filterClauseId", new Object[] {newFilterClauseId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingFilterClauseId = result.get(0);

		Assert.assertEquals(existingFilterClauseId, newFilterClauseId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			FilterClause.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("filterClauseId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"filterClauseId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected FilterClause addFilterClause() throws Exception {
		long pk = RandomTestUtil.nextLong();

		FilterClause filterClause = _persistence.create(pk);

		filterClause.setGroupId(RandomTestUtil.nextLong());

		filterClause.setCompanyId(RandomTestUtil.nextLong());

		filterClause.setUserId(RandomTestUtil.nextLong());

		filterClause.setUserName(RandomTestUtil.randomString());

		filterClause.setCreateDate(RandomTestUtil.nextDate());

		filterClause.setModifiedDate(RandomTestUtil.nextDate());

		filterClause.setConfigurationSetId(RandomTestUtil.nextLong());

		filterClause.setFilterClause(RandomTestUtil.randomString());

		_filterClauses.add(_persistence.update(filterClause));

		return filterClause;
	}

	private List<FilterClause> _filterClauses = new ArrayList<FilterClause>();
	private FilterClausePersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}