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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchSearchClausesException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SCSearchClauses;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SCSearchClausesLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCSearchClausesPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCSearchClausesUtil;
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
public class SCSearchClausesPersistenceTest {

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
		_persistence = SCSearchClausesUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SCSearchClauses> iterator = _scSearchClauseses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSearchClauses scSearchClauses = _persistence.create(pk);

		Assert.assertNotNull(scSearchClauses);

		Assert.assertEquals(scSearchClauses.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SCSearchClauses newSCSearchClauses = addSCSearchClauses();

		_persistence.remove(newSCSearchClauses);

		SCSearchClauses existingSCSearchClauses =
			_persistence.fetchByPrimaryKey(newSCSearchClauses.getPrimaryKey());

		Assert.assertNull(existingSCSearchClauses);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSCSearchClauses();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSearchClauses newSCSearchClauses = _persistence.create(pk);

		newSCSearchClauses.setMvccVersion(RandomTestUtil.nextLong());

		newSCSearchClauses.setGroupId(RandomTestUtil.nextLong());

		newSCSearchClauses.setCompanyId(RandomTestUtil.nextLong());

		newSCSearchClauses.setUserId(RandomTestUtil.nextLong());

		newSCSearchClauses.setUserName(RandomTestUtil.randomString());

		newSCSearchClauses.setCreateDate(RandomTestUtil.nextDate());

		newSCSearchClauses.setModifiedDate(RandomTestUtil.nextDate());

		newSCSearchClauses.setSetId(RandomTestUtil.nextLong());

		newSCSearchClauses.setSearchClauses(RandomTestUtil.randomString());

		_scSearchClauseses.add(_persistence.update(newSCSearchClauses));

		SCSearchClauses existingSCSearchClauses = _persistence.findByPrimaryKey(
			newSCSearchClauses.getPrimaryKey());

		Assert.assertEquals(
			existingSCSearchClauses.getMvccVersion(),
			newSCSearchClauses.getMvccVersion());
		Assert.assertEquals(
			existingSCSearchClauses.getSearchClausesId(),
			newSCSearchClauses.getSearchClausesId());
		Assert.assertEquals(
			existingSCSearchClauses.getGroupId(),
			newSCSearchClauses.getGroupId());
		Assert.assertEquals(
			existingSCSearchClauses.getCompanyId(),
			newSCSearchClauses.getCompanyId());
		Assert.assertEquals(
			existingSCSearchClauses.getUserId(),
			newSCSearchClauses.getUserId());
		Assert.assertEquals(
			existingSCSearchClauses.getUserName(),
			newSCSearchClauses.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCSearchClauses.getCreateDate()),
			Time.getShortTimestamp(newSCSearchClauses.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCSearchClauses.getModifiedDate()),
			Time.getShortTimestamp(newSCSearchClauses.getModifiedDate()));
		Assert.assertEquals(
			existingSCSearchClauses.getSetId(), newSCSearchClauses.getSetId());
		Assert.assertEquals(
			existingSCSearchClauses.getSearchClauses(),
			newSCSearchClauses.getSearchClauses());
	}

	@Test
	public void testCountBySetId() throws Exception {
		_persistence.countBySetId(RandomTestUtil.nextLong());

		_persistence.countBySetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SCSearchClauses newSCSearchClauses = addSCSearchClauses();

		SCSearchClauses existingSCSearchClauses = _persistence.findByPrimaryKey(
			newSCSearchClauses.getPrimaryKey());

		Assert.assertEquals(existingSCSearchClauses, newSCSearchClauses);
	}

	@Test(expected = NoSuchSearchClausesException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SCSearchClauses> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SCSearchClauses", "mvccVersion", true, "searchClausesId", true,
			"groupId", true, "companyId", true, "userId", true, "userName",
			true, "createDate", true, "modifiedDate", true, "setId", true,
			"searchClauses", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SCSearchClauses newSCSearchClauses = addSCSearchClauses();

		SCSearchClauses existingSCSearchClauses =
			_persistence.fetchByPrimaryKey(newSCSearchClauses.getPrimaryKey());

		Assert.assertEquals(existingSCSearchClauses, newSCSearchClauses);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSearchClauses missingSCSearchClauses = _persistence.fetchByPrimaryKey(
			pk);

		Assert.assertNull(missingSCSearchClauses);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SCSearchClauses newSCSearchClauses1 = addSCSearchClauses();
		SCSearchClauses newSCSearchClauses2 = addSCSearchClauses();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSearchClauses1.getPrimaryKey());
		primaryKeys.add(newSCSearchClauses2.getPrimaryKey());

		Map<Serializable, SCSearchClauses> scSearchClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, scSearchClauseses.size());
		Assert.assertEquals(
			newSCSearchClauses1,
			scSearchClauseses.get(newSCSearchClauses1.getPrimaryKey()));
		Assert.assertEquals(
			newSCSearchClauses2,
			scSearchClauseses.get(newSCSearchClauses2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SCSearchClauses> scSearchClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scSearchClauseses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SCSearchClauses newSCSearchClauses = addSCSearchClauses();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSearchClauses.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SCSearchClauses> scSearchClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scSearchClauseses.size());
		Assert.assertEquals(
			newSCSearchClauses,
			scSearchClauseses.get(newSCSearchClauses.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SCSearchClauses> scSearchClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scSearchClauseses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SCSearchClauses newSCSearchClauses = addSCSearchClauses();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSearchClauses.getPrimaryKey());

		Map<Serializable, SCSearchClauses> scSearchClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scSearchClauseses.size());
		Assert.assertEquals(
			newSCSearchClauses,
			scSearchClauseses.get(newSCSearchClauses.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SCSearchClausesLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SCSearchClauses>() {

				@Override
				public void performAction(SCSearchClauses scSearchClauses) {
					Assert.assertNotNull(scSearchClauses);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SCSearchClauses newSCSearchClauses = addSCSearchClauses();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSearchClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"searchClausesId", newSCSearchClauses.getSearchClausesId()));

		List<SCSearchClauses> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SCSearchClauses existingSCSearchClauses = result.get(0);

		Assert.assertEquals(existingSCSearchClauses, newSCSearchClauses);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSearchClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"searchClausesId", RandomTestUtil.nextLong()));

		List<SCSearchClauses> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SCSearchClauses newSCSearchClauses = addSCSearchClauses();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSearchClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("searchClausesId"));

		Object newSearchClausesId = newSCSearchClauses.getSearchClausesId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"searchClausesId", new Object[] {newSearchClausesId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingSearchClausesId = result.get(0);

		Assert.assertEquals(existingSearchClausesId, newSearchClausesId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSearchClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("searchClausesId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"searchClausesId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SCSearchClauses addSCSearchClauses() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSearchClauses scSearchClauses = _persistence.create(pk);

		scSearchClauses.setMvccVersion(RandomTestUtil.nextLong());

		scSearchClauses.setGroupId(RandomTestUtil.nextLong());

		scSearchClauses.setCompanyId(RandomTestUtil.nextLong());

		scSearchClauses.setUserId(RandomTestUtil.nextLong());

		scSearchClauses.setUserName(RandomTestUtil.randomString());

		scSearchClauses.setCreateDate(RandomTestUtil.nextDate());

		scSearchClauses.setModifiedDate(RandomTestUtil.nextDate());

		scSearchClauses.setSetId(RandomTestUtil.nextLong());

		scSearchClauses.setSearchClauses(RandomTestUtil.randomString());

		_scSearchClauseses.add(_persistence.update(scSearchClauses));

		return scSearchClauses;
	}

	private List<SCSearchClauses> _scSearchClauseses =
		new ArrayList<SCSearchClauses>();
	private SCSearchClausesPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}