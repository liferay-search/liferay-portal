/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.rest.client.dto.v1_0;

import com.liferay.portal.search.rest.client.function.UnsafeSupplier;
import com.liferay.portal.search.rest.client.serdes.v1_0.InferenceEndpointValidationSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Petteri Karttunen
 * @generated
 */
@Generated("")
public class InferenceEndpointValidation implements Cloneable, Serializable {

	public static InferenceEndpointValidation toDTO(String json) {
		return InferenceEndpointValidationSerDes.toDTO(json);
	}

	public Object getFieldErrors() {
		return fieldErrors;
	}

	public void setFieldErrors(Object fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

	public void setFieldErrors(
		UnsafeSupplier<Object, Exception> fieldErrorsUnsafeSupplier) {

		try {
			fieldErrors = fieldErrorsUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Object fieldErrors;

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public void setValid(
		UnsafeSupplier<Boolean, Exception> validUnsafeSupplier) {

		try {
			valid = validUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean valid;

	@Override
	public InferenceEndpointValidation clone()
		throws CloneNotSupportedException {

		return (InferenceEndpointValidation)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof InferenceEndpointValidation)) {
			return false;
		}

		InferenceEndpointValidation inferenceEndpointValidation =
			(InferenceEndpointValidation)object;

		return Objects.equals(
			toString(), inferenceEndpointValidation.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return InferenceEndpointValidationSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:1117438326