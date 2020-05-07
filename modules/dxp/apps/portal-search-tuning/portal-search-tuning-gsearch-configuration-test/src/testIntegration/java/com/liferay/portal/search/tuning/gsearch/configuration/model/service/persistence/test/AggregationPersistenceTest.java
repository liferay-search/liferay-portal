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
import com.liferay.portal.search.tuning.gsearch.configuration.model.exception.NoSuchAggregationException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.model.Aggregation;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.AggregationLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.AggregationPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.AggregationUtil;
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
public class AggregationPersistenceTest {

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
		_persistence = AggregationUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<Aggregation> iterator = _aggregations.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		Aggregation aggregation = _persistence.create(pk);

		Assert.assertNotNull(aggregation);

		Assert.assertEquals(aggregation.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		Aggregation newAggregation = addAggregation();

		_persistence.remove(newAggregation);

		Aggregation existingAggregation = _persistence.fetchByPrimaryKey(
			newAggregation.getPrimaryKey());

		Assert.assertNull(existingAggregation);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addAggregation();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		Aggregation newAggregation = _persistence.create(pk);

		newAggregation.setGroupId(RandomTestUtil.nextLong());

		newAggregation.setCompanyId(RandomTestUtil.nextLong());

		newAggregation.setUserId(RandomTestUtil.nextLong());

		newAggregation.setUserName(RandomTestUtil.randomString());

		newAggregation.setCreateDate(RandomTestUtil.nextDate());

		newAggregation.setModifiedDate(RandomTestUtil.nextDate());

		newAggregation.setConfigurationSetId(RandomTestUtil.nextLong());

		newAggregation.setAggregation(RandomTestUtil.randomString());

		_aggregations.add(_persistence.update(newAggregation));

		Aggregation existingAggregation = _persistence.findByPrimaryKey(
			newAggregation.getPrimaryKey());

		Assert.assertEquals(
			existingAggregation.getAggregationId(),
			newAggregation.getAggregationId());
		Assert.assertEquals(
			existingAggregation.getGroupId(), newAggregation.getGroupId());
		Assert.assertEquals(
			existingAggregation.getCompanyId(), newAggregation.getCompanyId());
		Assert.assertEquals(
			existingAggregation.getUserId(), newAggregation.getUserId());
		Assert.assertEquals(
			existingAggregation.getUserName(), newAggregation.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingAggregation.getCreateDate()),
			Time.getShortTimestamp(newAggregation.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingAggregation.getModifiedDate()),
			Time.getShortTimestamp(newAggregation.getModifiedDate()));
		Assert.assertEquals(
			existingAggregation.getConfigurationSetId(),
			newAggregation.getConfigurationSetId());
		Assert.assertEquals(
			existingAggregation.getAggregation(),
			newAggregation.getAggregation());
	}

	@Test
	public void testCountByConfigurationSetId() throws Exception {
		_persistence.countByConfigurationSetId(RandomTestUtil.nextLong());

		_persistence.countByConfigurationSetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		Aggregation newAggregation = addAggregation();

		Aggregation existingAggregation = _persistence.findByPrimaryKey(
			newAggregation.getPrimaryKey());

		Assert.assertEquals(existingAggregation, newAggregation);
	}

	@Test(expected = NoSuchAggregationException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<Aggregation> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"Aggregation", "aggregationId", true, "groupId", true, "companyId",
			true, "userId", true, "userName", true, "createDate", true,
			"modifiedDate", true, "configurationSetId", true, "aggregation",
			true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		Aggregation newAggregation = addAggregation();

		Aggregation existingAggregation = _persistence.fetchByPrimaryKey(
			newAggregation.getPrimaryKey());

		Assert.assertEquals(existingAggregation, newAggregation);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		Aggregation missingAggregation = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingAggregation);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		Aggregation newAggregation1 = addAggregation();
		Aggregation newAggregation2 = addAggregation();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newAggregation1.getPrimaryKey());
		primaryKeys.add(newAggregation2.getPrimaryKey());

		Map<Serializable, Aggregation> aggregations =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, aggregations.size());
		Assert.assertEquals(
			newAggregation1, aggregations.get(newAggregation1.getPrimaryKey()));
		Assert.assertEquals(
			newAggregation2, aggregations.get(newAggregation2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, Aggregation> aggregations =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(aggregations.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		Aggregation newAggregation = addAggregation();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newAggregation.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, Aggregation> aggregations =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, aggregations.size());
		Assert.assertEquals(
			newAggregation, aggregations.get(newAggregation.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, Aggregation> aggregations =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(aggregations.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		Aggregation newAggregation = addAggregation();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newAggregation.getPrimaryKey());

		Map<Serializable, Aggregation> aggregations =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, aggregations.size());
		Assert.assertEquals(
			newAggregation, aggregations.get(newAggregation.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			AggregationLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<Aggregation>() {

				@Override
				public void performAction(Aggregation aggregation) {
					Assert.assertNotNull(aggregation);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		Aggregation newAggregation = addAggregation();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			Aggregation.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"aggregationId", newAggregation.getAggregationId()));

		List<Aggregation> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		Aggregation existingAggregation = result.get(0);

		Assert.assertEquals(existingAggregation, newAggregation);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			Aggregation.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"aggregationId", RandomTestUtil.nextLong()));

		List<Aggregation> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		Aggregation newAggregation = addAggregation();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			Aggregation.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("aggregationId"));

		Object newAggregationId = newAggregation.getAggregationId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"aggregationId", new Object[] {newAggregationId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingAggregationId = result.get(0);

		Assert.assertEquals(existingAggregationId, newAggregationId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			Aggregation.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("aggregationId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"aggregationId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected Aggregation addAggregation() throws Exception {
		long pk = RandomTestUtil.nextLong();

		Aggregation aggregation = _persistence.create(pk);

		aggregation.setGroupId(RandomTestUtil.nextLong());

		aggregation.setCompanyId(RandomTestUtil.nextLong());

		aggregation.setUserId(RandomTestUtil.nextLong());

		aggregation.setUserName(RandomTestUtil.randomString());

		aggregation.setCreateDate(RandomTestUtil.nextDate());

		aggregation.setModifiedDate(RandomTestUtil.nextDate());

		aggregation.setConfigurationSetId(RandomTestUtil.nextLong());

		aggregation.setAggregation(RandomTestUtil.randomString());

		_aggregations.add(_persistence.update(aggregation));

		return aggregation;
	}

	private List<Aggregation> _aggregations = new ArrayList<Aggregation>();
	private AggregationPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}