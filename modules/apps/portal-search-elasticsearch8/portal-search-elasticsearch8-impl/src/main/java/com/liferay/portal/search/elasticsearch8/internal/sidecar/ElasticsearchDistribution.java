/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.sidecar;

import com.liferay.petra.string.StringBundler;

import java.util.Arrays;
import java.util.List;

/**
 * @author Bryan Engler
 */
public class ElasticsearchDistribution implements Distribution {

	public static final String VERSION = "9.5.4";

	@Override
	public Distributable getElasticsearchDistributable() {
		return new DistributableImpl(
			StringBundler.concat(
				"https://artifacts.elastic.co/downloads/elasticsearch",
				"/elasticsearch-", VERSION, "-linux-x86_64.tar.gz"),
			_ELASTICSEARCH_CHECKSUM);
	}

	@Override
	public List<Distributable> getPluginDistributables() {
		return Arrays.asList(
			new DistributableImpl(
				_getDownloadURLString("analysis-icu"), _ICU_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-kuromoji"), _KUROMOJI_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-smartcn"), _SMARTCN_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-stempel"), _STEMPEL_CHECKSUM));
	}

	private String _getDownloadURLString(String plugin) {
		return StringBundler.concat(
			"https://artifacts.elastic.co/downloads/elasticsearch-plugins/",
			plugin, "/", plugin, "-", VERSION, ".zip");
	}

	private static final String _ELASTICSEARCH_CHECKSUM =
		"3157b7155444463d8faedc15a430b001a362a33f48cd5226e501c8fdb121a33d14b2" +
			"bc52f2d690a2ad7f44edef09509b43f76dc680630ab4220332fcee14f54b";

	private static final String _ICU_CHECKSUM =
		"4c6e46f861005bdfc569da788e998f98675ec6d8fb496f93ee6b47f2a4fbb8884d8e" +
			"18d485d156dbffad5bdec347ff99aa4c4d81cffd5003979f2b7502dd1c48";

	private static final String _KUROMOJI_CHECKSUM =
		"5e48543511199b1879f8db1c378d17aaa67cc9b122d721916887ea3244d159d825b5" +
			"b6fcf30f057188d6506a294992b71f76ed7b711ceca1992993793e27da58";

	private static final String _SMARTCN_CHECKSUM =
		"6d41c374e4629dad1c4fc0d3cd1beab0a2ac000eda5ccb6d5e5cba241b947d093024" +
			"fc73b96e3a9e92ba515ec4bce2c89dfdf98e0692d1afae18cfa45d72aa32";

	private static final String _STEMPEL_CHECKSUM =
		"e67489dd65b726ed569d2a242413864a9b68ceb005435143f83032226a44efe5a483" +
			"ab44437d179d62363d5835df562750c48351f417d6d086e84138ee02e11e";

}