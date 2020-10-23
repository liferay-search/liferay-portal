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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.keywords;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.spi.keywords.KeywordsProcessor;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(service = KeywordsProcessor.class)
public class MisspellingsKeywordsProcessor implements KeywordsProcessor {

	@Override
	public String process(
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		String keywords, Messages messages) {

		if (_allowMisspellings(blueprintsAttributes)) {
			return keywords;
		}

		// TODO Auto-generated method stub

		return null;
	}

	private boolean _allowMisspellings(
		BlueprintsAttributes blueprintsAttributes) {

		Optional<Object> optional = blueprintsAttributes.getAttributeOptional(
			ReservedParameterNames.ALLOW_MISSPELLING.getKey());

		if (!optional.isPresent()) {
			return false;
		}

		return GetterUtil.getBoolean(optional.get());
	}

}