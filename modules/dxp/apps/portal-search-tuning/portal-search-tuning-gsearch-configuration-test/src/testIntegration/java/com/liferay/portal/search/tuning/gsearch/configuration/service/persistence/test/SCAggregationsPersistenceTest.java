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
import com.liferay.portal.search.tuning.gsearch.configuration.model.SCAggregations;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SCAggregationsLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCAggregationsPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCAggregationsUtil;
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
public class SCAggregationsPersistenceTest {

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
		_persistence = SCAggregationsUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SCAggregations> iterator = _scAggregationses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCAggregations scAggregations = _persistence.create(pk);

		Assert.assertNotNull(scAggregations);

		Assert.assertEquals(scAggregations.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SCAggregations newSCAggregations = addSCAggregations();

		_persistence.remove(newSCAggregations);

		SCAggregations existingSCAggregations = _persistence.fetchByPrimaryKey(
			newSCAggregations.getPrimaryKey());

		Assert.assertNull(existingSCAggregations);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSCAggregations();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCAggregations newSCAggregations = _persistence.create(pk);

		newSCAggregations.setMvccVersion(RandomTestUtil.nextLong());

		newSCAggregations.setGroupId(RandomTestUtil.nextLong());

		newSCAggregations.setCompanyId(RandomTestUtil.nextLong());

		newSCAggregations.setUserId(RandomTestUtil.nextLong());

		newSCAggregations.setUserName(RandomTestUtil.randomString());

		newSCAggregations.setCreateDate(RandomTestUtil.nextDate());

		newSCAggregations.setModifiedDate(RandomTestUtil.nextDate());

		newSCAggregations.setSetId(RandomTestUtil.nextLong());

		newSCAggregations.setAggregations(RandomTestUtil.randomString());

		_scAggregationses.add(_persistence.update(newSCAggregations));

		SCAggregations existingSCAggregations = _persistence.findByPrimaryKey(
			newSCAggregations.getPrimaryKey());

		Assert.assertEquals(
			existingSCAggregations.getMvccVersion(),
			newSCAggregations.getMvccVersion());
		Assert.assertEquals(
			existingSCAggregations.getAggregationsId(),
			newSCAggregations.getAggregationsId());
		Assert.assertEquals(
			existingSCAggregations.getGroupId(),
			newSCAggregations.getGroupId());
		Assert.assertEquals(
			existingSCAggregations.getCompanyId(),
			newSCAggregations.getCompanyId());
		Assert.assertEquals(
			existingSCAggregations.getUserId(), newSCAggregations.getUserId());
		Assert.assertEquals(
			existingSCAggregations.getUserName(),
			newSCAggregations.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCAggregations.getCreateDate()),
			Time.getShortTimestamp(newSCAggregations.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCAggregations.getModifiedDate()),
			Time.getShortTimestamp(newSCAggregations.getModifiedDate()));
		Assert.assertEquals(
			existingSCAggregations.getSetId(), newSCAggregations.getSetId());
		Assert.assertEquals(
			existingSCAggregations.getAggregations(),
			newSCAggregations.getAggregations());
	}

	@Test
	public void testCountBySetId() throws Exception {
		_persistence.countBySetId(RandomTestUtil.nextLong());

		_persistence.countBySetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SCAggregations newSCAggregations = addSCAggregations();

		SCAggregations existingSCAggregations = _persistence.findByPrimaryKey(
			newSCAggregations.getPrimaryKey());

		Assert.assertEquals(existingSCAggregations, newSCAggregations);
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

	protected OrderByComparator<SCAggregations> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SCAggregations", "mvccVersion", true, "aggregationsId", true,
			"groupId", true, "companyId", true, "userId", true, "userName",
			true, "createDate", true, "modifiedDate", true, "setId", true,
			"aggregations", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SCAggregations newSCAggregations = addSCAggregations();

		SCAggregations existingSCAggregations = _persistence.fetchByPrimaryKey(
			newSCAggregations.getPrimaryKey());

		Assert.assertEquals(existingSCAggregations, newSCAggregations);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCAggregations missingSCAggregations = _persistence.fetchByPrimaryKey(
			pk);

		Assert.assertNull(missingSCAggregations);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SCAggregations newSCAggregations1 = addSCAggregations();
		SCAggregations newSCAggregations2 = addSCAggregations();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCAggregations1.getPrimaryKey());
		primaryKeys.add(newSCAggregations2.getPrimaryKey());

		Map<Serializable, SCAggregations> scAggregationses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, scAggregationses.size());
		Assert.assertEquals(
			newSCAggregations1,
			scAggregationses.get(newSCAggregations1.getPrimaryKey()));
		Assert.assertEquals(
			newSCAggregations2,
			scAggregationses.get(newSCAggregations2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SCAggregations> scAggregationses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scAggregationses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SCAggregations newSCAggregations = addSCAggregations();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCAggregations.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SCAggregations> scAggregationses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scAggregationses.size());
		Assert.assertEquals(
			newSCAggregations,
			scAggregationses.get(newSCAggregations.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SCAggregations> scAggregationses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scAggregationses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SCAggregations newSCAggregations = addSCAggregations();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCAggregations.getPrimaryKey());

		Map<Serializable, SCAggregations> scAggregationses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scAggregationses.size());
		Assert.assertEquals(
			newSCAggregations,
			scAggregationses.get(newSCAggregations.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SCAggregationsLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SCAggregations>() {

				@Override
				public void performAction(SCAggregations scAggregations) {
					Assert.assertNotNull(scAggregations);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SCAggregations newSCAggregations = addSCAggregations();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCAggregations.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"aggregationsId", newSCAggregations.getAggregationsId()));

		List<SCAggregations> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SCAggregations existingSCAggregations = result.get(0);

		Assert.assertEquals(existingSCAggregations, newSCAggregations);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCAggregations.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"aggregationsId", RandomTestUtil.nextLong()));

		List<SCAggregations> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SCAggregations newSCAggregations = addSCAggregations();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCAggregations.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("aggregationsId"));

		Object newAggregationsId = newSCAggregations.getAggregationsId();

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
			SCAggregations.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("aggregationsId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"aggregationsId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SCAggregations addSCAggregations() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCAggregations scAggregations = _persistence.create(pk);

		scAggregations.setMvccVersion(RandomTestUtil.nextLong());

		scAggregations.setGroupId(RandomTestUtil.nextLong());

		scAggregations.setCompanyId(RandomTestUtil.nextLong());

		scAggregations.setUserId(RandomTestUtil.nextLong());

		scAggregations.setUserName(RandomTestUtil.randomString());

		scAggregations.setCreateDate(RandomTestUtil.nextDate());

		scAggregations.setModifiedDate(RandomTestUtil.nextDate());

		scAggregations.setSetId(RandomTestUtil.nextLong());

		scAggregations.setAggregations(RandomTestUtil.randomString());

		_scAggregationses.add(_persistence.update(scAggregations));

		return scAggregations;
	}

	private List<SCAggregations> _scAggregationses =
		new ArrayList<SCAggregations>();
	private SCAggregationsPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}