/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.dto.v1_0;

import com.liferay.headless.delivery.client.function.UnsafeSupplier;
import com.liferay.headless.delivery.client.serdes.v1_0.ContentSetProviderSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class ContentSetProvider implements Cloneable, Serializable {

	public static ContentSetProvider toDTO(String json) {
		return ContentSetProviderSerDes.toDTO(json);
	}

	public String getItemSubtype() {
		return itemSubtype;
	}

	public void setItemSubtype(String itemSubtype) {
		this.itemSubtype = itemSubtype;
	}

	public void setItemSubtype(
		UnsafeSupplier<String, Exception> itemSubtypeUnsafeSupplier) {

		try {
			itemSubtype = itemSubtypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String itemSubtype;

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public void setItemType(
		UnsafeSupplier<String, Exception> itemTypeUnsafeSupplier) {

		try {
			itemType = itemTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String itemType;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setKey(UnsafeSupplier<String, Exception> keyUnsafeSupplier) {
		try {
			key = keyUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String key;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitle(
		UnsafeSupplier<String, Exception> titleUnsafeSupplier) {

		try {
			title = titleUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String title;

	@Override
	public ContentSetProvider clone() throws CloneNotSupportedException {
		return (ContentSetProvider)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ContentSetProvider)) {
			return false;
		}

		ContentSetProvider contentSetProvider = (ContentSetProvider)object;

		return Objects.equals(toString(), contentSetProvider.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return ContentSetProviderSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:-467000140