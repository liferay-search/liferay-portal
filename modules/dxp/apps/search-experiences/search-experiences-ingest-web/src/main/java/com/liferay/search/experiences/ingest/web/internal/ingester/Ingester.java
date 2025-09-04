/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.ingest.web.internal.ingester;

import com.liferay.search.experiences.ingest.web.internal.stats.IngestionStats;

import jakarta.portlet.ActionRequest;

/**
 * @author Petteri Karttunen
 */
public interface Ingester {

	public IngestionStats ingest(ActionRequest actionRequest);

}