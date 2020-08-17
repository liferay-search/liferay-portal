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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.response.item.builder;

import com.liferay.journal.service.JournalArticleService;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util.ValueUtil;
import com.liferay.portal.search.tuning.blueprints.engine.response.ResponseAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.results.item.ResultItemBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "model.class.name=com.liferay.message.boards.model.MBMessage",
	service = ResultItemBuilder.class
)
public class MBMessageItemBuilder
	extends BaseResultItemBuilder implements ResultItemBuilder {

	@Override
	public String getTitle(
			SearchRequestContext searchRequestContext,
			ResponseAttributes responseAttributes, Document document)
		throws Exception {

		String title = getStringFieldContent(
			document, Field.CONTENT, searchRequestContext.getLocale());

		return ValueUtil.stripHTML(title, -1);
	}

	protected String getDLFileEntryCommentLink() {
		return null;
	}

	@Reference
	private JournalArticleService _journalArticleService;

	@Reference
	private Portal _portal;

}