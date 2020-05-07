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
import com.liferay.portal.search.tuning.gsearch.configuration.model.exception.NoSuchRescoreClauseException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.model.RescoreClause;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.RescoreClauseLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.RescoreClausePersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.RescoreClauseUtil;
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
public class RescoreClausePersistenceTest {

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
		_persistence = RescoreClauseUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<RescoreClause> iterator = _rescoreClauses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		RescoreClause rescoreClause = _persistence.create(pk);

		Assert.assertNotNull(rescoreClause);

		Assert.assertEquals(rescoreClause.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		RescoreClause newRescoreClause = addRescoreClause();

		_persistence.remove(newRescoreClause);

		RescoreClause existingRescoreClause = _persistence.fetchByPrimaryKey(
			newRescoreClause.getPrimaryKey());

		Assert.assertNull(existingRescoreClause);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addRescoreClause();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		RescoreClause newRescoreClause = _persistence.create(pk);

		newRescoreClause.setGroupId(RandomTestUtil.nextLong());

		newRescoreClause.setCompanyId(RandomTestUtil.nextLong());

		newRescoreClause.setUserId(RandomTestUtil.nextLong());

		newRescoreClause.setUserName(RandomTestUtil.randomString());

		newRescoreClause.setCreateDate(RandomTestUtil.nextDate());

		newRescoreClause.setModifiedDate(RandomTestUtil.nextDate());

		newRescoreClause.setConfigurationSetId(RandomTestUtil.nextLong());

		newRescoreClause.setRescoreClause(RandomTestUtil.randomString());

		_rescoreClauses.add(_persistence.update(newRescoreClause));

		RescoreClause existingRescoreClause = _persistence.findByPrimaryKey(
			newRescoreClause.getPrimaryKey());

		Assert.assertEquals(
			existingRescoreClause.getRescoreClauseId(),
			newRescoreClause.getRescoreClauseId());
		Assert.assertEquals(
			existingRescoreClause.getGroupId(), newRescoreClause.getGroupId());
		Assert.assertEquals(
			existingRescoreClause.getCompanyId(),
			newRescoreClause.getCompanyId());
		Assert.assertEquals(
			existingRescoreClause.getUserId(), newRescoreClause.getUserId());
		Assert.assertEquals(
			existingRescoreClause.getUserName(),
			newRescoreClause.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingRescoreClause.getCreateDate()),
			Time.getShortTimestamp(newRescoreClause.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingRescoreClause.getModifiedDate()),
			Time.getShortTimestamp(newRescoreClause.getModifiedDate()));
		Assert.assertEquals(
			existingRescoreClause.getConfigurationSetId(),
			newRescoreClause.getConfigurationSetId());
		Assert.assertEquals(
			existingRescoreClause.getRescoreClause(),
			newRescoreClause.getRescoreClause());
	}

	@Test
	public void testCountByConfigurationSetId() throws Exception {
		_persistence.countByConfigurationSetId(RandomTestUtil.nextLong());

		_persistence.countByConfigurationSetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		RescoreClause newRescoreClause = addRescoreClause();

		RescoreClause existingRescoreClause = _persistence.findByPrimaryKey(
			newRescoreClause.getPrimaryKey());

		Assert.assertEquals(existingRescoreClause, newRescoreClause);
	}

	@Test(expected = NoSuchRescoreClauseException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<RescoreClause> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"RescoreClause", "rescoreClauseId", true, "groupId", true,
			"companyId", true, "userId", true, "userName", true, "createDate",
			true, "modifiedDate", true, "configurationSetId", true,
			"rescoreClause", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		RescoreClause newRescoreClause = addRescoreClause();

		RescoreClause existingRescoreClause = _persistence.fetchByPrimaryKey(
			newRescoreClause.getPrimaryKey());

		Assert.assertEquals(existingRescoreClause, newRescoreClause);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		RescoreClause missingRescoreClause = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingRescoreClause);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		RescoreClause newRescoreClause1 = addRescoreClause();
		RescoreClause newRescoreClause2 = addRescoreClause();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newRescoreClause1.getPrimaryKey());
		primaryKeys.add(newRescoreClause2.getPrimaryKey());

		Map<Serializable, RescoreClause> rescoreClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, rescoreClauses.size());
		Assert.assertEquals(
			newRescoreClause1,
			rescoreClauses.get(newRescoreClause1.getPrimaryKey()));
		Assert.assertEquals(
			newRescoreClause2,
			rescoreClauses.get(newRescoreClause2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, RescoreClause> rescoreClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(rescoreClauses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		RescoreClause newRescoreClause = addRescoreClause();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newRescoreClause.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, RescoreClause> rescoreClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, rescoreClauses.size());
		Assert.assertEquals(
			newRescoreClause,
			rescoreClauses.get(newRescoreClause.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, RescoreClause> rescoreClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(rescoreClauses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		RescoreClause newRescoreClause = addRescoreClause();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newRescoreClause.getPrimaryKey());

		Map<Serializable, RescoreClause> rescoreClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, rescoreClauses.size());
		Assert.assertEquals(
			newRescoreClause,
			rescoreClauses.get(newRescoreClause.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			RescoreClauseLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<RescoreClause>() {

				@Override
				public void performAction(RescoreClause rescoreClause) {
					Assert.assertNotNull(rescoreClause);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		RescoreClause newRescoreClause = addRescoreClause();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			RescoreClause.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"rescoreClauseId", newRescoreClause.getRescoreClauseId()));

		List<RescoreClause> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		RescoreClause existingRescoreClause = result.get(0);

		Assert.assertEquals(existingRescoreClause, newRescoreClause);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			RescoreClause.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"rescoreClauseId", RandomTestUtil.nextLong()));

		List<RescoreClause> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		RescoreClause newRescoreClause = addRescoreClause();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			RescoreClause.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("rescoreClauseId"));

		Object newRescoreClauseId = newRescoreClause.getRescoreClauseId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"rescoreClauseId", new Object[] {newRescoreClauseId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingRescoreClauseId = result.get(0);

		Assert.assertEquals(existingRescoreClauseId, newRescoreClauseId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			RescoreClause.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("rescoreClauseId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"rescoreClauseId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected RescoreClause addRescoreClause() throws Exception {
		long pk = RandomTestUtil.nextLong();

		RescoreClause rescoreClause = _persistence.create(pk);

		rescoreClause.setGroupId(RandomTestUtil.nextLong());

		rescoreClause.setCompanyId(RandomTestUtil.nextLong());

		rescoreClause.setUserId(RandomTestUtil.nextLong());

		rescoreClause.setUserName(RandomTestUtil.randomString());

		rescoreClause.setCreateDate(RandomTestUtil.nextDate());

		rescoreClause.setModifiedDate(RandomTestUtil.nextDate());

		rescoreClause.setConfigurationSetId(RandomTestUtil.nextLong());

		rescoreClause.setRescoreClause(RandomTestUtil.randomString());

		_rescoreClauses.add(_persistence.update(rescoreClause));

		return rescoreClause;
	}

	private List<RescoreClause> _rescoreClauses =
		new ArrayList<RescoreClause>();
	private RescoreClausePersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}