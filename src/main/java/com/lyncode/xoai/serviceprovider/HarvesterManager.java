/**
 * Copyright 2012 Lyncode
 *
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
 * 
 * @author Development @ Lyncode <development@lyncode.com>
 * @version 2.2.9
 */

package com.lyncode.xoai.serviceprovider;

import com.lyncode.xoai.serviceprovider.exceptions.BadArgumentException;
import com.lyncode.xoai.serviceprovider.exceptions.CannotDisseminateFormatException;
import com.lyncode.xoai.serviceprovider.exceptions.IdDoesNotExistException;
import com.lyncode.xoai.serviceprovider.exceptions.InternalHarvestException;
import com.lyncode.xoai.serviceprovider.oaipmh.oai_dc.OAIDCParser;
import com.lyncode.xoai.serviceprovider.oaipmh.spec.RecordType;
import com.lyncode.xoai.serviceprovider.oaipmh.spec.schemas.oai_dc.OAIDC;
import com.lyncode.xoai.serviceprovider.util.ProcessingQueue;
import com.lyncode.xoai.serviceprovider.verbs.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.Properties;

/**
 * This class works as a wrapper to provide an API with all OAI-PMH possible requests.
 * It is based upon the definition at: http://www.openarchives.org/OAI/openarchivesprotocol.html
 * 
 * @author Development @ Lyncode <development@lyncode.com>
 * @version 2.2.9
 */
public class HarvesterManager
{
	private static Logger log = LogManager.getLogger(HarvesterManager.class);
    public static final String USERAGENT = "XOAI Service Provider by Lyncode.com";
    public static final String FROM = "general@lyncode.com";
    
    private static boolean configured = false;
    
    public static void main (String... args) {
    	//BasicConfigurator.configure();
    	HarvesterManager harvester = new HarvesterManager("http://demo.dspace.org/oai/request", log);

    	ListRecords lr = harvester.listRecords("oai_dc");
    	ProcessingQueue<RecordType> results = lr.harvest(new OAIDCParser(log));
    	while (!results.hasFinished()) {
    		RecordType rec = results.dequeue();
    		if (rec != null) {
    			OAIDC dc = (OAIDC) rec.getMetadata().getAny();
    			for (OAIDC.Element e : dc.getElements()) 
    				System.out.println(e.getName()+"="+e.getValue());
    		}
    	}
    	System.out.println("FINISH");
    }
    
    public void trustAllCertificates () {
    	if (!configured) {
	    	// Create a trust manager that does not validate certificate chains
	    	TrustManager[] trustAllCerts = new TrustManager[]{
	    	    new X509TrustManager() {
	    	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	    	            return null;
	    	        }
	    	        public void checkClientTrusted(
	    	            java.security.cert.X509Certificate[] certs, String authType) {
	    	        }
	    	        public void checkServerTrusted(
	    	            java.security.cert.X509Certificate[] certs, String authType) {
	    	        }
	    	    }
	    	};
	
	    	// Install the all-trusting trust manager
	    	try {
	    	    SSLContext sc = SSLContext.getInstance("SSL");
	    	    sc.init(null, trustAllCerts, new java.security.SecureRandom());
	    	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	    	} catch (Exception e) {
	    		log.debug(e.getMessage(), e);
	    	}
	    	
	    	configured = true;
    	}
    }
    
    private String baseUrl;
    private int intervalBetweenRequests;
    private String proxyIp = null;
    private int proxyPort = -1;
    private Logger logInstance;

    public HarvesterManager (String baseUrl, Logger log) {
        this.baseUrl = baseUrl;
        this.intervalBetweenRequests = 1000;
        this.logInstance = log;
    }
    public HarvesterManager (String baseUrl, int interval, Logger log) {
        this.baseUrl = baseUrl;
        this.intervalBetweenRequests = interval;
        this.logInstance = log;
    }
    public HarvesterManager (String baseUrl, String proxyIp, int proxyPort, Logger log) {
        this.baseUrl = baseUrl;
        this.intervalBetweenRequests = 1000;
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
        this.logInstance = log;
    }
    public HarvesterManager (String baseUrl, int interval, String proxyIp, int proxyPort, Logger log) {
        this.baseUrl = baseUrl;
        this.intervalBetweenRequests = interval;
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
        this.logInstance = log;
    }

    public int getIntervalBetweenRequests () {
        return intervalBetweenRequests;
    }

    public ListRecords listRecords (String metadataPrefix) {
        return new ListRecords(baseUrl, metadataPrefix, this.getIntervalBetweenRequests(), this.proxyIp, this.proxyPort,
                this.logInstance);
    }
    
    public ListRecords listRecords (String metadataPrefix, Parameters extra) {
        return new ListRecords(baseUrl, metadataPrefix, extra, this.getIntervalBetweenRequests(), this.proxyIp,
                this.proxyPort, this.logInstance);
    }

    public ListIdentifiers listIdentifiers (String metadataPrefix) {
        return new ListIdentifiers(baseUrl, metadataPrefix, new Parameters(), this.getIntervalBetweenRequests(),
                this.proxyIp, this.proxyPort, this.logInstance);
    }
    
    public ListIdentifiers listIdentifiers (String metadataPrefix, Parameters extra) {
        return new ListIdentifiers(baseUrl, metadataPrefix, extra, this.getIntervalBetweenRequests(),
                this.proxyIp, this.proxyPort, this.logInstance);
    }
    
    public ListMetadataFormats listMetadataFormats () {
        return new ListMetadataFormats(baseUrl, this.proxyIp, this.proxyPort, this.logInstance);
    }
    public ListMetadataFormats listMetadataFormats (Parameters extra) {
        return new ListMetadataFormats(baseUrl, extra, this.proxyIp, this.proxyPort, this.logInstance);
    }
    
    public ListSets listSets () {
        return new ListSets(baseUrl, this.getIntervalBetweenRequests(), this.proxyIp, this.proxyPort, this.logInstance);
    }
    
    public GetRecord getRecord (String identifier, String metadataPrefix) throws InternalHarvestException,
            BadArgumentException, CannotDisseminateFormatException, IdDoesNotExistException {
        return new GetRecord(baseUrl, identifier, metadataPrefix, this.proxyIp, this.proxyPort, this.logInstance);
    }
    
    public Identify identify () throws InternalHarvestException, BadArgumentException {
        return new Identify(baseUrl, this.proxyIp, this.proxyPort, this.logInstance);
    }
}
