/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.test.util.query;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.search.internal.query.function.score.FieldValueFactorScoreFunctionImpl;
import com.liferay.portal.search.internal.query.function.score.GaussianDecayScoreFunctionImpl;
import com.liferay.portal.search.query.function.score.FieldValueFactorScoreFunction;
import com.liferay.portal.search.query.function.score.GaussianDecayScoreFunction;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author André de Oliveira
 */
public abstract class BaseScoreFunctionTranslatorTestCase {

	@Test
	public void testTranslate() {
		FieldValueFactorScoreFunction fieldValueFactorScoreFunction =
			new FieldValueFactorScoreFunctionImpl("priority");

		fieldValueFactorScoreFunction.setFactor(100.0F);
		fieldValueFactorScoreFunction.setMissing(0.0);
		fieldValueFactorScoreFunction.setModifier(
			FieldValueFactorScoreFunction.Modifier.LN1P);

		String string = translate(fieldValueFactorScoreFunction);

		_assertContains("\"factor\":100.0", string);
		_assertContains("\"field\":\"priority\"", string);
		_assertContains("\"missing\":0.0", string);
		_assertContains("\"modifier\":\"ln1p\"", string);
	}

	@Test
	public void testTranslateGaussianDecayWithoutMultiValueMode() {
		GaussianDecayScoreFunction gaussianDecayScoreFunction =
			new GaussianDecayScoreFunctionImpl(
				"publishDate", "now", "1825d", "180d");

		String string = translate(gaussianDecayScoreFunction);

		_assertContains("\"scale\":\"1825d\"", string);
		_assertNotContains("multi_value_mode", string);
	}

	@Test
	public void testTranslateGaussianDecayWithoutOrigin() {
		GaussianDecayScoreFunction gaussianDecayScoreFunction =
			new GaussianDecayScoreFunctionImpl(
				"publishDate", null, "1825d", "180d");

		String string = translate(gaussianDecayScoreFunction);

		_assertContains("\"scale\":\"1825d\"", string);
		_assertNotContains("\"origin\"", string);
	}

	protected abstract String translate(
		FieldValueFactorScoreFunction fieldValueFactorScoreFunction);

	protected abstract String translate(
		GaussianDecayScoreFunction gaussianDecayScoreFunction);

	private void _assertContains(String expected, String actual) {
		if (!actual.contains(expected)) {
			Assert.assertEquals(expected, actual);
		}
	}

	private void _assertNotContains(String unexpected, String actual) {
		if (actual.contains(unexpected)) {
			Assert.fail(
				StringBundler.concat(
					"Unexpected \"", unexpected, "\" found in ", actual));
		}
	}

}