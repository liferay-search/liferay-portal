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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchSynonymSetException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SynonymSet;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SynonymSetLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SynonymSetPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SynonymSetUtil;
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
public class SynonymSetPersistenceTest {

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
		_persistence = SynonymSetUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SynonymSet> iterator = _synonymSets.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SynonymSet synonymSet = _persistence.create(pk);

		Assert.assertNotNull(synonymSet);

		Assert.assertEquals(synonymSet.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SynonymSet newSynonymSet = addSynonymSet();

		_persistence.remove(newSynonymSet);

		SynonymSet existingSynonymSet = _persistence.fetchByPrimaryKey(
			newSynonymSet.getPrimaryKey());

		Assert.assertNull(existingSynonymSet);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSynonymSet();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SynonymSet newSynonymSet = _persistence.create(pk);

		newSynonymSet.setGroupId(RandomTestUtil.nextLong());

		newSynonymSet.setCompanyId(RandomTestUtil.nextLong());

		newSynonymSet.setUserId(RandomTestUtil.nextLong());

		newSynonymSet.setUserName(RandomTestUtil.randomString());

		newSynonymSet.setCreateDate(RandomTestUtil.nextDate());

		newSynonymSet.setModifiedDate(RandomTestUtil.nextDate());

		newSynonymSet.setConfigurationSetId(RandomTestUtil.nextLong());

		newSynonymSet.setSynonymSet(RandomTestUtil.randomString());

		_synonymSets.add(_persistence.update(newSynonymSet));

		SynonymSet existingSynonymSet = _persistence.findByPrimaryKey(
			newSynonymSet.getPrimaryKey());

		Assert.assertEquals(
			existingSynonymSet.getSynonymSetId(),
			newSynonymSet.getSynonymSetId());
		Assert.assertEquals(
			existingSynonymSet.getGroupId(), newSynonymSet.getGroupId());
		Assert.assertEquals(
			existingSynonymSet.getCompanyId(), newSynonymSet.getCompanyId());
		Assert.assertEquals(
			existingSynonymSet.getUserId(), newSynonymSet.getUserId());
		Assert.assertEquals(
			existingSynonymSet.getUserName(), newSynonymSet.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSynonymSet.getCreateDate()),
			Time.getShortTimestamp(newSynonymSet.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSynonymSet.getModifiedDate()),
			Time.getShortTimestamp(newSynonymSet.getModifiedDate()));
		Assert.assertEquals(
			existingSynonymSet.getConfigurationSetId(),
			newSynonymSet.getConfigurationSetId());
		Assert.assertEquals(
			existingSynonymSet.getSynonymSet(), newSynonymSet.getSynonymSet());
	}

	@Test
	public void testCountByConfigurationSetId() throws Exception {
		_persistence.countByConfigurationSetId(RandomTestUtil.nextLong());

		_persistence.countByConfigurationSetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SynonymSet newSynonymSet = addSynonymSet();

		SynonymSet existingSynonymSet = _persistence.findByPrimaryKey(
			newSynonymSet.getPrimaryKey());

		Assert.assertEquals(existingSynonymSet, newSynonymSet);
	}

	@Test(expected = NoSuchSynonymSetException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SynonymSet> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SynonymSet", "synonymSetId", true, "groupId", true, "companyId",
			true, "userId", true, "userName", true, "createDate", true,
			"modifiedDate", true, "configurationSetId", true, "synonymSet",
			true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SynonymSet newSynonymSet = addSynonymSet();

		SynonymSet existingSynonymSet = _persistence.fetchByPrimaryKey(
			newSynonymSet.getPrimaryKey());

		Assert.assertEquals(existingSynonymSet, newSynonymSet);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SynonymSet missingSynonymSet = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSynonymSet);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SynonymSet newSynonymSet1 = addSynonymSet();
		SynonymSet newSynonymSet2 = addSynonymSet();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSynonymSet1.getPrimaryKey());
		primaryKeys.add(newSynonymSet2.getPrimaryKey());

		Map<Serializable, SynonymSet> synonymSets =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, synonymSets.size());
		Assert.assertEquals(
			newSynonymSet1, synonymSets.get(newSynonymSet1.getPrimaryKey()));
		Assert.assertEquals(
			newSynonymSet2, synonymSets.get(newSynonymSet2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SynonymSet> synonymSets =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(synonymSets.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SynonymSet newSynonymSet = addSynonymSet();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSynonymSet.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SynonymSet> synonymSets =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, synonymSets.size());
		Assert.assertEquals(
			newSynonymSet, synonymSets.get(newSynonymSet.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SynonymSet> synonymSets =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(synonymSets.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SynonymSet newSynonymSet = addSynonymSet();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSynonymSet.getPrimaryKey());

		Map<Serializable, SynonymSet> synonymSets =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, synonymSets.size());
		Assert.assertEquals(
			newSynonymSet, synonymSets.get(newSynonymSet.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SynonymSetLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SynonymSet>() {

				@Override
				public void performAction(SynonymSet synonymSet) {
					Assert.assertNotNull(synonymSet);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SynonymSet newSynonymSet = addSynonymSet();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SynonymSet.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"synonymSetId", newSynonymSet.getSynonymSetId()));

		List<SynonymSet> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SynonymSet existingSynonymSet = result.get(0);

		Assert.assertEquals(existingSynonymSet, newSynonymSet);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SynonymSet.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"synonymSetId", RandomTestUtil.nextLong()));

		List<SynonymSet> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SynonymSet newSynonymSet = addSynonymSet();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SynonymSet.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("synonymSetId"));

		Object newSynonymSetId = newSynonymSet.getSynonymSetId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"synonymSetId", new Object[] {newSynonymSetId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingSynonymSetId = result.get(0);

		Assert.assertEquals(existingSynonymSetId, newSynonymSetId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SynonymSet.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("synonymSetId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"synonymSetId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SynonymSet addSynonymSet() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SynonymSet synonymSet = _persistence.create(pk);

		synonymSet.setGroupId(RandomTestUtil.nextLong());

		synonymSet.setCompanyId(RandomTestUtil.nextLong());

		synonymSet.setUserId(RandomTestUtil.nextLong());

		synonymSet.setUserName(RandomTestUtil.randomString());

		synonymSet.setCreateDate(RandomTestUtil.nextDate());

		synonymSet.setModifiedDate(RandomTestUtil.nextDate());

		synonymSet.setConfigurationSetId(RandomTestUtil.nextLong());

		synonymSet.setSynonymSet(RandomTestUtil.randomString());

		_synonymSets.add(_persistence.update(synonymSet));

		return synonymSet;
	}

	private List<SynonymSet> _synonymSets = new ArrayList<SynonymSet>();
	private SynonymSetPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}