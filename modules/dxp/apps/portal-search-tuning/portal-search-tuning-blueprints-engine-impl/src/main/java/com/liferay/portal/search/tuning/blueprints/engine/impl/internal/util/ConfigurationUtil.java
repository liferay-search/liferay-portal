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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.ClauseContext;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.FilterMode;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Occur;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.Operator;

/**
 * @author Petteri Karttunen
 */
public class ConfigurationUtil {

	public static ClauseContext getClauseContext(String context)
		throws IllegalArgumentException {

		context = StringUtil.toUpperCase(context);

		return ClauseContext.valueOf(context);
	}

	public static FilterMode getFilterMode(String filter)
		throws IllegalArgumentException {

		filter = StringUtil.toUpperCase(filter);

		return FilterMode.valueOf(filter);
	}

	public static Occur getItemOccur(String occur)
		throws IllegalArgumentException {

		occur = StringUtil.toUpperCase(occur);

		return Occur.valueOf(occur);
	}

	public static Operator getOperator(String operator)
		throws IllegalArgumentException {

		operator = StringUtil.toUpperCase(operator);

		return Operator.valueOf(operator);
	}

	public static SortOrder getSortOrder(String sortOrder)
		throws IllegalArgumentException {

		sortOrder = StringUtil.toUpperCase(sortOrder);

		return SortOrder.valueOf(sortOrder);
	}

}