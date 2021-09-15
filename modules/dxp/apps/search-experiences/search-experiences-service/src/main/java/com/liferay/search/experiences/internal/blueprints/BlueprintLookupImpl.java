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

package com.liferay.search.experiences.internal.blueprints;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.search.experiences.blueprints.Blueprint;
import com.liferay.search.experiences.blueprints.BlueprintLookup;
import com.liferay.search.experiences.exception.NoSuchSXPBlueprintException;
import com.liferay.search.experiences.model.SXPBlueprint;
import com.liferay.search.experiences.service.SXPBlueprintLocalService;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author André de Oliveira
 */
@Component(service = BlueprintLookup.class)
public class BlueprintLookupImpl implements BlueprintLookup {

	@Override
	public Optional<Blueprint> getBlueprintOptional(long blueprintId) {
		return Optional.ofNullable(
			_getSXPBlueprint(blueprintId)
		).map(
			BlueprintImpl::new
		);
	}

	private SXPBlueprint _getSXPBlueprint(long blueprintId) {
		try {
			return _sxpBlueprintLocalService.getSXPBlueprint(blueprintId);
		}
		catch (NoSuchSXPBlueprintException noSuchSXPBlueprintException) {
			_log.error(noSuchSXPBlueprintException);

			return null;
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintLookupImpl.class);

	@Reference
	private SXPBlueprintLocalService _sxpBlueprintLocalService;

}