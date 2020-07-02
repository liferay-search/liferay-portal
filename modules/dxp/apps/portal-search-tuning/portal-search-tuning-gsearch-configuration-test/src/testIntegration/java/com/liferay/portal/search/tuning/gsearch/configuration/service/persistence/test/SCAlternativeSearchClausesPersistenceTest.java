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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchAlternativeSearchClausesException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SCAlternativeSearchClauses;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SCAlternativeSearchClausesLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCAlternativeSearchClausesPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCAlternativeSearchClausesUtil;
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
public class SCAlternativeSearchClausesPersistenceTest {

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
		_persistence = SCAlternativeSearchClausesUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SCAlternativeSearchClauses> iterator =
			_scAlternativeSearchClauseses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCAlternativeSearchClauses scAlternativeSearchClauses =
			_persistence.create(pk);

		Assert.assertNotNull(scAlternativeSearchClauses);

		Assert.assertEquals(scAlternativeSearchClauses.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SCAlternativeSearchClauses newSCAlternativeSearchClauses =
			addSCAlternativeSearchClauses();

		_persistence.remove(newSCAlternativeSearchClauses);

		SCAlternativeSearchClauses existingSCAlternativeSearchClauses =
			_persistence.fetchByPrimaryKey(
				newSCAlternativeSearchClauses.getPrimaryKey());

		Assert.assertNull(existingSCAlternativeSearchClauses);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSCAlternativeSearchClauses();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCAlternativeSearchClauses newSCAlternativeSearchClauses =
			_persistence.create(pk);

		newSCAlternativeSearchClauses.setMvccVersion(RandomTestUtil.nextLong());

		newSCAlternativeSearchClauses.setGroupId(RandomTestUtil.nextLong());

		newSCAlternativeSearchClauses.setCompanyId(RandomTestUtil.nextLong());

		newSCAlternativeSearchClauses.setUserId(RandomTestUtil.nextLong());

		newSCAlternativeSearchClauses.setUserName(
			RandomTestUtil.randomString());

		newSCAlternativeSearchClauses.setCreateDate(RandomTestUtil.nextDate());

		newSCAlternativeSearchClauses.setModifiedDate(
			RandomTestUtil.nextDate());

		newSCAlternativeSearchClauses.setSetId(RandomTestUtil.nextLong());

		newSCAlternativeSearchClauses.setAlternativeSearchClauses(
			RandomTestUtil.randomString());

		_scAlternativeSearchClauseses.add(
			_persistence.update(newSCAlternativeSearchClauses));

		SCAlternativeSearchClauses existingSCAlternativeSearchClauses =
			_persistence.findByPrimaryKey(
				newSCAlternativeSearchClauses.getPrimaryKey());

		Assert.assertEquals(
			existingSCAlternativeSearchClauses.getMvccVersion(),
			newSCAlternativeSearchClauses.getMvccVersion());
		Assert.assertEquals(
			existingSCAlternativeSearchClauses.getAlternativeSearchClausesId(),
			newSCAlternativeSearchClauses.getAlternativeSearchClausesId());
		Assert.assertEquals(
			existingSCAlternativeSearchClauses.getGroupId(),
			newSCAlternativeSearchClauses.getGroupId());
		Assert.assertEquals(
			existingSCAlternativeSearchClauses.getCompanyId(),
			newSCAlternativeSearchClauses.getCompanyId());
		Assert.assertEquals(
			existingSCAlternativeSearchClauses.getUserId(),
			newSCAlternativeSearchClauses.getUserId());
		Assert.assertEquals(
			existingSCAlternativeSearchClauses.getUserName(),
			newSCAlternativeSearchClauses.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingSCAlternativeSearchClauses.getCreateDate()),
			Time.getShortTimestamp(
				newSCAlternativeSearchClauses.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingSCAlternativeSearchClauses.getModifiedDate()),
			Time.getShortTimestamp(
				newSCAlternativeSearchClauses.getModifiedDate()));
		Assert.assertEquals(
			existingSCAlternativeSearchClauses.getSetId(),
			newSCAlternativeSearchClauses.getSetId());
		Assert.assertEquals(
			existingSCAlternativeSearchClauses.getAlternativeSearchClauses(),
			newSCAlternativeSearchClauses.getAlternativeSearchClauses());
	}

	@Test
	public void testCountBySetId() throws Exception {
		_persistence.countBySetId(RandomTestUtil.nextLong());

		_persistence.countBySetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SCAlternativeSearchClauses newSCAlternativeSearchClauses =
			addSCAlternativeSearchClauses();

		SCAlternativeSearchClauses existingSCAlternativeSearchClauses =
			_persistence.findByPrimaryKey(
				newSCAlternativeSearchClauses.getPrimaryKey());

		Assert.assertEquals(
			existingSCAlternativeSearchClauses, newSCAlternativeSearchClauses);
	}

	@Test(expected = NoSuchAlternativeSearchClausesException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SCAlternativeSearchClauses>
		getOrderByComparator() {

		return OrderByComparatorFactoryUtil.create(
			"SCAlternativeSearchClauses", "mvccVersion", true,
			"alternativeSearchClausesId", true, "groupId", true, "companyId",
			true, "userId", true, "userName", true, "createDate", true,
			"modifiedDate", true, "setId", true, "alternativeSearchClauses",
			true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SCAlternativeSearchClauses newSCAlternativeSearchClauses =
			addSCAlternativeSearchClauses();

		SCAlternativeSearchClauses existingSCAlternativeSearchClauses =
			_persistence.fetchByPrimaryKey(
				newSCAlternativeSearchClauses.getPrimaryKey());

		Assert.assertEquals(
			existingSCAlternativeSearchClauses, newSCAlternativeSearchClauses);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCAlternativeSearchClauses missingSCAlternativeSearchClauses =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSCAlternativeSearchClauses);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SCAlternativeSearchClauses newSCAlternativeSearchClauses1 =
			addSCAlternativeSearchClauses();
		SCAlternativeSearchClauses newSCAlternativeSearchClauses2 =
			addSCAlternativeSearchClauses();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCAlternativeSearchClauses1.getPrimaryKey());
		primaryKeys.add(newSCAlternativeSearchClauses2.getPrimaryKey());

		Map<Serializable, SCAlternativeSearchClauses>
			scAlternativeSearchClauseses = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(2, scAlternativeSearchClauseses.size());
		Assert.assertEquals(
			newSCAlternativeSearchClauses1,
			scAlternativeSearchClauseses.get(
				newSCAlternativeSearchClauses1.getPrimaryKey()));
		Assert.assertEquals(
			newSCAlternativeSearchClauses2,
			scAlternativeSearchClauseses.get(
				newSCAlternativeSearchClauses2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SCAlternativeSearchClauses>
			scAlternativeSearchClauseses = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(scAlternativeSearchClauseses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SCAlternativeSearchClauses newSCAlternativeSearchClauses =
			addSCAlternativeSearchClauses();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCAlternativeSearchClauses.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SCAlternativeSearchClauses>
			scAlternativeSearchClauseses = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, scAlternativeSearchClauseses.size());
		Assert.assertEquals(
			newSCAlternativeSearchClauses,
			scAlternativeSearchClauseses.get(
				newSCAlternativeSearchClauses.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SCAlternativeSearchClauses>
			scAlternativeSearchClauseses = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(scAlternativeSearchClauseses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SCAlternativeSearchClauses newSCAlternativeSearchClauses =
			addSCAlternativeSearchClauses();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCAlternativeSearchClauses.getPrimaryKey());

		Map<Serializable, SCAlternativeSearchClauses>
			scAlternativeSearchClauseses = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, scAlternativeSearchClauseses.size());
		Assert.assertEquals(
			newSCAlternativeSearchClauses,
			scAlternativeSearchClauseses.get(
				newSCAlternativeSearchClauses.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SCAlternativeSearchClausesLocalServiceUtil.
				getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod
				<SCAlternativeSearchClauses>() {

				@Override
				public void performAction(
					SCAlternativeSearchClauses scAlternativeSearchClauses) {

					Assert.assertNotNull(scAlternativeSearchClauses);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SCAlternativeSearchClauses newSCAlternativeSearchClauses =
			addSCAlternativeSearchClauses();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCAlternativeSearchClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"alternativeSearchClausesId",
				newSCAlternativeSearchClauses.getAlternativeSearchClausesId()));

		List<SCAlternativeSearchClauses> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		SCAlternativeSearchClauses existingSCAlternativeSearchClauses =
			result.get(0);

		Assert.assertEquals(
			existingSCAlternativeSearchClauses, newSCAlternativeSearchClauses);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCAlternativeSearchClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"alternativeSearchClausesId", RandomTestUtil.nextLong()));

		List<SCAlternativeSearchClauses> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SCAlternativeSearchClauses newSCAlternativeSearchClauses =
			addSCAlternativeSearchClauses();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCAlternativeSearchClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("alternativeSearchClausesId"));

		Object newAlternativeSearchClausesId =
			newSCAlternativeSearchClauses.getAlternativeSearchClausesId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"alternativeSearchClausesId",
				new Object[] {newAlternativeSearchClausesId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingAlternativeSearchClausesId = result.get(0);

		Assert.assertEquals(
			existingAlternativeSearchClausesId, newAlternativeSearchClausesId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCAlternativeSearchClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("alternativeSearchClausesId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"alternativeSearchClausesId",
				new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SCAlternativeSearchClauses addSCAlternativeSearchClauses()
		throws Exception {

		long pk = RandomTestUtil.nextLong();

		SCAlternativeSearchClauses scAlternativeSearchClauses =
			_persistence.create(pk);

		scAlternativeSearchClauses.setMvccVersion(RandomTestUtil.nextLong());

		scAlternativeSearchClauses.setGroupId(RandomTestUtil.nextLong());

		scAlternativeSearchClauses.setCompanyId(RandomTestUtil.nextLong());

		scAlternativeSearchClauses.setUserId(RandomTestUtil.nextLong());

		scAlternativeSearchClauses.setUserName(RandomTestUtil.randomString());

		scAlternativeSearchClauses.setCreateDate(RandomTestUtil.nextDate());

		scAlternativeSearchClauses.setModifiedDate(RandomTestUtil.nextDate());

		scAlternativeSearchClauses.setSetId(RandomTestUtil.nextLong());

		scAlternativeSearchClauses.setAlternativeSearchClauses(
			RandomTestUtil.randomString());

		_scAlternativeSearchClauseses.add(
			_persistence.update(scAlternativeSearchClauses));

		return scAlternativeSearchClauses;
	}

	private List<SCAlternativeSearchClauses> _scAlternativeSearchClauseses =
		new ArrayList<SCAlternativeSearchClauses>();
	private SCAlternativeSearchClausesPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}