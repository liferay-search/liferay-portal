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

package com.liferay.search.experiences.internal.validator;

import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.search.experiences.exception.SXPBlueprintConfigurationsJSONException;
import com.liferay.search.experiences.exception.SXPBlueprintTitleException;
import com.liferay.search.experiences.internal.validator.util.JSONSchemaValidatorUtil;
import com.liferay.search.experiences.problem.Problem;
import com.liferay.search.experiences.problem.Severity;
import com.liferay.search.experiences.validator.SXPBlueprintValidator;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SXPBlueprintValidator.class)
public class SXPBlueprintValidatorImpl implements SXPBlueprintValidator {

	@Override
	public void validate(String configurationsJSON)
		throws SXPBlueprintConfigurationsJSONException {

		if (Validator.isNull(configurationsJSON)) {
			return;
		}

		// TODO What should the standard be for JSON schema files?

		List<Problem> problems = JSONSchemaValidatorUtil.validate(
			SXPBlueprintValidatorImpl.class, configurationsJSON,
			"dependencies/sxpblueprint.schema.json");

		if (!ListUtil.isEmpty(problems)) {
			throw new SXPBlueprintConfigurationsJSONException(problems);
		}
	}

	@Override
	public void validate(
			String configurationsJSON, Map<Locale, String> titleMap)
		throws SXPBlueprintConfigurationsJSONException,
			   SXPBlueprintTitleException {

		validate(configurationsJSON);

		if (MapUtil.isEmpty(titleMap)) {
			throw new SXPBlueprintTitleException(
				ListUtil.fromArray(
					new Problem.Builder().message(
						"Title is empty"
					).severity(
						Severity.ERROR
					).build()));
		}
	}

}