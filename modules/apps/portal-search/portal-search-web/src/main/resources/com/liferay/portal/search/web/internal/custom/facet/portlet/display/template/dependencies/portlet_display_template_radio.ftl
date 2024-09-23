<@liferay_ui["panel-container"]
	extended=true
	id="${namespace + 'facetDatePanelContainer'}"
	markupView="lexicon"
	persistState=true
>
	<@liferay_ui.panel
		collapsible=true
		cssClass="search-facet"
		id="${namespace + 'facetDatePanel'}"
		markupView="lexicon"
		persistState=true
		title="date"
	>
		<#if !customFacetDisplayContext.isNothingSelected()>
			<@clay.button
				cssClass="btn-unstyled c-mb-4 facet-clear-btn"
				displayType="link"
				id="${namespace + 'facetDateClear'}"
				onClick="Liferay.Search.FacetUtil.clearSelections(event);"
			>
				<strong>${languageUtil.get(locale, "clear")}</strong>
			</@clay.button>
		</#if>

		<ul class="date list-unstyled">
			<#if entries?has_content>
				<#list entries as entry>
					<li class="facet-value">
						<div class="custom-control custom-radio">
							<label class="facet-checkbox-label" for="${namespace}${entry.getBucketText()}">
								<input
									${(entry.isSelected())?then("checked", "")}
									class="custom-control-input facet-term"
									disabled
									id="${namespace}${entry.getBucketText()}"
									name="${namespace}${entry.getBucketText()}"
									onChange='${"window.location.href = \"${entry.getFilterValue()}\";"}'
									role="radio"
									type="radio"
								/>

								<span class="custom-control-label term-name ${(entry.isSelected())?then('facet-term-selected', 'facet-term-unselected')}">
									<span class="custom-control-label-text">
										<#if entry.isSelected()>
											<strong><@liferay_ui["message"] key="${htmlUtil.escape(entry.getBucketText())}" /></strong>
										<#else>
											<@liferay_ui["message"] key="${htmlUtil.escape(entry.getBucketText())}" />
										</#if>
									</span>
								</span>

								<small class="term-count">
									(${entry.getFrequency()})
								</small>
							</label>
						</div>
					</li>
				</#list>
			</#if>

			<li class="facet-value">
				<div class="custom-control custom-radio">
					<label class="facet-checkbox-label" for="${namespace}${customDateRangeBucketDisplayContext.getBucketText()}">
						<input
							${(customDateRangeBucketDisplayContext.isSelected())?then("checked", "")}
							class="custom-control-input facet-term"
							disabled
							id="${namespace}${customDateRangeBucketDisplayContext.getBucketText()}"
							name="${namespace}${customDateRangeBucketDisplayContext.getBucketText()}"
							onChange='${"window.location.href = \"${customDateRangeBucketDisplayContext.getFilterValue()}\";"}'
							role="radio"
							type="radio"
						/>

						<span class="custom-control-label term-name ${(customDateRangeBucketDisplayContext.isSelected())?then('facet-term-selected', 'facet-term-unselected')}">
							<span class="custom-control-label-text">
								<#if customDateRangeBucketDisplayContext.isSelected()>
									<strong><@liferay_ui["message"] key="${htmlUtil.escape(customDateRangeBucketDisplayContext.getBucketText())}" /></strong>
								<#else>
									<@liferay_ui["message"] key="${htmlUtil.escape(customDateRangeBucketDisplayContext.getBucketText())}" />
								</#if>
							</span>
						</span>

						<#if customDateRangeBucketDisplayContext.isSelected()>
							<small class="term-count">
								(${customDateRangeBucketDisplayContext.getFrequency()})
							</small>
						</#if>
					</label>
				</div>
			</li>

			<div class="${(!customFacetCalendarDisplayContext.isSelected())?then("hide", "")} date-custom-range" id="${namespace}customRange">
				<div class="col-md-6" id="${namespace}customRangeFrom">
					<@liferay_aui["field-wrapper"] label="from">
						<@liferay_ui["input-date"]
							cssClass="date-facet-custom-range-input-date-from"
							dayParam="fromDay"
							dayValue=customFacetCalendarDisplayContext.getFromDayValue()
							disabled=false
							firstDayOfWeek=customFacetCalendarDisplayContext.getFromFirstDayOfWeek()
							monthParam="fromMonth"
							monthValue=customFacetCalendarDisplayContext.getFromMonthValue()
							name="${namespace}fromInput"
							yearParam="fromYear"
							yearValue=customFacetCalendarDisplayContext.getFromYearValue()
						/>
					</@>
				</div>

				<div class="col-md-6" id="${namespace}customRangeTo">
					<@liferay_aui["field-wrapper"] label="to">
						<@liferay_ui["input-date"]
							cssClass="date-facet-custom-range-input-date-to"
							dayParam="toDay"
							dayValue=customFacetCalendarDisplayContext.getToDayValue()
							disabled=false
							firstDayOfWeek=customFacetCalendarDisplayContext.getToFirstDayOfWeek()
							monthParam="toMonth"
							monthValue=customFacetCalendarDisplayContext.getToMonthValue()
							name="toInput"
							yearParam="toYear"
							yearValue=customFacetCalendarDisplayContext.getToYearValue()
						/>
					</@>
				</div>

				<@clay["button"]
					cssClass="date-facet-custom-range-filter-button"
					disabled=customFacetCalendarDisplayContext.isRangeBackwards()
					displayType="secondary"
					id="${namespace + 'searchCustomRangeButton'}"
					label="search"
					name="${namespace + 'searchCustomRangeButton'}"
				/>
			</div>
		</ul>
	</@>
</@>