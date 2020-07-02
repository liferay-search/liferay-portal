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

package com.liferay.portal.search.tuning.gsearch.impl.internal.parameter.visitor;

import com.liferay.portal.search.tuning.gsearch.parameter.BooleanParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.DateParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.DoubleParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.FloatParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.IntegerArrayParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.IntegerParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.LongArrayParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.LongParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.StringArrayParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.StringParameter;
import com.liferay.portal.search.tuning.gsearch.parameter.ToStringVisitor;

/**
 * @author Petteri Karttunen
 */
public interface ToConfigurationStringVisitor extends ToStringVisitor {

	public String visit(BooleanParameter parameter) throws Exception;

	public String visit(DateParameter parameter, String outputDateFormat)
		throws Exception;

	public String visit(DoubleParameter parameter) throws Exception;

	public String visit(FloatParameter parameter) throws Exception;

	public String visit(IntegerArrayParameter parameter) throws Exception;

	public String visit(IntegerParameter parameter) throws Exception;

	public String visit(LongArrayParameter parameter) throws Exception;

	public String visit(LongParameter parameter) throws Exception;

	public String visit(StringArrayParameter parameter) throws Exception;

	public String visit(StringParameter parameter) throws Exception;

}