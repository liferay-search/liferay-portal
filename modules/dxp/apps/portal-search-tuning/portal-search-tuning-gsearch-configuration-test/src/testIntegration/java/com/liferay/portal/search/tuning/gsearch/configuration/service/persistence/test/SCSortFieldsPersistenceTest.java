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
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchSortFieldsException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SCSortFields;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SCSortFieldsLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCSortFieldsPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SCSortFieldsUtil;
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
public class SCSortFieldsPersistenceTest {

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
		_persistence = SCSortFieldsUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SCSortFields> iterator = _scSortFieldses.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSortFields scSortFields = _persistence.create(pk);

		Assert.assertNotNull(scSortFields);

		Assert.assertEquals(scSortFields.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SCSortFields newSCSortFields = addSCSortFields();

		_persistence.remove(newSCSortFields);

		SCSortFields existingSCSortFields = _persistence.fetchByPrimaryKey(
			newSCSortFields.getPrimaryKey());

		Assert.assertNull(existingSCSortFields);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSCSortFields();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSortFields newSCSortFields = _persistence.create(pk);

		newSCSortFields.setMvccVersion(RandomTestUtil.nextLong());

		newSCSortFields.setGroupId(RandomTestUtil.nextLong());

		newSCSortFields.setCompanyId(RandomTestUtil.nextLong());

		newSCSortFields.setUserId(RandomTestUtil.nextLong());

		newSCSortFields.setUserName(RandomTestUtil.randomString());

		newSCSortFields.setCreateDate(RandomTestUtil.nextDate());

		newSCSortFields.setModifiedDate(RandomTestUtil.nextDate());

		newSCSortFields.setSetId(RandomTestUtil.nextLong());

		newSCSortFields.setSortFields(RandomTestUtil.randomString());

		_scSortFieldses.add(_persistence.update(newSCSortFields));

		SCSortFields existingSCSortFields = _persistence.findByPrimaryKey(
			newSCSortFields.getPrimaryKey());

		Assert.assertEquals(
			existingSCSortFields.getMvccVersion(),
			newSCSortFields.getMvccVersion());
		Assert.assertEquals(
			existingSCSortFields.getSortFieldsId(),
			newSCSortFields.getSortFieldsId());
		Assert.assertEquals(
			existingSCSortFields.getGroupId(), newSCSortFields.getGroupId());
		Assert.assertEquals(
			existingSCSortFields.getCompanyId(),
			newSCSortFields.getCompanyId());
		Assert.assertEquals(
			existingSCSortFields.getUserId(), newSCSortFields.getUserId());
		Assert.assertEquals(
			existingSCSortFields.getUserName(), newSCSortFields.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCSortFields.getCreateDate()),
			Time.getShortTimestamp(newSCSortFields.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSCSortFields.getModifiedDate()),
			Time.getShortTimestamp(newSCSortFields.getModifiedDate()));
		Assert.assertEquals(
			existingSCSortFields.getSetId(), newSCSortFields.getSetId());
		Assert.assertEquals(
			existingSCSortFields.getSortFields(),
			newSCSortFields.getSortFields());
	}

	@Test
	public void testCountBySetId() throws Exception {
		_persistence.countBySetId(RandomTestUtil.nextLong());

		_persistence.countBySetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SCSortFields newSCSortFields = addSCSortFields();

		SCSortFields existingSCSortFields = _persistence.findByPrimaryKey(
			newSCSortFields.getPrimaryKey());

		Assert.assertEquals(existingSCSortFields, newSCSortFields);
	}

	@Test(expected = NoSuchSortFieldsException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SCSortFields> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SCSortFields", "mvccVersion", true, "sortFieldsId", true,
			"groupId", true, "companyId", true, "userId", true, "userName",
			true, "createDate", true, "modifiedDate", true, "setId", true,
			"sortFields", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SCSortFields newSCSortFields = addSCSortFields();

		SCSortFields existingSCSortFields = _persistence.fetchByPrimaryKey(
			newSCSortFields.getPrimaryKey());

		Assert.assertEquals(existingSCSortFields, newSCSortFields);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSortFields missingSCSortFields = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSCSortFields);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SCSortFields newSCSortFields1 = addSCSortFields();
		SCSortFields newSCSortFields2 = addSCSortFields();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSortFields1.getPrimaryKey());
		primaryKeys.add(newSCSortFields2.getPrimaryKey());

		Map<Serializable, SCSortFields> scSortFieldses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, scSortFieldses.size());
		Assert.assertEquals(
			newSCSortFields1,
			scSortFieldses.get(newSCSortFields1.getPrimaryKey()));
		Assert.assertEquals(
			newSCSortFields2,
			scSortFieldses.get(newSCSortFields2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SCSortFields> scSortFieldses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scSortFieldses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SCSortFields newSCSortFields = addSCSortFields();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSortFields.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SCSortFields> scSortFieldses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scSortFieldses.size());
		Assert.assertEquals(
			newSCSortFields,
			scSortFieldses.get(newSCSortFields.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SCSortFields> scSortFieldses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(scSortFieldses.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SCSortFields newSCSortFields = addSCSortFields();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSCSortFields.getPrimaryKey());

		Map<Serializable, SCSortFields> scSortFieldses =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, scSortFieldses.size());
		Assert.assertEquals(
			newSCSortFields,
			scSortFieldses.get(newSCSortFields.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SCSortFieldsLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SCSortFields>() {

				@Override
				public void performAction(SCSortFields scSortFields) {
					Assert.assertNotNull(scSortFields);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SCSortFields newSCSortFields = addSCSortFields();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSortFields.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"sortFieldsId", newSCSortFields.getSortFieldsId()));

		List<SCSortFields> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SCSortFields existingSCSortFields = result.get(0);

		Assert.assertEquals(existingSCSortFields, newSCSortFields);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSortFields.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"sortFieldsId", RandomTestUtil.nextLong()));

		List<SCSortFields> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SCSortFields newSCSortFields = addSCSortFields();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSortFields.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("sortFieldsId"));

		Object newSortFieldsId = newSCSortFields.getSortFieldsId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"sortFieldsId", new Object[] {newSortFieldsId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingSortFieldsId = result.get(0);

		Assert.assertEquals(existingSortFieldsId, newSortFieldsId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SCSortFields.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("sortFieldsId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"sortFieldsId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SCSortFields addSCSortFields() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SCSortFields scSortFields = _persistence.create(pk);

		scSortFields.setMvccVersion(RandomTestUtil.nextLong());

		scSortFields.setGroupId(RandomTestUtil.nextLong());

		scSortFields.setCompanyId(RandomTestUtil.nextLong());

		scSortFields.setUserId(RandomTestUtil.nextLong());

		scSortFields.setUserName(RandomTestUtil.randomString());

		scSortFields.setCreateDate(RandomTestUtil.nextDate());

		scSortFields.setModifiedDate(RandomTestUtil.nextDate());

		scSortFields.setSetId(RandomTestUtil.nextLong());

		scSortFields.setSortFields(RandomTestUtil.randomString());

		_scSortFieldses.add(_persistence.update(scSortFields));

		return scSortFields;
	}

	private List<SCSortFields> _scSortFieldses = new ArrayList<SCSortFields>();
	private SCSortFieldsPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}