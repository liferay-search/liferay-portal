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

package com.liferay.portal.search.tuning.blueprints.engine.internal.clause.condition.handler;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.clause.ConditionsConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.EvaluationType;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.exception.ParameterEvaluationException;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.condition.visitor.AnyWordInVisitor;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.condition.visitor.ConditionEvaluationVisitor;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.condition.visitor.ContainsVisitor;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.condition.visitor.EqualsVisitor;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.condition.visitor.GreaterThanVisitor;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.condition.visitor.InRangeVisitor;
import com.liferay.portal.search.tuning.blueprints.engine.internal.clause.condition.visitor.InVisitor;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.spi.clause.ConditionHandler;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=default",
	service = ConditionHandler.class
)
public class DefaultConditionHandler implements ConditionHandler {

	@Override
	public boolean isTrue(
		SearchRequestContext searchRequestContext,
		JSONObject configurationJsonObject) {

		if (_validateCondition(searchRequestContext, configurationJsonObject)) {
			return false;
		}

		String parameterName = configurationJsonObject.getString(
			ConditionsConfigurationKeys.PARAMETER_NAME.getJsonKey());

		SearchParameterData searchParameterData =
			searchRequestContext.getSearchParameterData();

		Optional<Parameter> parameterOptional =
			searchParameterData.getByConfigurationVariableName(parameterName);

		if (!parameterOptional.isPresent()) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.clause-condition-parameter-not-resolved", null,
					null, configurationJsonObject,
					ConditionsConfigurationKeys.PARAMETER_NAME.getJsonKey(),
					parameterName));

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Clause condition parameter could not be resolved [ " +
						configurationJsonObject + " ].");
			}

			return false;
		}

		String evaluationTypeString = configurationJsonObject.getString(
			ConditionsConfigurationKeys.EVALUATION_TYPE.getJsonKey());

		EvaluationType evaluationType;

		try {
			evaluationType = _getEvaluationType(evaluationTypeString);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unknown-clause-condition-evaluation-type",
					illegalArgumentException.getMessage(),
					illegalArgumentException, configurationJsonObject,
					ConditionsConfigurationKeys.EVALUATION_TYPE.getJsonKey(),
					evaluationTypeString));

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unknown clause condition evaluation type " +
						evaluationTypeString + ".",
					illegalArgumentException);
			}

			return false;
		}

		Parameter parameter = parameterOptional.get();

		ConditionEvaluationVisitor visitor = null;

		List<EvaluationType> supportedEvaluationTypes =
			parameter.getSupportedEvaluationTypes();

		if (EvaluationType.ANY_WORD_IN.equals(evaluationType) &&
			supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new AnyWordInVisitor(configurationJsonObject, false);
		}
		else if (EvaluationType.CONTAINS.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new ContainsVisitor(configurationJsonObject, false);
		}
		else if (EvaluationType.EQ.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new EqualsVisitor(configurationJsonObject, false);
		}
		else if (EvaluationType.GT.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new GreaterThanVisitor(
				configurationJsonObject, false, false);
		}
		else if (EvaluationType.GTE.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new GreaterThanVisitor(
				configurationJsonObject, false, true);
		}
		else if (EvaluationType.IN.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new InVisitor(configurationJsonObject, false);
		}
		else if (EvaluationType.IN_RANGE.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new InRangeVisitor(configurationJsonObject, false);
		}
		else if (EvaluationType.LT.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new GreaterThanVisitor(
				configurationJsonObject, true, false);
		}
		else if (EvaluationType.LTE.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new GreaterThanVisitor(
				configurationJsonObject, true, true);
		}
		else if (EvaluationType.NE.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new EqualsVisitor(configurationJsonObject, true);
		}
		else if (EvaluationType.NO_WORD_IN.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new AnyWordInVisitor(configurationJsonObject, true);
		}
		else if (EvaluationType.NOT_CONTAINS.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new ContainsVisitor(configurationJsonObject, true);
		}
		else if (EvaluationType.NOT_IN.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new InRangeVisitor(configurationJsonObject, true);
		}
		else if (EvaluationType.NOT_IN_RANGE.equals(evaluationType) &&
				 supportedEvaluationTypes.contains(evaluationType)) {

			visitor = new InRangeVisitor(configurationJsonObject, true);
		}

		if (visitor == null) {
			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.unable-to-resolve-clause-condition-handler",
					null, null, configurationJsonObject,
					ConditionsConfigurationKeys.EVALUATION_TYPE.getJsonKey(),
					evaluationType.name()));

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to resolve clause condition handler " +
						evaluationType.name() + ".");
			}

			return false;
		}

		try {
			return parameter.accept(visitor);
		}
		catch (ParameterEvaluationException parameterEvaluationException) {
			searchRequestContext.addMessage(
				parameterEvaluationException.getDetailsMessage());

			return false;
		}
	}

	private EvaluationType _getEvaluationType(String s)
		throws IllegalArgumentException {

		s = StringUtil.toUpperCase(s);

		return EvaluationType.valueOf(s);
	}

	private boolean _validateCondition(
		SearchRequestContext searchRequestContext,
		JSONObject configurationJsonObject) {

		boolean valid = true;

		if (!configurationJsonObject.has(
				ConditionsConfigurationKeys.PARAMETER_NAME.getJsonKey())) {

			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-clause-condition-parameter", null,
					null, configurationJsonObject,
					ConditionsConfigurationKeys.PARAMETER_NAME.getJsonKey(),
					null));

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Clause condition parameter undefined [ " +
						configurationJsonObject + " ].");
			}

			valid = false;
		}

		if (!configurationJsonObject.has(
				ConditionsConfigurationKeys.EVALUATION_TYPE.getJsonKey())) {

			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-clause-condition-evaluation-type",
					null, null, configurationJsonObject,
					ConditionsConfigurationKeys.EVALUATION_TYPE.getJsonKey(),
					null));

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Clause condition evaluation type undefined [ " +
						configurationJsonObject + " ].");
			}

			valid = false;
		}

		if (!configurationJsonObject.has(
				ConditionsConfigurationKeys.MATCH_VALUE.getJsonKey())) {

			searchRequestContext.addMessage(
				new Message(
					Severity.ERROR, "core",
					"core.error.undefined-clause-condition-match-value", null,
					null, configurationJsonObject,
					ConditionsConfigurationKeys.MATCH_VALUE.getJsonKey(),
					null));

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Clause condition match value undefined [ " +
						configurationJsonObject + " ].");
			}

			valid = false;
		}

		return valid;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DefaultConditionHandler.class);

}