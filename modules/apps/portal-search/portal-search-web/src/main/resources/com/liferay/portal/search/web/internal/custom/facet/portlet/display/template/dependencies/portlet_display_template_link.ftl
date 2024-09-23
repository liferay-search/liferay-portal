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
						<a href="${htmlUtil.escape(entry.getFilterValue())}">
							<span class="term-name ${(entry.isSelected())?then("facet-term-selected", "facet-term-unselected")}">
								<#if entry.isSelected()>
									<strong><@liferay_ui["message"] key="${htmlUtil.escape(entry.getBucketText())}" /></strong>
								<#else>
									<@liferay_ui["message"] key="${htmlUtil.escape(entry.getBucketText())}" />
								</#if>
							</span>

							<#if entry.isFrequencyVisible()>
								<small class="term-count">
									(${entry.getFrequency()})
								</small>
							</#if>
						</a>
					</li>
				</#list>
			</#if>

			<#if customDateRangeBucketDisplayContext??>
				<li class="facet-value">
					<a href="${htmlUtil.escape(customDateRangeBucketDisplayContext.getFilterValue())}" id="${namespace}${customDateRangeBucketDisplayContext.getBucketText()}">
						<span class="term-name ${(customDateRangeBucketDisplayContext.isSelected())?then("facet-term-selected", "facet-term-unselected")}">
							<#if customDateRangeBucketDisplayContext.isSelected()>
								<strong><@liferay_ui["message"] key="${htmlUtil.escape(customDateRangeBucketDisplayContext.getBucketText())}" /></strong>
							<#else>
								<@liferay_ui["message"] key="${htmlUtil.escape(customDateRangeBucketDisplayContext.getBucketText())}" />
							</#if>
						</span>
					</a>
				</li>
			</#if>

			<#if customFacetCalendarDisplayContext??>
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
								name="fromInput"
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
			</#if>
		</ul>
	</@>
</@>