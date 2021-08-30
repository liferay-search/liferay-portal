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

package com.liferay.search.experiences.blueprints.engine.internal.util;

import com.liferay.search.experiences.blueprints.engine.internal.aggregation.AggregationTranslatorFactory;
import com.liferay.search.experiences.blueprints.engine.internal.clause.ClauseTranslatorFactory;
import com.liferay.search.experiences.blueprints.engine.internal.condition.ConditionHandlerFactory;
import com.liferay.search.experiences.blueprints.engine.internal.suggester.SuggesterTranslatorFactory;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDataCreator;
import com.liferay.search.experiences.blueprints.engine.parameter.ParameterDefinition;
import com.liferay.search.experiences.blueprints.engine.util.BlueprintsEngineContextHelper;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = BlueprintsEngineContextHelper.class)
public class BlueprintsEngineContextHelperImpl
	implements BlueprintsEngineContextHelper {

	@Override
	public String[] getAvailableAggregationTranslatorNames() {
		return _aggregationBuilderFactory.getTranslatorNames();
	}

	@Override
	public String[] getAvailableClauseTranslatorNames() {
		return _clauseBuilderFactory.getTranslatorNames();
	}

	@Override
	public String[] getAvailableConditionHandlerNames() {
		return _conditionHandlerFactory.getHandlerNames();
	}

	@Override
	public String[] getAvailableSuggesterTranslatorNames() {
		return _suggesterBuilderFactory.getTranslatorNames();
	}

	@Override
	public Map<String, List<ParameterDefinition>>
		getContributedParameterDefinitions() {

		return _parameterDataCreator.getContributedParameterDefinitions();
	}

	@Reference
	private AggregationTranslatorFactory _aggregationBuilderFactory;

	@Reference
	private ClauseTranslatorFactory _clauseBuilderFactory;

	@Reference
	private ConditionHandlerFactory _conditionHandlerFactory;

	@Reference
	private ParameterDataCreator _parameterDataCreator;

	@Reference
	private SuggesterTranslatorFactory _suggesterBuilderFactory;

}