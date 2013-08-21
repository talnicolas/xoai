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

package com.lyncode.xoai.serviceprovider.iterators;

import com.lyncode.xoai.serviceprovider.HarvesterManager;
import com.lyncode.xoai.serviceprovider.configuration.Configuration;
import com.lyncode.xoai.serviceprovider.data.MetadataFormat;
import com.lyncode.xoai.serviceprovider.exceptions.IdDoesNotExistException;
import com.lyncode.xoai.serviceprovider.exceptions.InternalHarvestException;
import com.lyncode.xoai.serviceprovider.exceptions.NoMetadataFormatsException;
import com.lyncode.xoai.serviceprovider.util.URLEncoder;
import com.lyncode.xoai.serviceprovider.util.XMLUtils;
import com.lyncode.xoai.serviceprovider.verbs.ListMetadataFormats.ExtraParameters;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;


/**
 * @author Development @ Lyncode <development@lyncode.com>
 * @version 2.2.9
 */
public class MetadataFormatIterator
{
    private static Logger log = LogManager.getLogger(MetadataFormatIterator.class);
    
    private Configuration config;
    private String baseUrl;
    private String proxyIp;
    private int proxyPort;
    private ExtraParameters extra;

    public MetadataFormatIterator(Configuration configuration, String baseUrl, String proxyIp, int proxyPort,
                                  ExtraParameters extra)
    {
        super();
        this.config = configuration;
        this.baseUrl = baseUrl;
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
        this.extra = extra;
    }


    private Queue<MetadataFormat> _queue = null;
    
    private String makeUrl () {
        if (extra == null || extra.equals(""))
                return (baseUrl + "?verb=ListMetadataFormats");
        else
                return (baseUrl + "?verb=ListMetadataFormats" + URLEncoder.SEPARATOR + extra.toUrl());
        
    }
    
    private void harvest () throws InternalHarvestException, NoMetadataFormatsException, IdDoesNotExistException {
        HttpClient httpclient = new DefaultHttpClient();
        String url = makeUrl();
        log.info("Harvesting: "+url);
        HttpGet httpget = new HttpGet(url);
        httpget.addHeader("User-Agent", HarvesterManager.USERAGENT);
        httpget.addHeader("From", HarvesterManager.FROM);
        
        HttpResponse response = null;

        if(this.proxyIp != null && this.proxyPort > -1)
        {
            HttpHost proxy = new HttpHost(this.proxyIp, this.proxyPort);
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        try
        {
            response = httpclient.execute(httpget);
            StatusLine status = response.getStatusLine();
            
            log.debug(response.getStatusLine());
            
            if(status.getStatusCode() == 503) // 503 Status (must wait)
            {
                org.apache.http.Header[] headers = response.getAllHeaders();
                for (org.apache.http.Header h : headers) {
                    if (h.getName().equals("Retry-After")) {
                        String retry_time = h.getValue();
                        try {
							Thread.sleep(Integer.parseInt(retry_time)*1000);
						} catch (NumberFormatException e) {
							log.warn("Cannot parse "+retry_time+" to Integer", e);
						} catch (InterruptedException e) {
							log.debug(e.getMessage(), e);
						}
                        httpclient.getConnectionManager().shutdown();
                        httpclient = new DefaultHttpClient();
                        response = httpclient.execute(httpget);
                    }
                }
            }
            
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            
            Document doc = XMLUtils.parseDocument(instream);
            
            XMLUtils.checkListMetadataFormats(doc);
            
            
            NodeList listRecords = doc.getElementsByTagName("metadataFormat");
            for (int i = 0;i<listRecords.getLength();i++)
                _queue.add(XMLUtils.getMetadataFormat(listRecords.item(i)));
            
        }
        catch (IOException e)
        {
            throw new InternalHarvestException(e);
        } catch (ParserConfigurationException e) {
            throw new InternalHarvestException(e);
		} catch (SAXException e) {
            throw new InternalHarvestException(e);
		}
        
    }
    
    public boolean hasNext() throws NoMetadataFormatsException, IdDoesNotExistException, InternalHarvestException
    {
        if (_queue == null) {
            if (_queue == null) _queue = new LinkedList<MetadataFormat>();
            this.harvest();
        }
        
        return (_queue.size() > 0);
    }

    public MetadataFormat next()
    {
        return _queue.poll();
    }


}
