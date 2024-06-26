/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.thalesctvl;

import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import io.trino.spi.function.Description;
import io.trino.spi.function.ScalarFunction;
import io.trino.spi.function.SqlNullable;
import io.trino.spi.function.SqlType;
import io.trino.spi.type.StandardTypes;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;

public final class ThalesCTVLFunctions
{
    private ThalesCTVLFunctions() {}

    @ScalarFunction("ctvl_tokenize_char")
    @Description("Returns tokenizeed data")
    @SqlType(StandardTypes.VARCHAR)
    public static Slice ctvl_tokenize_char(@SqlNullable @SqlType(StandardTypes.VARCHAR) Slice inputstring)
    {
        //String userid =  System.getenv("CTSUSER");
        //String password =  System.getenv("CTSPWD");
        String userid = "cts-user";
        String password = "yourpwd";
        String ctsip = "yourip";
        String idpassword = userid + ":" + password;
        String encodedidpwd = null;
        disableCertValidation();
        encodedidpwd = Base64.getEncoder().encodeToString(idpassword.getBytes("UTF-8"));
        String httpsurl = "https://" + ctsip + "/vts/rest/v2.0/tokenize/";
        URL myurl = new URL(httpsurl);
        HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();
        String jStr = "{\"data\":\"" + inputstring + "\",\"tokengroup\":\"tg1\",\"tokentemplate\":\"tt1\"}";
        con.setRequestProperty("Content-length", String.valueOf(jStr.length()));
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Basic " + encodedidpwd);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setDoInput(true);
        DataOutputStream output = new DataOutputStream(con.getOutputStream());
        output.writeBytes(jStr);
        output.close();
        BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String line = "";
        String strResponse = "";
        while ((line = rd.readLine()) != null) {
            strResponse = strResponse + line;
        }
        rd.close();
        con.disconnect();
        System.out.println("Tokenize server: " + https_url);
        System.out.println("Tokenize request: " + jStr);
        System.out.println("Tokenize response: " + strResponse);
        return (Slices.utf8Slice(strResponse));
    }

    public static void disableCertValidation()
            throws Exception
    {
        TrustManager[] trustAllCerts = new TrustManager[]
        {
            new X509TrustManager()
            {
                public java.security.cert.X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                {
                }
                }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier()
        {
            public boolean verify(String hostname, SSLSession session)
            {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
