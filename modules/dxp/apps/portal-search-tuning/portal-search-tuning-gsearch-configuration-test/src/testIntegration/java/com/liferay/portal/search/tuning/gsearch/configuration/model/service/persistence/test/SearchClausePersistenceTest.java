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
import com.liferay.portal.search.tuning.gsearch.configuration.model.exception.NoSuchSearchClauseException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.model.SearchClause;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.SearchClauseLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.SearchClausePersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.SearchClauseUtil;
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
public class SearchClausePersistenceTest {

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
		_persistence = SearchClauseUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SearchClause> iterator = _searchClauses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchClause searchClause = _persistence.create(pk);

		Assert.assertNotNull(searchClause);

		Assert.assertEquals(searchClause.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SearchClause newSearchClause = addSearchClause();

		_persistence.remove(newSearchClause);

		SearchClause existingSearchClause = _persistence.fetchByPrimaryKey(
			newSearchClause.getPrimaryKey());

		Assert.assertNull(existingSearchClause);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSearchClause();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchClause newSearchClause = _persistence.create(pk);

		newSearchClause.setGroupId(RandomTestUtil.nextLong());

		newSearchClause.setCompanyId(RandomTestUtil.nextLong());

		newSearchClause.setUserId(RandomTestUtil.nextLong());

		newSearchClause.setUserName(RandomTestUtil.randomString());

		newSearchClause.setCreateDate(RandomTestUtil.nextDate());

		newSearchClause.setModifiedDate(RandomTestUtil.nextDate());

		newSearchClause.setConfigurationSetId(RandomTestUtil.nextLong());

		newSearchClause.setSearchClause(RandomTestUtil.randomString());

		_searchClauses.add(_persistence.update(newSearchClause));

		SearchClause existingSearchClause = _persistence.findByPrimaryKey(
			newSearchClause.getPrimaryKey());

		Assert.assertEquals(
			existingSearchClause.getSearchClauseId(),
			newSearchClause.getSearchClauseId());
		Assert.assertEquals(
			existingSearchClause.getGroupId(), newSearchClause.getGroupId());
		Assert.assertEquals(
			existingSearchClause.getCompanyId(),
			newSearchClause.getCompanyId());
		Assert.assertEquals(
			existingSearchClause.getUserId(), newSearchClause.getUserId());
		Assert.assertEquals(
			existingSearchClause.getUserName(), newSearchClause.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSearchClause.getCreateDate()),
			Time.getShortTimestamp(newSearchClause.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSearchClause.getModifiedDate()),
			Time.getShortTimestamp(newSearchClause.getModifiedDate()));
		Assert.assertEquals(
			existingSearchClause.getConfigurationSetId(),
			newSearchClause.getConfigurationSetId());
		Assert.assertEquals(
			existingSearchClause.getSearchClause(),
			newSearchClause.getSearchClause());
	}

	@Test
	public void testCountByConfigurationSetId() throws Exception {
		_persistence.countByConfigurationSetId(RandomTestUtil.nextLong());

		_persistence.countByConfigurationSetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SearchClause newSearchClause = addSearchClause();

		SearchClause existingSearchClause = _persistence.findByPrimaryKey(
			newSearchClause.getPrimaryKey());

		Assert.assertEquals(existingSearchClause, newSearchClause);
	}

	@Test(expected = NoSuchSearchClauseException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SearchClause> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SearchClause", "searchClauseId", true, "groupId", true,
			"companyId", true, "userId", true, "userName", true, "createDate",
			true, "modifiedDate", true, "configurationSetId", true,
			"searchClause", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SearchClause newSearchClause = addSearchClause();

		SearchClause existingSearchClause = _persistence.fetchByPrimaryKey(
			newSearchClause.getPrimaryKey());

		Assert.assertEquals(existingSearchClause, newSearchClause);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchClause missingSearchClause = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSearchClause);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SearchClause newSearchClause1 = addSearchClause();
		SearchClause newSearchClause2 = addSearchClause();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchClause1.getPrimaryKey());
		primaryKeys.add(newSearchClause2.getPrimaryKey());

		Map<Serializable, SearchClause> searchClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, searchClauses.size());
		Assert.assertEquals(
			newSearchClause1,
			searchClauses.get(newSearchClause1.getPrimaryKey()));
		Assert.assertEquals(
			newSearchClause2,
			searchClauses.get(newSearchClause2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SearchClause> searchClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(searchClauses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SearchClause newSearchClause = addSearchClause();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchClause.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SearchClause> searchClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, searchClauses.size());
		Assert.assertEquals(
			newSearchClause,
			searchClauses.get(newSearchClause.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SearchClause> searchClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(searchClauses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SearchClause newSearchClause = addSearchClause();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchClause.getPrimaryKey());

		Map<Serializable, SearchClause> searchClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, searchClauses.size());
		Assert.assertEquals(
			newSearchClause,
			searchClauses.get(newSearchClause.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SearchClauseLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SearchClause>() {

				@Override
				public void performAction(SearchClause searchClause) {
					Assert.assertNotNull(searchClause);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SearchClause newSearchClause = addSearchClause();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchClause.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"searchClauseId", newSearchClause.getSearchClauseId()));

		List<SearchClause> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SearchClause existingSearchClause = result.get(0);

		Assert.assertEquals(existingSearchClause, newSearchClause);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchClause.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"searchClauseId", RandomTestUtil.nextLong()));

		List<SearchClause> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SearchClause newSearchClause = addSearchClause();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchClause.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("searchClauseId"));

		Object newSearchClauseId = newSearchClause.getSearchClauseId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"searchClauseId", new Object[] {newSearchClauseId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingSearchClauseId = result.get(0);

		Assert.assertEquals(existingSearchClauseId, newSearchClauseId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchClause.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("searchClauseId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"searchClauseId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SearchClause addSearchClause() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchClause searchClause = _persistence.create(pk);

		searchClause.setGroupId(RandomTestUtil.nextLong());

		searchClause.setCompanyId(RandomTestUtil.nextLong());

		searchClause.setUserId(RandomTestUtil.nextLong());

		searchClause.setUserName(RandomTestUtil.randomString());

		searchClause.setCreateDate(RandomTestUtil.nextDate());

		searchClause.setModifiedDate(RandomTestUtil.nextDate());

		searchClause.setConfigurationSetId(RandomTestUtil.nextLong());

		searchClause.setSearchClause(RandomTestUtil.randomString());

		_searchClauses.add(_persistence.update(searchClause));

		return searchClause;
	}

	private List<SearchClause> _searchClauses = new ArrayList<SearchClause>();
	private SearchClausePersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}