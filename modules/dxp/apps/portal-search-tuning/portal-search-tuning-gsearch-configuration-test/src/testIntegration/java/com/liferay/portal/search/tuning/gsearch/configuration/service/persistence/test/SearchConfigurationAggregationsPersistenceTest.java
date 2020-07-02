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
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchAggregationsException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfigurationAggregations;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationAggregationsLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SearchConfigurationAggregationsPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SearchConfigurationAggregationsUtil;
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
public class SearchConfigurationAggregationsPersistenceTest {

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
		_persistence = SearchConfigurationAggregationsUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SearchConfigurationAggregations> iterator =
			_searchConfigurationAggregationses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchConfigurationAggregations searchConfigurationAggregations =
			_persistence.create(pk);

		Assert.assertNotNull(searchConfigurationAggregations);

		Assert.assertEquals(
			searchConfigurationAggregations.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SearchConfigurationAggregations newSearchConfigurationAggregations =
			addSearchConfigurationAggregations();

		_persistence.remove(newSearchConfigurationAggregations);

		SearchConfigurationAggregations
			existingSearchConfigurationAggregations =
				_persistence.fetchByPrimaryKey(
					newSearchConfigurationAggregations.getPrimaryKey());

		Assert.assertNull(existingSearchConfigurationAggregations);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSearchConfigurationAggregations();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchConfigurationAggregations newSearchConfigurationAggregations =
			_persistence.create(pk);

		newSearchConfigurationAggregations.setMvccVersion(
			RandomTestUtil.nextLong());

		newSearchConfigurationAggregations.setGroupId(
			RandomTestUtil.nextLong());

		newSearchConfigurationAggregations.setCompanyId(
			RandomTestUtil.nextLong());

		newSearchConfigurationAggregations.setUserId(RandomTestUtil.nextLong());

		newSearchConfigurationAggregations.setUserName(
			RandomTestUtil.randomString());

		newSearchConfigurationAggregations.setCreateDate(
			RandomTestUtil.nextDate());

		newSearchConfigurationAggregations.setModifiedDate(
			RandomTestUtil.nextDate());

		newSearchConfigurationAggregations.setSearchConfigurationSetId(
			RandomTestUtil.nextLong());

		newSearchConfigurationAggregations.setAggregations(
			RandomTestUtil.randomString());

		_searchConfigurationAggregationses.add(
			_persistence.update(newSearchConfigurationAggregations));

		SearchConfigurationAggregations
			existingSearchConfigurationAggregations =
				_persistence.findByPrimaryKey(
					newSearchConfigurationAggregations.getPrimaryKey());

		Assert.assertEquals(
			existingSearchConfigurationAggregations.getMvccVersion(),
			newSearchConfigurationAggregations.getMvccVersion());
		Assert.assertEquals(
			existingSearchConfigurationAggregations.getAggregationsId(),
			newSearchConfigurationAggregations.getAggregationsId());
		Assert.assertEquals(
			existingSearchConfigurationAggregations.getGroupId(),
			newSearchConfigurationAggregations.getGroupId());
		Assert.assertEquals(
			existingSearchConfigurationAggregations.getCompanyId(),
			newSearchConfigurationAggregations.getCompanyId());
		Assert.assertEquals(
			existingSearchConfigurationAggregations.getUserId(),
			newSearchConfigurationAggregations.getUserId());
		Assert.assertEquals(
			existingSearchConfigurationAggregations.getUserName(),
			newSearchConfigurationAggregations.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingSearchConfigurationAggregations.getCreateDate()),
			Time.getShortTimestamp(
				newSearchConfigurationAggregations.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingSearchConfigurationAggregations.getModifiedDate()),
			Time.getShortTimestamp(
				newSearchConfigurationAggregations.getModifiedDate()));
		Assert.assertEquals(
			existingSearchConfigurationAggregations.
				getSearchConfigurationSetId(),
			newSearchConfigurationAggregations.getSearchConfigurationSetId());
		Assert.assertEquals(
			existingSearchConfigurationAggregations.getAggregations(),
			newSearchConfigurationAggregations.getAggregations());
	}

	@Test
	public void testCountBySearchConfigurationSetId() throws Exception {
		_persistence.countBySearchConfigurationSetId(RandomTestUtil.nextLong());

		_persistence.countBySearchConfigurationSetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SearchConfigurationAggregations newSearchConfigurationAggregations =
			addSearchConfigurationAggregations();

		SearchConfigurationAggregations
			existingSearchConfigurationAggregations =
				_persistence.findByPrimaryKey(
					newSearchConfigurationAggregations.getPrimaryKey());

		Assert.assertEquals(
			existingSearchConfigurationAggregations,
			newSearchConfigurationAggregations);
	}

	@Test(expected = NoSuchAggregationsException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SearchConfigurationAggregations>
		getOrderByComparator() {

		return OrderByComparatorFactoryUtil.create(
			"SearchConfigurationAggregations", "mvccVersion", true,
			"aggregationsId", true, "groupId", true, "companyId", true,
			"userId", true, "userName", true, "createDate", true,
			"modifiedDate", true, "searchConfigurationSetId", true,
			"aggregations", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SearchConfigurationAggregations newSearchConfigurationAggregations =
			addSearchConfigurationAggregations();

		SearchConfigurationAggregations
			existingSearchConfigurationAggregations =
				_persistence.fetchByPrimaryKey(
					newSearchConfigurationAggregations.getPrimaryKey());

		Assert.assertEquals(
			existingSearchConfigurationAggregations,
			newSearchConfigurationAggregations);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchConfigurationAggregations missingSearchConfigurationAggregations =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSearchConfigurationAggregations);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SearchConfigurationAggregations newSearchConfigurationAggregations1 =
			addSearchConfigurationAggregations();
		SearchConfigurationAggregations newSearchConfigurationAggregations2 =
			addSearchConfigurationAggregations();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchConfigurationAggregations1.getPrimaryKey());
		primaryKeys.add(newSearchConfigurationAggregations2.getPrimaryKey());

		Map<Serializable, SearchConfigurationAggregations>
			searchConfigurationAggregationses = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(2, searchConfigurationAggregationses.size());
		Assert.assertEquals(
			newSearchConfigurationAggregations1,
			searchConfigurationAggregationses.get(
				newSearchConfigurationAggregations1.getPrimaryKey()));
		Assert.assertEquals(
			newSearchConfigurationAggregations2,
			searchConfigurationAggregationses.get(
				newSearchConfigurationAggregations2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SearchConfigurationAggregations>
			searchConfigurationAggregationses = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(searchConfigurationAggregationses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SearchConfigurationAggregations newSearchConfigurationAggregations =
			addSearchConfigurationAggregations();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchConfigurationAggregations.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SearchConfigurationAggregations>
			searchConfigurationAggregationses = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, searchConfigurationAggregationses.size());
		Assert.assertEquals(
			newSearchConfigurationAggregations,
			searchConfigurationAggregationses.get(
				newSearchConfigurationAggregations.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SearchConfigurationAggregations>
			searchConfigurationAggregationses = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(searchConfigurationAggregationses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SearchConfigurationAggregations newSearchConfigurationAggregations =
			addSearchConfigurationAggregations();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchConfigurationAggregations.getPrimaryKey());

		Map<Serializable, SearchConfigurationAggregations>
			searchConfigurationAggregationses = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, searchConfigurationAggregationses.size());
		Assert.assertEquals(
			newSearchConfigurationAggregations,
			searchConfigurationAggregationses.get(
				newSearchConfigurationAggregations.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SearchConfigurationAggregationsLocalServiceUtil.
				getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod
				<SearchConfigurationAggregations>() {

				@Override
				public void performAction(
					SearchConfigurationAggregations
						searchConfigurationAggregations) {

					Assert.assertNotNull(searchConfigurationAggregations);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SearchConfigurationAggregations newSearchConfigurationAggregations =
			addSearchConfigurationAggregations();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfigurationAggregations.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"aggregationsId",
				newSearchConfigurationAggregations.getAggregationsId()));

		List<SearchConfigurationAggregations> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		SearchConfigurationAggregations
			existingSearchConfigurationAggregations = result.get(0);

		Assert.assertEquals(
			existingSearchConfigurationAggregations,
			newSearchConfigurationAggregations);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfigurationAggregations.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"aggregationsId", RandomTestUtil.nextLong()));

		List<SearchConfigurationAggregations> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SearchConfigurationAggregations newSearchConfigurationAggregations =
			addSearchConfigurationAggregations();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfigurationAggregations.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("aggregationsId"));

		Object newAggregationsId =
			newSearchConfigurationAggregations.getAggregationsId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"aggregationsId", new Object[] {newAggregationsId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingAggregationsId = result.get(0);

		Assert.assertEquals(existingAggregationsId, newAggregationsId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfigurationAggregations.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("aggregationsId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"aggregationsId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SearchConfigurationAggregations
			addSearchConfigurationAggregations()
		throws Exception {

		long pk = RandomTestUtil.nextLong();

		SearchConfigurationAggregations searchConfigurationAggregations =
			_persistence.create(pk);

		searchConfigurationAggregations.setMvccVersion(
			RandomTestUtil.nextLong());

		searchConfigurationAggregations.setGroupId(RandomTestUtil.nextLong());

		searchConfigurationAggregations.setCompanyId(RandomTestUtil.nextLong());

		searchConfigurationAggregations.setUserId(RandomTestUtil.nextLong());

		searchConfigurationAggregations.setUserName(
			RandomTestUtil.randomString());

		searchConfigurationAggregations.setCreateDate(
			RandomTestUtil.nextDate());

		searchConfigurationAggregations.setModifiedDate(
			RandomTestUtil.nextDate());

		searchConfigurationAggregations.setSearchConfigurationSetId(
			RandomTestUtil.nextLong());

		searchConfigurationAggregations.setAggregations(
			RandomTestUtil.randomString());

		_searchConfigurationAggregationses.add(
			_persistence.update(searchConfigurationAggregations));

		return searchConfigurationAggregations;
	}

	private List<SearchConfigurationAggregations>
		_searchConfigurationAggregationses =
			new ArrayList<SearchConfigurationAggregations>();
	private SearchConfigurationAggregationsPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}