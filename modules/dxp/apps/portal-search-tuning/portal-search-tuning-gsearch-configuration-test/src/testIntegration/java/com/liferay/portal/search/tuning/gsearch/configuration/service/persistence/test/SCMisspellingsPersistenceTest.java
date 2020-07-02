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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchMisspellingsException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SCMisspellings;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SCMisspellingsLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCMisspellingsPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCMisspellingsUtil;
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
public class SCMisspellingsPersistenceTest {

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
		_persistence = SCMisspellingsUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SCMisspellings> iterator = _scMisspellingses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCMisspellings scMisspellings = _persistence.create(pk);

		Assert.assertNotNull(scMisspellings);

		Assert.assertEquals(scMisspellings.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SCMisspellings newSCMisspellings = addSCMisspellings();

		_persistence.remove(newSCMisspellings);

		SCMisspellings existingSCMisspellings = _persistence.fetchByPrimaryKey(
			newSCMisspellings.getPrimaryKey());

		Assert.assertNull(existingSCMisspellings);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSCMisspellings();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCMisspellings newSCMisspellings = _persistence.create(pk);

		newSCMisspellings.setMvccVersion(RandomTestUtil.nextLong());

		newSCMisspellings.setGroupId(RandomTestUtil.nextLong());

		newSCMisspellings.setCompanyId(RandomTestUtil.nextLong());

		newSCMisspellings.setUserId(RandomTestUtil.nextLong());

		newSCMisspellings.setUserName(RandomTestUtil.randomString());

		newSCMisspellings.setCreateDate(RandomTestUtil.nextDate());

		newSCMisspellings.setModifiedDate(RandomTestUtil.nextDate());

		newSCMisspellings.setSetId(RandomTestUtil.nextLong());

		newSCMisspellings.setMisspellings(RandomTestUtil.randomString());

		_scMisspellingses.add(_persistence.update(newSCMisspellings));

		SCMisspellings existingSCMisspellings = _persistence.findByPrimaryKey(
			newSCMisspellings.getPrimaryKey());

		Assert.assertEquals(
			existingSCMisspellings.getMvccVersion(),
			newSCMisspellings.getMvccVersion());
		Assert.assertEquals(
			existingSCMisspellings.getMisspellingsId(),
			newSCMisspellings.getMisspellingsId());
		Assert.assertEquals(
			existingSCMisspellings.getGroupId(),
			newSCMisspellings.getGroupId());
		Assert.assertEquals(
			existingSCMisspellings.getCompanyId(),
			newSCMisspellings.getCompanyId());
		Assert.assertEquals(
			existingSCMisspellings.getUserId(), newSCMisspellings.getUserId());
		Assert.assertEquals(
			existingSCMisspellings.getUserName(),
			newSCMisspellings.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCMisspellings.getCreateDate()),
			Time.getShortTimestamp(newSCMisspellings.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCMisspellings.getModifiedDate()),
			Time.getShortTimestamp(newSCMisspellings.getModifiedDate()));
		Assert.assertEquals(
			existingSCMisspellings.getSetId(), newSCMisspellings.getSetId());
		Assert.assertEquals(
			existingSCMisspellings.getMisspellings(),
			newSCMisspellings.getMisspellings());
	}

	@Test
	public void testCountBySetId() throws Exception {
		_persistence.countBySetId(RandomTestUtil.nextLong());

		_persistence.countBySetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SCMisspellings newSCMisspellings = addSCMisspellings();

		SCMisspellings existingSCMisspellings = _persistence.findByPrimaryKey(
			newSCMisspellings.getPrimaryKey());

		Assert.assertEquals(existingSCMisspellings, newSCMisspellings);
	}

	@Test(expected = NoSuchMisspellingsException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SCMisspellings> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SCMisspellings", "mvccVersion", true, "misspellingsId", true,
			"groupId", true, "companyId", true, "userId", true, "userName",
			true, "createDate", true, "modifiedDate", true, "setId", true,
			"misspellings", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SCMisspellings newSCMisspellings = addSCMisspellings();

		SCMisspellings existingSCMisspellings = _persistence.fetchByPrimaryKey(
			newSCMisspellings.getPrimaryKey());

		Assert.assertEquals(existingSCMisspellings, newSCMisspellings);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCMisspellings missingSCMisspellings = _persistence.fetchByPrimaryKey(
			pk);

		Assert.assertNull(missingSCMisspellings);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SCMisspellings newSCMisspellings1 = addSCMisspellings();
		SCMisspellings newSCMisspellings2 = addSCMisspellings();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCMisspellings1.getPrimaryKey());
		primaryKeys.add(newSCMisspellings2.getPrimaryKey());

		Map<Serializable, SCMisspellings> scMisspellingses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, scMisspellingses.size());
		Assert.assertEquals(
			newSCMisspellings1,
			scMisspellingses.get(newSCMisspellings1.getPrimaryKey()));
		Assert.assertEquals(
			newSCMisspellings2,
			scMisspellingses.get(newSCMisspellings2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SCMisspellings> scMisspellingses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scMisspellingses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SCMisspellings newSCMisspellings = addSCMisspellings();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCMisspellings.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SCMisspellings> scMisspellingses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scMisspellingses.size());
		Assert.assertEquals(
			newSCMisspellings,
			scMisspellingses.get(newSCMisspellings.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SCMisspellings> scMisspellingses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scMisspellingses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SCMisspellings newSCMisspellings = addSCMisspellings();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCMisspellings.getPrimaryKey());

		Map<Serializable, SCMisspellings> scMisspellingses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scMisspellingses.size());
		Assert.assertEquals(
			newSCMisspellings,
			scMisspellingses.get(newSCMisspellings.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SCMisspellingsLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SCMisspellings>() {

				@Override
				public void performAction(SCMisspellings scMisspellings) {
					Assert.assertNotNull(scMisspellings);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SCMisspellings newSCMisspellings = addSCMisspellings();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCMisspellings.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"misspellingsId", newSCMisspellings.getMisspellingsId()));

		List<SCMisspellings> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SCMisspellings existingSCMisspellings = result.get(0);

		Assert.assertEquals(existingSCMisspellings, newSCMisspellings);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCMisspellings.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"misspellingsId", RandomTestUtil.nextLong()));

		List<SCMisspellings> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SCMisspellings newSCMisspellings = addSCMisspellings();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCMisspellings.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("misspellingsId"));

		Object newMisspellingsId = newSCMisspellings.getMisspellingsId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"misspellingsId", new Object[] {newMisspellingsId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingMisspellingsId = result.get(0);

		Assert.assertEquals(existingMisspellingsId, newMisspellingsId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCMisspellings.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("misspellingsId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"misspellingsId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SCMisspellings addSCMisspellings() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCMisspellings scMisspellings = _persistence.create(pk);

		scMisspellings.setMvccVersion(RandomTestUtil.nextLong());

		scMisspellings.setGroupId(RandomTestUtil.nextLong());

		scMisspellings.setCompanyId(RandomTestUtil.nextLong());

		scMisspellings.setUserId(RandomTestUtil.nextLong());

		scMisspellings.setUserName(RandomTestUtil.randomString());

		scMisspellings.setCreateDate(RandomTestUtil.nextDate());

		scMisspellings.setModifiedDate(RandomTestUtil.nextDate());

		scMisspellings.setSetId(RandomTestUtil.nextLong());

		scMisspellings.setMisspellings(RandomTestUtil.randomString());

		_scMisspellingses.add(_persistence.update(scMisspellings));

		return scMisspellings;
	}

	private List<SCMisspellings> _scMisspellingses =
		new ArrayList<SCMisspellings>();
	private SCMisspellingsPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}