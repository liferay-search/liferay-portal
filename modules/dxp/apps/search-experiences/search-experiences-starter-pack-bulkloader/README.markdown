# Blueprints Bulk Loader

Creates Liferay Journal Articles from Google Places data or Wikipedia articles.

**_This modules is for internal testing purposes only. Requires the `search-experiences-federation-*` modules to be also deployed!_**

## Imporing Custom Google Places API Response JSON (upload)

Upload an arbitray JSON file containing a response from a Google Places API request (see below) and create Web Content Articles.

## Importing Federated Search Content (Liferay Help Center and Liferay Learn)

Preliminary crawler (Liferay Learn) and Zendesk API based (Liferay Help Center) ingesters to load articles from these sources and index them into Elasticsearch (no Web Content Articles created currently).

**_Requires further work to make them work again, ingestion fails with errors currently._**

Original dev branch: https://github.com/liferay-search/liferay-portal/tree/LPS-137590_Customer_Portal_Federated_Site_Search - [LPS-137590](https://issues.liferay.com/browse/LPS-137590)

## Importing Google Places Data

Imports location data created using the Google "Places API" as Web Content Articles. Adds also geopoints to an expando field "location".

Additional data sets can be added by:

1. Copying the results of a "Places API" request into a .json file

1. Placing the .json file in the resources directory

1. Putting an entry in ``FILENAME_TO_CITY_MAP`` in ``PlacesConstants`` class.

An API Key is needed to perform requests, see https://liferay.slack.com/archives/C0154CEGR3Q/p1598901770005800

### Example "Places API" requests

* Restaurants within ~10 miles of Los Angeles:
https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=$API_KEY&location=34.061645,-118.261353&radius=15000&type=restaurant

* Tourist Attractions within ~1 mile of New York:
https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=$API_KEY&location=40.761619,-73.972851&radius=1500&type=tourist_attraction

JSON results were formatted with https://jsonformatter.curiousconcept.com/

### List of supported "types" for a "Places API" request
https://developers.google.com/places/web-service/supported_types

## Importing IKEA Stores

Imports geolocation data of IKEA stores as Web Content Articles from Budapest, Chicago, Helsinki, Los Angeles and Minneapolis areas using .json files (included).

## Importing NPS National Parks

Imports geolocation data of U.S. National Parks as Web Content Articles from a .geojson file (included).

Original implementation can be found on the following dev branch: https://github.com/liferay-search/liferay-portal/tree/LPS-140079-blueprints-dxpc-demo-7.4.13.ep4 - [LPS-140079](https://issues.liferay.com/browse/LPS-140079)

## Importing Wikipedia Articles

Crawling starts from the given Wikipedia article(s) following the "links" in article metadata.

Please note that the import process takes time: importing hundreds of articles can take several minutes.
