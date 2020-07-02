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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchSynonymSetsException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SCSynonymSets;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SCSynonymSetsLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCSynonymSetsPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCSynonymSetsUtil;
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
public class SCSynonymSetsPersistenceTest {

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
		_persistence = SCSynonymSetsUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SCSynonymSets> iterator = _scSynonymSetses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSynonymSets scSynonymSets = _persistence.create(pk);

		Assert.assertNotNull(scSynonymSets);

		Assert.assertEquals(scSynonymSets.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SCSynonymSets newSCSynonymSets = addSCSynonymSets();

		_persistence.remove(newSCSynonymSets);

		SCSynonymSets existingSCSynonymSets = _persistence.fetchByPrimaryKey(
			newSCSynonymSets.getPrimaryKey());

		Assert.assertNull(existingSCSynonymSets);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSCSynonymSets();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSynonymSets newSCSynonymSets = _persistence.create(pk);

		newSCSynonymSets.setMvccVersion(RandomTestUtil.nextLong());

		newSCSynonymSets.setGroupId(RandomTestUtil.nextLong());

		newSCSynonymSets.setCompanyId(RandomTestUtil.nextLong());

		newSCSynonymSets.setUserId(RandomTestUtil.nextLong());

		newSCSynonymSets.setUserName(RandomTestUtil.randomString());

		newSCSynonymSets.setCreateDate(RandomTestUtil.nextDate());

		newSCSynonymSets.setModifiedDate(RandomTestUtil.nextDate());

		newSCSynonymSets.setSetId(RandomTestUtil.nextLong());

		newSCSynonymSets.setSynonymSets(RandomTestUtil.randomString());

		_scSynonymSetses.add(_persistence.update(newSCSynonymSets));

		SCSynonymSets existingSCSynonymSets = _persistence.findByPrimaryKey(
			newSCSynonymSets.getPrimaryKey());

		Assert.assertEquals(
			existingSCSynonymSets.getMvccVersion(),
			newSCSynonymSets.getMvccVersion());
		Assert.assertEquals(
			existingSCSynonymSets.getSynonymSetsId(),
			newSCSynonymSets.getSynonymSetsId());
		Assert.assertEquals(
			existingSCSynonymSets.getGroupId(), newSCSynonymSets.getGroupId());
		Assert.assertEquals(
			existingSCSynonymSets.getCompanyId(),
			newSCSynonymSets.getCompanyId());
		Assert.assertEquals(
			existingSCSynonymSets.getUserId(), newSCSynonymSets.getUserId());
		Assert.assertEquals(
			existingSCSynonymSets.getUserName(),
			newSCSynonymSets.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCSynonymSets.getCreateDate()),
			Time.getShortTimestamp(newSCSynonymSets.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCSynonymSets.getModifiedDate()),
			Time.getShortTimestamp(newSCSynonymSets.getModifiedDate()));
		Assert.assertEquals(
			existingSCSynonymSets.getSetId(), newSCSynonymSets.getSetId());
		Assert.assertEquals(
			existingSCSynonymSets.getSynonymSets(),
			newSCSynonymSets.getSynonymSets());
	}

	@Test
	public void testCountBySetId() throws Exception {
		_persistence.countBySetId(RandomTestUtil.nextLong());

		_persistence.countBySetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SCSynonymSets newSCSynonymSets = addSCSynonymSets();

		SCSynonymSets existingSCSynonymSets = _persistence.findByPrimaryKey(
			newSCSynonymSets.getPrimaryKey());

		Assert.assertEquals(existingSCSynonymSets, newSCSynonymSets);
	}

	@Test(expected = NoSuchSynonymSetsException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SCSynonymSets> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SCSynonymSets", "mvccVersion", true, "synonymSetsId", true,
			"groupId", true, "companyId", true, "userId", true, "userName",
			true, "createDate", true, "modifiedDate", true, "setId", true,
			"synonymSets", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SCSynonymSets newSCSynonymSets = addSCSynonymSets();

		SCSynonymSets existingSCSynonymSets = _persistence.fetchByPrimaryKey(
			newSCSynonymSets.getPrimaryKey());

		Assert.assertEquals(existingSCSynonymSets, newSCSynonymSets);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSynonymSets missingSCSynonymSets = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSCSynonymSets);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SCSynonymSets newSCSynonymSets1 = addSCSynonymSets();
		SCSynonymSets newSCSynonymSets2 = addSCSynonymSets();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSynonymSets1.getPrimaryKey());
		primaryKeys.add(newSCSynonymSets2.getPrimaryKey());

		Map<Serializable, SCSynonymSets> scSynonymSetses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, scSynonymSetses.size());
		Assert.assertEquals(
			newSCSynonymSets1,
			scSynonymSetses.get(newSCSynonymSets1.getPrimaryKey()));
		Assert.assertEquals(
			newSCSynonymSets2,
			scSynonymSetses.get(newSCSynonymSets2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SCSynonymSets> scSynonymSetses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scSynonymSetses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SCSynonymSets newSCSynonymSets = addSCSynonymSets();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSynonymSets.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SCSynonymSets> scSynonymSetses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scSynonymSetses.size());
		Assert.assertEquals(
			newSCSynonymSets,
			scSynonymSetses.get(newSCSynonymSets.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SCSynonymSets> scSynonymSetses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scSynonymSetses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SCSynonymSets newSCSynonymSets = addSCSynonymSets();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSynonymSets.getPrimaryKey());

		Map<Serializable, SCSynonymSets> scSynonymSetses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scSynonymSetses.size());
		Assert.assertEquals(
			newSCSynonymSets,
			scSynonymSetses.get(newSCSynonymSets.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SCSynonymSetsLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SCSynonymSets>() {

				@Override
				public void performAction(SCSynonymSets scSynonymSets) {
					Assert.assertNotNull(scSynonymSets);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SCSynonymSets newSCSynonymSets = addSCSynonymSets();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSynonymSets.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"synonymSetsId", newSCSynonymSets.getSynonymSetsId()));

		List<SCSynonymSets> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SCSynonymSets existingSCSynonymSets = result.get(0);

		Assert.assertEquals(existingSCSynonymSets, newSCSynonymSets);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSynonymSets.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"synonymSetsId", RandomTestUtil.nextLong()));

		List<SCSynonymSets> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SCSynonymSets newSCSynonymSets = addSCSynonymSets();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSynonymSets.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("synonymSetsId"));

		Object newSynonymSetsId = newSCSynonymSets.getSynonymSetsId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"synonymSetsId", new Object[] {newSynonymSetsId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingSynonymSetsId = result.get(0);

		Assert.assertEquals(existingSynonymSetsId, newSynonymSetsId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSynonymSets.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("synonymSetsId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"synonymSetsId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SCSynonymSets addSCSynonymSets() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSynonymSets scSynonymSets = _persistence.create(pk);

		scSynonymSets.setMvccVersion(RandomTestUtil.nextLong());

		scSynonymSets.setGroupId(RandomTestUtil.nextLong());

		scSynonymSets.setCompanyId(RandomTestUtil.nextLong());

		scSynonymSets.setUserId(RandomTestUtil.nextLong());

		scSynonymSets.setUserName(RandomTestUtil.randomString());

		scSynonymSets.setCreateDate(RandomTestUtil.nextDate());

		scSynonymSets.setModifiedDate(RandomTestUtil.nextDate());

		scSynonymSets.setSetId(RandomTestUtil.nextLong());

		scSynonymSets.setSynonymSets(RandomTestUtil.randomString());

		_scSynonymSetses.add(_persistence.update(scSynonymSets));

		return scSynonymSets;
	}

	private List<SCSynonymSets> _scSynonymSetses =
		new ArrayList<SCSynonymSets>();
	private SCSynonymSetsPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}