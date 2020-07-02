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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchSetException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SCSet;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SCSetLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCSetPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCSetUtil;
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
public class SCSetPersistenceTest {

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
		_persistence = SCSetUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SCSet> iterator = _scSets.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSet scSet = _persistence.create(pk);

		Assert.assertNotNull(scSet);

		Assert.assertEquals(scSet.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SCSet newSCSet = addSCSet();

		_persistence.remove(newSCSet);

		SCSet existingSCSet = _persistence.fetchByPrimaryKey(
			newSCSet.getPrimaryKey());

		Assert.assertNull(existingSCSet);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSCSet();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSet newSCSet = _persistence.create(pk);

		newSCSet.setMvccVersion(RandomTestUtil.nextLong());

		newSCSet.setUuid(RandomTestUtil.randomString());

		newSCSet.setGroupId(RandomTestUtil.nextLong());

		newSCSet.setCompanyId(RandomTestUtil.nextLong());

		newSCSet.setUserId(RandomTestUtil.nextLong());

		newSCSet.setUserName(RandomTestUtil.randomString());

		newSCSet.setCreateDate(RandomTestUtil.nextDate());

		newSCSet.setModifiedDate(RandomTestUtil.nextDate());

		newSCSet.setName(RandomTestUtil.randomString());

		newSCSet.setDescription(RandomTestUtil.randomString());

		_scSets.add(_persistence.update(newSCSet));

		SCSet existingSCSet = _persistence.findByPrimaryKey(
			newSCSet.getPrimaryKey());

		Assert.assertEquals(
			existingSCSet.getMvccVersion(), newSCSet.getMvccVersion());
		Assert.assertEquals(existingSCSet.getUuid(), newSCSet.getUuid());
		Assert.assertEquals(existingSCSet.getSetId(), newSCSet.getSetId());
		Assert.assertEquals(existingSCSet.getGroupId(), newSCSet.getGroupId());
		Assert.assertEquals(
			existingSCSet.getCompanyId(), newSCSet.getCompanyId());
		Assert.assertEquals(existingSCSet.getUserId(), newSCSet.getUserId());
		Assert.assertEquals(
			existingSCSet.getUserName(), newSCSet.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCSet.getCreateDate()),
			Time.getShortTimestamp(newSCSet.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCSet.getModifiedDate()),
			Time.getShortTimestamp(newSCSet.getModifiedDate()));
		Assert.assertEquals(existingSCSet.getName(), newSCSet.getName());
		Assert.assertEquals(
			existingSCSet.getDescription(), newSCSet.getDescription());
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
	public void testCountByCompanyId() throws Exception {
		_persistence.countByCompanyId(RandomTestUtil.nextLong());

		_persistence.countByCompanyId(0L);
	}

	@Test
	public void testCountByGroupId() throws Exception {
		_persistence.countByGroupId(RandomTestUtil.nextLong());

		_persistence.countByGroupId(0L);
	}

	@Test
	public void testCountByC_N() throws Exception {
		_persistence.countByC_N(RandomTestUtil.nextLong(), "");

		_persistence.countByC_N(0L, "null");

		_persistence.countByC_N(0L, (String)null);
	}

	@Test
	public void testCountByG_N() throws Exception {
		_persistence.countByG_N(RandomTestUtil.nextLong(), "");

		_persistence.countByG_N(0L, "null");

		_persistence.countByG_N(0L, (String)null);
	}

	@Test
	public void testCountByG_U() throws Exception {
		_persistence.countByG_U(
			RandomTestUtil.nextLong(), RandomTestUtil.nextLong());

		_persistence.countByG_U(0L, 0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SCSet newSCSet = addSCSet();

		SCSet existingSCSet = _persistence.findByPrimaryKey(
			newSCSet.getPrimaryKey());

		Assert.assertEquals(existingSCSet, newSCSet);
	}

	@Test(expected = NoSuchSetException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SCSet> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SCSet", "mvccVersion", true, "uuid", true, "setId", true,
			"groupId", true, "companyId", true, "userId", true, "userName",
			true, "createDate", true, "modifiedDate", true, "name", true,
			"description", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SCSet newSCSet = addSCSet();

		SCSet existingSCSet = _persistence.fetchByPrimaryKey(
			newSCSet.getPrimaryKey());

		Assert.assertEquals(existingSCSet, newSCSet);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSet missingSCSet = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSCSet);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SCSet newSCSet1 = addSCSet();
		SCSet newSCSet2 = addSCSet();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSet1.getPrimaryKey());
		primaryKeys.add(newSCSet2.getPrimaryKey());

		Map<Serializable, SCSet> scSets = _persistence.fetchByPrimaryKeys(
			primaryKeys);

		Assert.assertEquals(2, scSets.size());
		Assert.assertEquals(newSCSet1, scSets.get(newSCSet1.getPrimaryKey()));
		Assert.assertEquals(newSCSet2, scSets.get(newSCSet2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SCSet> scSets = _persistence.fetchByPrimaryKeys(
			primaryKeys);

		Assert.assertTrue(scSets.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SCSet newSCSet = addSCSet();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSet.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SCSet> scSets = _persistence.fetchByPrimaryKeys(
			primaryKeys);

		Assert.assertEquals(1, scSets.size());
		Assert.assertEquals(newSCSet, scSets.get(newSCSet.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SCSet> scSets = _persistence.fetchByPrimaryKeys(
			primaryKeys);

		Assert.assertTrue(scSets.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SCSet newSCSet = addSCSet();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSet.getPrimaryKey());

		Map<Serializable, SCSet> scSets = _persistence.fetchByPrimaryKeys(
			primaryKeys);

		Assert.assertEquals(1, scSets.size());
		Assert.assertEquals(newSCSet, scSets.get(newSCSet.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SCSetLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SCSet>() {

				@Override
				public void performAction(SCSet scSet) {
					Assert.assertNotNull(scSet);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SCSet newSCSet = addSCSet();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSet.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("setId", newSCSet.getSetId()));

		List<SCSet> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		SCSet existingSCSet = result.get(0);

		Assert.assertEquals(existingSCSet, newSCSet);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSet.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("setId", RandomTestUtil.nextLong()));

		List<SCSet> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SCSet newSCSet = addSCSet();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSet.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(ProjectionFactoryUtil.property("setId"));

		Object newSetId = newSCSet.getSetId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in("setId", new Object[] {newSetId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingSetId = result.get(0);

		Assert.assertEquals(existingSetId, newSetId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSet.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(ProjectionFactoryUtil.property("setId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"setId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testResetOriginalValues() throws Exception {
		SCSet newSCSet = addSCSet();

		_persistence.clearCache();

		SCSet existingSCSet = _persistence.findByPrimaryKey(
			newSCSet.getPrimaryKey());

		Assert.assertTrue(
			Objects.equals(
				existingSCSet.getUuid(),
				ReflectionTestUtil.invoke(
					existingSCSet, "getOriginalUuid", new Class<?>[0])));
		Assert.assertEquals(
			Long.valueOf(existingSCSet.getGroupId()),
			ReflectionTestUtil.<Long>invoke(
				existingSCSet, "getOriginalGroupId", new Class<?>[0]));

		Assert.assertEquals(
			Long.valueOf(existingSCSet.getGroupId()),
			ReflectionTestUtil.<Long>invoke(
				existingSCSet, "getOriginalGroupId", new Class<?>[0]));
		Assert.assertTrue(
			Objects.equals(
				existingSCSet.getName(),
				ReflectionTestUtil.invoke(
					existingSCSet, "getOriginalName", new Class<?>[0])));
	}

	protected SCSet addSCSet() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSet scSet = _persistence.create(pk);

		scSet.setMvccVersion(RandomTestUtil.nextLong());

		scSet.setUuid(RandomTestUtil.randomString());

		scSet.setGroupId(RandomTestUtil.nextLong());

		scSet.setCompanyId(RandomTestUtil.nextLong());

		scSet.setUserId(RandomTestUtil.nextLong());

		scSet.setUserName(RandomTestUtil.randomString());

		scSet.setCreateDate(RandomTestUtil.nextDate());

		scSet.setModifiedDate(RandomTestUtil.nextDate());

		scSet.setName(RandomTestUtil.randomString());

		scSet.setDescription(RandomTestUtil.randomString());

		_scSets.add(_persistence.update(scSet));

		return scSet;
	}

	private List<SCSet> _scSets = new ArrayList<SCSet>();
	private SCSetPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}