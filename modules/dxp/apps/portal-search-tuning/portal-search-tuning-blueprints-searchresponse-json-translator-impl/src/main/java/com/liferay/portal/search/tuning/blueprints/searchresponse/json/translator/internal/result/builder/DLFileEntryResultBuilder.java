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

package com.liferay.portal.search.tuning.blueprints.searchresponse.json.translator.internal.result.builder;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.searchresponse.json.translator.spi.result.ResultBuilder;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "model.class.name=com.liferay.document.library.kernel.model.DLFileEntry",
	service = ResultBuilder.class
)
public class DLFileEntryResultBuilder
	extends BaseResultBuilder implements ResultBuilder {

	@Override
	public String getThumbnail(
			Document document, BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		StringBundler sb = new StringBundler(7);

		sb.append("/documents/");
		sb.append(document.getString(Field.SCOPE_GROUP_ID));
		sb.append("/");
		sb.append(document.getString(Field.FOLDER_ID));
		sb.append("/");
		sb.append(document.getString("path"));
		sb.append("?imageThumbnail=1");

		return sb.toString();
	}

}