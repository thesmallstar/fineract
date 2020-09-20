/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.integrationtests.common;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
public final class CurrenciesHelper {

    private CurrenciesHelper() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(CurrenciesHelper.class);
    private static final String CURRENCIES_URL = "/fineract-provider/api/v1/currencies";

    public static ArrayList<CurrencyDomain> getAllCurrencies(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String getAllCurrenciesURL = CURRENCIES_URL + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING ALL CURRENCIES -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, getAllCurrenciesURL, "");
        ArrayList<HashMap> selectedCurrencyOptions = (ArrayList<HashMap>) response.get("selectedCurrencyOptions");
        ArrayList<HashMap> currencyOptions = (ArrayList<HashMap>) response.get("currencyOptions");
        currencyOptions.addAll(selectedCurrencyOptions);
        final String jsonData = new Gson().toJson(new ArrayList<HashMap>(selectedCurrencyOptions));
        return new Gson().fromJson(jsonData, new TypeToken<ArrayList<CurrencyDomain>>() {}.getType());
    }

    public static ArrayList<CurrencyDomain> getSelectedCurrencies(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String getAllSelectedCurrenciesURL = CURRENCIES_URL + "?fields=selectedCurrencyOptions" + "&" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING ALL SELECTED CURRENCIES -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, getAllSelectedCurrenciesURL, "");
        final String jsonData = new Gson().toJson(response.get("selectedCurrencyOptions"));
        return new Gson().fromJson(jsonData, new TypeToken<ArrayList<CurrencyDomain>>() {}.getType());
    }

    public static CurrencyDomain getCurrencybyCode(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String code) {
        ArrayList<CurrencyDomain> currenciesList = getAllCurrencies(requestSpec, responseSpec);
        for (CurrencyDomain e : currenciesList) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static ArrayList<String> updateSelectedCurrencies(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final ArrayList<String> currencies) {
        final String currenciesUpdateURL = CURRENCIES_URL + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------UPDATE SELECTED CURRENCIES LIST---------------------------------------------");
        HashMap hash = Utils.performServerPut(requestSpec, responseSpec, currenciesUpdateURL, currenciesToJSON(currencies), "changes");
        return (ArrayList<String>) hash.get("currencies");
    }

    private static String currenciesToJSON(final ArrayList<String> currencies) {
        HashMap map = new HashMap<>();
        map.put("currencies", currencies);
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }
}
