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
package org.apache.fineract.infrastructure.hooks.processor;

import com.squareup.okhttp.OkHttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

@SuppressWarnings("unused")
public class ProcessorHelper {

    private final static Logger logger = LoggerFactory
            .getLogger(ProcessorHelper.class);

    @SuppressWarnings("null")
    public static OkHttpClient configureClient(final OkHttpClient client) {
        final TrustManager[] certs = new TrustManager[] { new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain,
                    final String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain,
                    final String authType) throws CertificateException {
            }
        } };

        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, certs, new SecureRandom());
        } catch (KeyManagementException ex) {
            //Is also a part of java java.security.GeneralSecurityException but Seperated to give a more detailed
            //Explanantion, can occur by line 2 of try block
            //This occurs when there is a problem with the key provided in our case
            //First argument is null and wont cause any problem
            //Second argument is not manually generated and hence won't case a problem
            //There is no change needed, we will probably never end up here in our use case
            logger.error("Problem occurred in configureClient function",ex);
        } catch (NoSuchAlgorithmException e) {
            //Can occur by line 1
            //Should never occur in our case since we have HardCoded TLS
            //I have used the error message from Doc since I don't Know what exactly this would mean
            logger.error("No Provider supports a TrustManagerFactorySpi implementation for the specified protocol.", e);
            }

        try {
            final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(final String hostname,
                        final SSLSession session) {
                    return true;
                }
            };
            client.setHostnameVerifier(hostnameVerifier);
            client.setSslSocketFactory(ctx.getSocketFactory());
        } catch (final Exception e) {
            //The above code throws no exception except "llegalStateException "
            //It is thrown when init is not called on ctx which is not our case(we called it)
            //Was this just added for safety? Should I remove this?
            //Anyways we would need to solve this while adding IllegalCatch check
            //which donot support use of "Exception" class in it's most general form
            //So we can skip this for now
            logger.error("Problem occurred in configureClient function",e);
        }

        return client;
    }

    public static OkHttpClient createClient() {
        final OkHttpClient client = new OkHttpClient();
        return configureClient(client);
    }

    @SuppressWarnings("rawtypes")
    public static Callback createCallback(final String url) {

        return new Callback() {
            @Override
            public void success(final Object o, final Response response) {
                logger.info("URL: {}\tStatus: {}", url, response.getStatus());
            }

            @Override
            public void failure(final RetrofitError retrofitError) {
                logger.info("Error occured.", retrofitError);
            }
        };
    }

    public static WebHookService createWebHookService(final String url) {

        final OkHttpClient client = ProcessorHelper.createClient();

        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(url).setClient(new OkClient(client)).build();

        return restAdapter.create(WebHookService.class);
    }

}