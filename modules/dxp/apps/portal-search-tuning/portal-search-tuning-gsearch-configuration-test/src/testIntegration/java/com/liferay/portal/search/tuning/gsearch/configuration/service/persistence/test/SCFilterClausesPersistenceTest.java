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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchFilterClausesException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SCFilterClauses;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SCFilterClausesLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCFilterClausesPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCFilterClausesUtil;
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
public class SCFilterClausesPersistenceTest {

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
		_persistence = SCFilterClausesUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SCFilterClauses> iterator = _scFilterClauseses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCFilterClauses scFilterClauses = _persistence.create(pk);

		Assert.assertNotNull(scFilterClauses);

		Assert.assertEquals(scFilterClauses.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SCFilterClauses newSCFilterClauses = addSCFilterClauses();

		_persistence.remove(newSCFilterClauses);

		SCFilterClauses existingSCFilterClauses =
			_persistence.fetchByPrimaryKey(newSCFilterClauses.getPrimaryKey());

		Assert.assertNull(existingSCFilterClauses);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSCFilterClauses();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCFilterClauses newSCFilterClauses = _persistence.create(pk);

		newSCFilterClauses.setMvccVersion(RandomTestUtil.nextLong());

		newSCFilterClauses.setGroupId(RandomTestUtil.nextLong());

		newSCFilterClauses.setCompanyId(RandomTestUtil.nextLong());

		newSCFilterClauses.setUserId(RandomTestUtil.nextLong());

		newSCFilterClauses.setUserName(RandomTestUtil.randomString());

		newSCFilterClauses.setCreateDate(RandomTestUtil.nextDate());

		newSCFilterClauses.setModifiedDate(RandomTestUtil.nextDate());

		newSCFilterClauses.setSetId(RandomTestUtil.nextLong());

		newSCFilterClauses.setFilterClauses(RandomTestUtil.randomString());

		_scFilterClauseses.add(_persistence.update(newSCFilterClauses));

		SCFilterClauses existingSCFilterClauses = _persistence.findByPrimaryKey(
			newSCFilterClauses.getPrimaryKey());

		Assert.assertEquals(
			existingSCFilterClauses.getMvccVersion(),
			newSCFilterClauses.getMvccVersion());
		Assert.assertEquals(
			existingSCFilterClauses.getFilterClausesId(),
			newSCFilterClauses.getFilterClausesId());
		Assert.assertEquals(
			existingSCFilterClauses.getGroupId(),
			newSCFilterClauses.getGroupId());
		Assert.assertEquals(
			existingSCFilterClauses.getCompanyId(),
			newSCFilterClauses.getCompanyId());
		Assert.assertEquals(
			existingSCFilterClauses.getUserId(),
			newSCFilterClauses.getUserId());
		Assert.assertEquals(
			existingSCFilterClauses.getUserName(),
			newSCFilterClauses.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCFilterClauses.getCreateDate()),
			Time.getShortTimestamp(newSCFilterClauses.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCFilterClauses.getModifiedDate()),
			Time.getShortTimestamp(newSCFilterClauses.getModifiedDate()));
		Assert.assertEquals(
			existingSCFilterClauses.getSetId(), newSCFilterClauses.getSetId());
		Assert.assertEquals(
			existingSCFilterClauses.getFilterClauses(),
			newSCFilterClauses.getFilterClauses());
	}

	@Test
	public void testCountBySetId() throws Exception {
		_persistence.countBySetId(RandomTestUtil.nextLong());

		_persistence.countBySetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SCFilterClauses newSCFilterClauses = addSCFilterClauses();

		SCFilterClauses existingSCFilterClauses = _persistence.findByPrimaryKey(
			newSCFilterClauses.getPrimaryKey());

		Assert.assertEquals(existingSCFilterClauses, newSCFilterClauses);
	}

	@Test(expected = NoSuchFilterClausesException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SCFilterClauses> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SCFilterClauses", "mvccVersion", true, "filterClausesId", true,
			"groupId", true, "companyId", true, "userId", true, "userName",
			true, "createDate", true, "modifiedDate", true, "setId", true,
			"filterClauses", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SCFilterClauses newSCFilterClauses = addSCFilterClauses();

		SCFilterClauses existingSCFilterClauses =
			_persistence.fetchByPrimaryKey(newSCFilterClauses.getPrimaryKey());

		Assert.assertEquals(existingSCFilterClauses, newSCFilterClauses);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCFilterClauses missingSCFilterClauses = _persistence.fetchByPrimaryKey(
			pk);

		Assert.assertNull(missingSCFilterClauses);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SCFilterClauses newSCFilterClauses1 = addSCFilterClauses();
		SCFilterClauses newSCFilterClauses2 = addSCFilterClauses();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCFilterClauses1.getPrimaryKey());
		primaryKeys.add(newSCFilterClauses2.getPrimaryKey());

		Map<Serializable, SCFilterClauses> scFilterClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, scFilterClauseses.size());
		Assert.assertEquals(
			newSCFilterClauses1,
			scFilterClauseses.get(newSCFilterClauses1.getPrimaryKey()));
		Assert.assertEquals(
			newSCFilterClauses2,
			scFilterClauseses.get(newSCFilterClauses2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SCFilterClauses> scFilterClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scFilterClauseses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SCFilterClauses newSCFilterClauses = addSCFilterClauses();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCFilterClauses.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SCFilterClauses> scFilterClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scFilterClauseses.size());
		Assert.assertEquals(
			newSCFilterClauses,
			scFilterClauseses.get(newSCFilterClauses.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SCFilterClauses> scFilterClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scFilterClauseses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SCFilterClauses newSCFilterClauses = addSCFilterClauses();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCFilterClauses.getPrimaryKey());

		Map<Serializable, SCFilterClauses> scFilterClauseses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scFilterClauseses.size());
		Assert.assertEquals(
			newSCFilterClauses,
			scFilterClauseses.get(newSCFilterClauses.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SCFilterClausesLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SCFilterClauses>() {

				@Override
				public void performAction(SCFilterClauses scFilterClauses) {
					Assert.assertNotNull(scFilterClauses);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SCFilterClauses newSCFilterClauses = addSCFilterClauses();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCFilterClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"filterClausesId", newSCFilterClauses.getFilterClausesId()));

		List<SCFilterClauses> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SCFilterClauses existingSCFilterClauses = result.get(0);

		Assert.assertEquals(existingSCFilterClauses, newSCFilterClauses);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCFilterClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"filterClausesId", RandomTestUtil.nextLong()));

		List<SCFilterClauses> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SCFilterClauses newSCFilterClauses = addSCFilterClauses();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCFilterClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("filterClausesId"));

		Object newFilterClausesId = newSCFilterClauses.getFilterClausesId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"filterClausesId", new Object[] {newFilterClausesId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingFilterClausesId = result.get(0);

		Assert.assertEquals(existingFilterClausesId, newFilterClausesId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCFilterClauses.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("filterClausesId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"filterClausesId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SCFilterClauses addSCFilterClauses() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCFilterClauses scFilterClauses = _persistence.create(pk);

		scFilterClauses.setMvccVersion(RandomTestUtil.nextLong());

		scFilterClauses.setGroupId(RandomTestUtil.nextLong());

		scFilterClauses.setCompanyId(RandomTestUtil.nextLong());

		scFilterClauses.setUserId(RandomTestUtil.nextLong());

		scFilterClauses.setUserName(RandomTestUtil.randomString());

		scFilterClauses.setCreateDate(RandomTestUtil.nextDate());

		scFilterClauses.setModifiedDate(RandomTestUtil.nextDate());

		scFilterClauses.setSetId(RandomTestUtil.nextLong());

		scFilterClauses.setFilterClauses(RandomTestUtil.randomString());

		_scFilterClauseses.add(_persistence.update(scFilterClauses));

		return scFilterClauses;
	}

	private List<SCFilterClauses> _scFilterClauseses =
		new ArrayList<SCFilterClauses>();
	private SCFilterClausesPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}