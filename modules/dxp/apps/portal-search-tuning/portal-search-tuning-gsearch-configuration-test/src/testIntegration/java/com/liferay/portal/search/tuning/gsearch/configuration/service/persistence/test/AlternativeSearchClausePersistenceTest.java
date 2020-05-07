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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchAlternativeSearchClauseException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.AlternativeSearchClause;
import com.liferay.portal.search.tuning.gsearch.configuration.service.AlternativeSearchClauseLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.AlternativeSearchClausePersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.AlternativeSearchClauseUtil;
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
public class AlternativeSearchClausePersistenceTest {

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
		_persistence = AlternativeSearchClauseUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<AlternativeSearchClause> iterator =
			_alternativeSearchClauses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		AlternativeSearchClause alternativeSearchClause = _persistence.create(
			pk);

		Assert.assertNotNull(alternativeSearchClause);

		Assert.assertEquals(alternativeSearchClause.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		AlternativeSearchClause newAlternativeSearchClause =
			addAlternativeSearchClause();

		_persistence.remove(newAlternativeSearchClause);

		AlternativeSearchClause existingAlternativeSearchClause =
			_persistence.fetchByPrimaryKey(
				newAlternativeSearchClause.getPrimaryKey());

		Assert.assertNull(existingAlternativeSearchClause);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addAlternativeSearchClause();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		AlternativeSearchClause newAlternativeSearchClause =
			_persistence.create(pk);

		newAlternativeSearchClause.setGroupId(RandomTestUtil.nextLong());

		newAlternativeSearchClause.setCompanyId(RandomTestUtil.nextLong());

		newAlternativeSearchClause.setUserId(RandomTestUtil.nextLong());

		newAlternativeSearchClause.setUserName(RandomTestUtil.randomString());

		newAlternativeSearchClause.setCreateDate(RandomTestUtil.nextDate());

		newAlternativeSearchClause.setModifiedDate(RandomTestUtil.nextDate());

		newAlternativeSearchClause.setConfigurationSetId(
			RandomTestUtil.nextLong());

		newAlternativeSearchClause.setAlternativeSearchClause(
			RandomTestUtil.randomString());

		_alternativeSearchClauses.add(
			_persistence.update(newAlternativeSearchClause));

		AlternativeSearchClause existingAlternativeSearchClause =
			_persistence.findByPrimaryKey(
				newAlternativeSearchClause.getPrimaryKey());

		Assert.assertEquals(
			existingAlternativeSearchClause.getAlternativeSearchClauseId(),
			newAlternativeSearchClause.getAlternativeSearchClauseId());
		Assert.assertEquals(
			existingAlternativeSearchClause.getGroupId(),
			newAlternativeSearchClause.getGroupId());
		Assert.assertEquals(
			existingAlternativeSearchClause.getCompanyId(),
			newAlternativeSearchClause.getCompanyId());
		Assert.assertEquals(
			existingAlternativeSearchClause.getUserId(),
			newAlternativeSearchClause.getUserId());
		Assert.assertEquals(
			existingAlternativeSearchClause.getUserName(),
			newAlternativeSearchClause.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingAlternativeSearchClause.getCreateDate()),
			Time.getShortTimestamp(newAlternativeSearchClause.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingAlternativeSearchClause.getModifiedDate()),
			Time.getShortTimestamp(
				newAlternativeSearchClause.getModifiedDate()));
		Assert.assertEquals(
			existingAlternativeSearchClause.getConfigurationSetId(),
			newAlternativeSearchClause.getConfigurationSetId());
		Assert.assertEquals(
			existingAlternativeSearchClause.getAlternativeSearchClause(),
			newAlternativeSearchClause.getAlternativeSearchClause());
	}

	@Test
	public void testCountByConfigurationSetId() throws Exception {
		_persistence.countByConfigurationSetId(RandomTestUtil.nextLong());

		_persistence.countByConfigurationSetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		AlternativeSearchClause newAlternativeSearchClause =
			addAlternativeSearchClause();

		AlternativeSearchClause existingAlternativeSearchClause =
			_persistence.findByPrimaryKey(
				newAlternativeSearchClause.getPrimaryKey());

		Assert.assertEquals(
			existingAlternativeSearchClause, newAlternativeSearchClause);
	}

	@Test(expected = NoSuchAlternativeSearchClauseException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<AlternativeSearchClause>
		getOrderByComparator() {

		return OrderByComparatorFactoryUtil.create(
			"AlternativeSearchClause", "alternativeSearchClauseId", true,
			"groupId", true, "companyId", true, "userId", true, "userName",
			true, "createDate", true, "modifiedDate", true,
			"configurationSetId", true, "alternativeSearchClause", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		AlternativeSearchClause newAlternativeSearchClause =
			addAlternativeSearchClause();

		AlternativeSearchClause existingAlternativeSearchClause =
			_persistence.fetchByPrimaryKey(
				newAlternativeSearchClause.getPrimaryKey());

		Assert.assertEquals(
			existingAlternativeSearchClause, newAlternativeSearchClause);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		AlternativeSearchClause missingAlternativeSearchClause =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingAlternativeSearchClause);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		AlternativeSearchClause newAlternativeSearchClause1 =
			addAlternativeSearchClause();
		AlternativeSearchClause newAlternativeSearchClause2 =
			addAlternativeSearchClause();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newAlternativeSearchClause1.getPrimaryKey());
		primaryKeys.add(newAlternativeSearchClause2.getPrimaryKey());

		Map<Serializable, AlternativeSearchClause> alternativeSearchClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, alternativeSearchClauses.size());
		Assert.assertEquals(
			newAlternativeSearchClause1,
			alternativeSearchClauses.get(
				newAlternativeSearchClause1.getPrimaryKey()));
		Assert.assertEquals(
			newAlternativeSearchClause2,
			alternativeSearchClauses.get(
				newAlternativeSearchClause2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, AlternativeSearchClause> alternativeSearchClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(alternativeSearchClauses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		AlternativeSearchClause newAlternativeSearchClause =
			addAlternativeSearchClause();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newAlternativeSearchClause.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, AlternativeSearchClause> alternativeSearchClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, alternativeSearchClauses.size());
		Assert.assertEquals(
			newAlternativeSearchClause,
			alternativeSearchClauses.get(
				newAlternativeSearchClause.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, AlternativeSearchClause> alternativeSearchClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(alternativeSearchClauses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		AlternativeSearchClause newAlternativeSearchClause =
			addAlternativeSearchClause();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newAlternativeSearchClause.getPrimaryKey());

		Map<Serializable, AlternativeSearchClause> alternativeSearchClauses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, alternativeSearchClauses.size());
		Assert.assertEquals(
			newAlternativeSearchClause,
			alternativeSearchClauses.get(
				newAlternativeSearchClause.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			AlternativeSearchClauseLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod
				<AlternativeSearchClause>() {

				@Override
				public void performAction(
					AlternativeSearchClause alternativeSearchClause) {

					Assert.assertNotNull(alternativeSearchClause);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		AlternativeSearchClause newAlternativeSearchClause =
			addAlternativeSearchClause();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			AlternativeSearchClause.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"alternativeSearchClauseId",
				newAlternativeSearchClause.getAlternativeSearchClauseId()));

		List<AlternativeSearchClause> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		AlternativeSearchClause existingAlternativeSearchClause = result.get(0);

		Assert.assertEquals(
			existingAlternativeSearchClause, newAlternativeSearchClause);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			AlternativeSearchClause.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"alternativeSearchClauseId", RandomTestUtil.nextLong()));

		List<AlternativeSearchClause> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		AlternativeSearchClause newAlternativeSearchClause =
			addAlternativeSearchClause();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			AlternativeSearchClause.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("alternativeSearchClauseId"));

		Object newAlternativeSearchClauseId =
			newAlternativeSearchClause.getAlternativeSearchClauseId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"alternativeSearchClauseId",
				new Object[] {newAlternativeSearchClauseId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingAlternativeSearchClauseId = result.get(0);

		Assert.assertEquals(
			existingAlternativeSearchClauseId, newAlternativeSearchClauseId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			AlternativeSearchClause.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("alternativeSearchClauseId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"alternativeSearchClauseId",
				new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected AlternativeSearchClause addAlternativeSearchClause()
		throws Exception {

		long pk = RandomTestUtil.nextLong();

		AlternativeSearchClause alternativeSearchClause = _persistence.create(
			pk);

		alternativeSearchClause.setGroupId(RandomTestUtil.nextLong());

		alternativeSearchClause.setCompanyId(RandomTestUtil.nextLong());

		alternativeSearchClause.setUserId(RandomTestUtil.nextLong());

		alternativeSearchClause.setUserName(RandomTestUtil.randomString());

		alternativeSearchClause.setCreateDate(RandomTestUtil.nextDate());

		alternativeSearchClause.setModifiedDate(RandomTestUtil.nextDate());

		alternativeSearchClause.setConfigurationSetId(
			RandomTestUtil.nextLong());

		alternativeSearchClause.setAlternativeSearchClause(
			RandomTestUtil.randomString());

		_alternativeSearchClauses.add(
			_persistence.update(alternativeSearchClause));

		return alternativeSearchClause;
	}

	private List<AlternativeSearchClause> _alternativeSearchClauses =
		new ArrayList<AlternativeSearchClause>();
	private AlternativeSearchClausePersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}