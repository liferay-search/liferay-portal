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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchRescoreClausesException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SCRescoreClauses;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SCRescoreClausesLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCRescoreClausesPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCRescoreClausesUtil;
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
public class SCRescoreClausesPersistenceTest {

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
		_persistence = SCRescoreClausesUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SCRescoreClauses> iterator = _scRescoreClauseses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCRescoreClauses scRescoreClauses = _persistence.create(pk);

		Assert.assertNotNull(scRescoreClauses);

		Assert.assertEquals(scRescoreClauses.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SCRescoreClauses newSCRescoreClauses = addSCRescoreClauses();

		_persistence.remove(newSCRescoreClauses);

		SCRescoreClauses existingSCRescoreClauses =
			_persistence.fetchByPrimaryKey(newSCRescoreClauses.getPrimaryKey());

		Assert.assertNull(existingSCRescoreClauses);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSCRescoreClauses();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCRescoreClauses newSCRescoreClauses = _persistence.create(pk);

		newSCRescoreClauses.setMvccVersion(RandomTestUtil.nextLong());

		newSCRescoreClauses.setGroupId(RandomTestUtil.nextLong());

		newSCRescoreClauses.setCompanyId(RandomTestUtil.nextLong());

		newSCRescoreClauses.setUserId(RandomTestUtil.nextLong());

		newSCRescoreClauses.setUserName(RandomTestUtil.randomString());

		newSCRescoreClauses.setCreateDate(RandomTestUtil.nextDate());

		newSCRescoreClauses.setModifiedDate(RandomTestUtil.nextDate());

		newSCRescoreClauses.setSetId(RandomTestUtil.nextLong());

		newSCRescoreClauses.setRescoreClauses(RandomTestUtil.randomString());

		_scRescoreClauseses.add(_persistence.update(newSCRescoreClauses));

		SCRescoreClauses existingSCRescoreClauses =
			_persistence.findByPrimaryKey(newSCRescoreClauses.getPrimaryKey());

		Assert.assertEquals(
			existingSCRescoreClauses.getMvccVersion(),
			newSCRescoreClauses.getMvccVersion());
		Assert.assertEquals(
			existingSCRescoreClauses.getRescoreClausesId(),
			newSCRescoreClauses.getRescoreClausesId());
		Assert.assertEquals(
			existingSCRescoreClauses.getGroupId(),
			newSCRescoreClauses.getGroupId());
		Assert.assertEquals(
			existingSCRescoreClauses.getCompanyId(),
			newSCRescoreClauses.getCompanyId());
		Assert.assertEquals(
			existingSCRescoreClauses.getUserId(),
			newSCRescoreClauses.getUserId());
		Assert.assertEquals(
			existingSCRescoreClauses.getUserName(),
			newSCRescoreClauses.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCRescoreClauses.getCreateDate()),
			Time.getShortTimestamp(newSCRescoreClauses.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCRescoreClauses.getModifiedDate()),
			Time.getShortTimestamp(newSCRescoreClauses.getModifiedDate()));
		Assert.assertEquals(
			existingSCRescoreClauses.getSetId(),
			newSCRescoreClauses.getSetId());
		Assert.assertEquals(
			existingSCRescoreClauses.getRescoreClauses(),
			newSCRescoreClauses.getRescoreClauses());
	}

	@Test
	public void testCountBySetId() throws Exception {
		_persistence.countBySetId(RandomTestUtil.nextLong());

		_persistence.countBySetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SCRescoreClauses newSCRescoreClauses = addSCRescoreClauses();

		SCRescoreClauses existingSCRescoreClauses =
			_persistence.findByPrimaryKey(newSCRescoreClauses.getPrimaryKey());

		Assert.assertEquals(existingSCRescoreClauses, newSCRescoreClauses);
	}

	@Test(expected = NoSuchRescoreClausesException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SCRescoreClauses> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SCRescoreClauses", "mvccVersion", true, "rescoreClausesId", true,
			"groupId", true, "companyId", true, "userId", true, "userName",
			true, "createDate", true, "modifiedDate", true, "setId", true,
			"rescoreClauses", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SCRescoreClauses newSCRescoreClauses = addSCRescoreClauses();

		SCRescoreClauses existingSCRescoreClauses =
			_persistence.fetchByPrimaryKey(newSCRescoreClauses.getPrimaryKey());

		Assert.assertEquals(existingSCRescoreClauses, newSCRescoreClauses);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCRescoreClauses missingSCRescoreClauses =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSCRescoreClauses);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SCRescoreClauses newSCRescoreClauses1 = addSCRescoreClauses();
		SCRescoreClauses newSCRescoreClauses2 = addSCRescoreClauses();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCRescoreClauses1.getPrimaryKey());
		primaryKeys.add(newSCRescoreClauses2.getPrimaryKey());

		Map<Serializable, SCRescoreClauses> scRescoreClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, scRescoreClauseses.size());
		Assert.assertEquals(
			newSCRescoreClauses1,
			scRescoreClauseses.get(newSCRescoreClauses1.getPrimaryKey()));
		Assert.assertEquals(
			newSCRescoreClauses2,
			scRescoreClauseses.get(newSCRescoreClauses2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SCRescoreClauses> scRescoreClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scRescoreClauseses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SCRescoreClauses newSCRescoreClauses = addSCRescoreClauses();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCRescoreClauses.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SCRescoreClauses> scRescoreClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scRescoreClauseses.size());
		Assert.assertEquals(
			newSCRescoreClauses,
			scRescoreClauseses.get(newSCRescoreClauses.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SCRescoreClauses> scRescoreClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scRescoreClauseses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SCRescoreClauses newSCRescoreClauses = addSCRescoreClauses();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCRescoreClauses.getPrimaryKey());

		Map<Serializable, SCRescoreClauses> scRescoreClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scRescoreClauseses.size());
		Assert.assertEquals(
			newSCRescoreClauses,
			scRescoreClauseses.get(newSCRescoreClauses.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SCRescoreClausesLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SCRescoreClauses>() {

				@Override
				public void performAction(SCRescoreClauses scRescoreClauses) {
					Assert.assertNotNull(scRescoreClauses);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SCRescoreClauses newSCRescoreClauses = addSCRescoreClauses();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCRescoreClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"rescoreClausesId", newSCRescoreClauses.getRescoreClausesId()));

		List<SCRescoreClauses> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SCRescoreClauses existingSCRescoreClauses = result.get(0);

		Assert.assertEquals(existingSCRescoreClauses, newSCRescoreClauses);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCRescoreClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"rescoreClausesId", RandomTestUtil.nextLong()));

		List<SCRescoreClauses> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SCRescoreClauses newSCRescoreClauses = addSCRescoreClauses();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCRescoreClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("rescoreClausesId"));

		Object newRescoreClausesId = newSCRescoreClauses.getRescoreClausesId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"rescoreClausesId", new Object[] {newRescoreClausesId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingRescoreClausesId = result.get(0);

		Assert.assertEquals(existingRescoreClausesId, newRescoreClausesId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCRescoreClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("rescoreClausesId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"rescoreClausesId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SCRescoreClauses addSCRescoreClauses() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCRescoreClauses scRescoreClauses = _persistence.create(pk);

		scRescoreClauses.setMvccVersion(RandomTestUtil.nextLong());

		scRescoreClauses.setGroupId(RandomTestUtil.nextLong());

		scRescoreClauses.setCompanyId(RandomTestUtil.nextLong());

		scRescoreClauses.setUserId(RandomTestUtil.nextLong());

		scRescoreClauses.setUserName(RandomTestUtil.randomString());

		scRescoreClauses.setCreateDate(RandomTestUtil.nextDate());

		scRescoreClauses.setModifiedDate(RandomTestUtil.nextDate());

		scRescoreClauses.setSetId(RandomTestUtil.nextLong());

		scRescoreClauses.setRescoreClauses(RandomTestUtil.randomString());

		_scRescoreClauseses.add(_persistence.update(scRescoreClauses));

		return scRescoreClauses;
	}

	private List<SCRescoreClauses> _scRescoreClauseses =
		new ArrayList<SCRescoreClauses>();
	private SCRescoreClausesPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}